package Model.DB;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.Pojo.CodiceComunicazioneTampone;
import Model.Pojo.CodiceIdentificativo;

public class CodiciIdentificativi {

    private static final String POSITIVI_COLLECTION = "Positivi";

    /**
     * Metodo che effettua le seguenti transaction, controllo dell'utilizzo di un tampone, inserimento dello status sanitario inerente
     * al tampone e inserimento in caso di positività di tutti i codici generati in locale.
     * (In caso una delle seguenti transaction non va a buon fine, tutte le altre transaction termineranno la loro esecuzione)
     * @param codiceComunicazioneTampone stringa contenente il codice identificativo del tampone
     * @param codici arrayList contenente tutti i codici dell'utente che è risultato positivo da inserire all'interno di Firestore
     * @param closureBoolean
     */
    public static final void communicatePositiveCodeTransaction(String codiceComunicazioneTampone, ArrayList<CodiceIdentificativo> codici, ClosureBoolean closureBoolean) {
        // Avvio della transaction
        FirestoreHelper.db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot document = transaction.get(FirestoreHelper.db.collection(CodiciComunicazioneTampone.CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(codiceComunicazioneTampone));
                CodiceComunicazioneTampone cct = document.toObject(CodiceComunicazioneTampone.class);

                // Controlla se il codice è stato già utilizzato
                if(cct.getUsato()) {
                    throw new FirebaseFirestoreException(Resources.getSystem().getString(R.string.codiceUsato), FirebaseFirestoreException.Code.ABORTED);
                }

                // Transaction per aggiornare il campo "usato" all'interno di CodiceComunicazioneTampone su Firestore
                transaction.update(FirestoreHelper.db.collection(CodiciComunicazioneTampone.CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(cct.getId()), "usato", true);
                // Transaction per aggiornare il campo "statusSanitario" all'interno di Utenti su Firestore
                transaction.update(FirestoreHelper.db.collection(Utenti.UTENTI_COLLECTION).document(AuthHelper.getUserId()), "statusSanitario", 100);
                for(CodiceIdentificativo c : codici) {
                    // Transaction per inserire i codici positivi all'interno della tabella "Positivi" su Firestore
                    transaction.set(FirestoreHelper.db.collection(POSITIVI_COLLECTION).document(c.getId()), c);
                }
                return null;
            };
            // In caso la transaction vada a buon fine ritorna true
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(closureBoolean != null) closureBoolean.closure(true);
            }
            // In caso la transaction non vada a buon fine ritorna false
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(closureBoolean != null) closureBoolean.closure(false);
            }
        });
    }



    /**
     * Metodo che effettua le seguenti transaction, controllo dell'utilizzo di un tampone, inserimento dello status sanitario inerente al tampone.
     * (In caso una delle seguenti transaction non va a buon fine, tutte le altre transaction termineranno la loro esecuzione)
     * @param codiceComunicazioneTampone stringa contenente il codice identificativo del tampone
     * @param closureBoolean
     */
    public static final void communicateNegativeCodeTransaction(String codiceComunicazioneTampone,ClosureBoolean closureBoolean) {
        // Avvio della transaction
        FirestoreHelper.db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot document = transaction.get(FirestoreHelper.db.collection(CodiciComunicazioneTampone.CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(codiceComunicazioneTampone));
                CodiceComunicazioneTampone cct = document.toObject(CodiceComunicazioneTampone.class);
                // Controlla se il codice è stato già utilizzato
                if(cct.getUsato()) {
                    throw new FirebaseFirestoreException(Resources.getSystem().getString(R.string.codiceUsato), FirebaseFirestoreException.Code.ABORTED);
                }

                // Transaction per aggiornare il campo "usato" all'interno di CodiceComunicazioneTampone su Firestore
                transaction.update(FirestoreHelper.db.collection(CodiciComunicazioneTampone.CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(cct.getId()), "usato", true);
                // Transaction per aggiornare il campo "statusSanitario" all'interno di Utenti su Firestore
                transaction.update(FirestoreHelper.db.collection(Utenti.UTENTI_COLLECTION).document(AuthHelper.getUserId()), "statusSanitario", 0);
                return null;
            };
            // In caso la transaction vada a buon fine ritorna true
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if(closureBoolean != null) closureBoolean.closure(true);
            }
            // In caso la transaction non vada a buon fine ritorna false
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
                if(closureBoolean != null) closureBoolean.closure(false);
            }
        });
    }


    /**
     * Metodo per l'inserimento dei codici positivi dell'utente loggato su Firestore
     * @param p oggetto della classe CodiceIdentificativo
     * @param closureResult in caso di successo ritorna la stringa contenente l'id, altrimenti null.
     */
    public static final void addCodePositive(CodiceIdentificativo p, ClosureResult<String> closureResult){
        FirestoreHelper.db.collection(POSITIVI_COLLECTION).document(p.getId()).set(p).addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                if (closureResult != null) closureResult.closure(p.getId());
            }else{
                if (closureResult != null) closureResult.closure(null);
            }
        });
    }



}
