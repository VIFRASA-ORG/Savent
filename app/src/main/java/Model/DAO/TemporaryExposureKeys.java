package Model.DAO;

import android.content.Context;
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
import java.util.Calendar;
import java.util.Date;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.LocalStorage.SQLiteHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.CodiceComunicazioneTampone;
import Model.POJO.TemporaryExposureKey;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti i TEK
 * memorizzati su firestore sia nella collection "TemporaryExposureKeys" che "Positivi"
 * contententi rispetivamente tutti i TEK generati e tutti i TEK di utenti positivi.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO TemporaryExposureKey.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe TemporaryExposureKey.
 */
public class TemporaryExposureKeys extends ResultsConverter{

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     * La prima indica la tabella che genera i nuovi TEK
     * La seconda indica la tabella dove vengono messi i TEK di utenti positivi.
     */
    private static final String TEK_GENERATION_COLLECTION = "TemporaryExposureKeys";
    private static final String POSITIVI_COLLECTION = "Positivi";

    /**
     * COSTANTI
     */
    public static final int DANGER_CONTACT_THRESHOLD = 20;
    public static final int DANGER_CONTACT_SINGLE_VALUE = 5;


    /**
     * Metodo che lascia creare un nuovo TEK per il device corrente al server Firebase.
     * Il nuovo TEK viene poi salvato nella tabella TemporaryExposureKey del database locale SQLite.
     *
     * @param context contesto dell'activity.
     * @param closureRes invocato con la stringa contenente il nuovo TEK se il task va a buon fine, null altrimenti.
     */
    public final static void generateNewTEK(Context context, ClosureResult<String> closureRes){
        TemporaryExposureKey t = new TemporaryExposureKey();
        t.setData(new Date());

        FirestoreHelper.db.collection(TEK_GENERATION_COLLECTION).add(t).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                //Saving the new tek to the Internal SQLite database
                Calendar c = Calendar.getInstance();
                c.setTime(t.getData());

                SQLiteHelper db = new SQLiteHelper(context);
                db.insertNewTek(task.getResult().getId(), c);

                if (closureRes!= null) closureRes.closure(task.getResult().getId());
            }else{
                if (closureRes!= null) closureRes.closure(null);
            }
        });
    }

    /**
     * Metodo che effettua le seguenti transaction, controllo dell'utilizzo di un tampone, inserimento dello status sanitario inerente
     * al tampone e inserimento in caso di positività di tutti i codici generati in locale.
     * (In caso una delle seguenti transaction non va a buon fine, tutte le altre transaction termineranno la loro esecuzione)
     *
     * @param codiceComunicazioneTampone stringa contenente il codice identificativo del tampone
     * @param codici arrayList contenente tutti i codici dell'utente che è risultato positivo da inserire all'interno di Firestore
     * @param closureBoolean
     */
    public static final void communicatePositiveTekTransaction(String codiceComunicazioneTampone, ArrayList<TemporaryExposureKey> codici, ClosureBoolean closureBoolean) {
        // Avvio della transaction
        // In caso la transaction vada a buon fine ritorna true
        FirestoreHelper.db.runTransaction((Transaction.Function<Void>) transaction -> {
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
            for(TemporaryExposureKey c : codici) {
                // Transaction per inserire i codici positivi all'interno della tabella "Positivi" su Firestore
                transaction.set(FirestoreHelper.db.collection(POSITIVI_COLLECTION).document(c.getId()), c);
            }
            return null;
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
     *
     * @param codiceComunicazioneTampone stringa contenente il codice identificativo del tampone
     * @param closureBoolean
     */
    public static final void communicateNegativeTekTransaction(String codiceComunicazioneTampone,ClosureBoolean closureBoolean) {
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
     *
     * @param p oggetto della classe TemporaryExposureKey
     * @param closureResult in caso di successo ritorna la stringa contenente l'id, altrimenti null.
     */
    public static final void addPositiveTek(TemporaryExposureKey p, ClosureResult<String> closureResult){
        FirestoreHelper.db.collection(POSITIVI_COLLECTION).document(p.getId()).set(p).addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                if (closureResult != null) closureResult.closure(p.getId());
            }else{
                if (closureResult != null) closureResult.closure(null);
            }
        });
    }

    /**
     * Metodo che scarica tutta la lista di tek positivi da firebase.
     *
     * @param closureList lista di tek positivi se esiste, null altrimenti.
     */
    private static final void downloadAllPositiveTek(ClosureList<TemporaryExposureKey> closureList){
        FirestoreHelper.db.collection(POSITIVI_COLLECTION).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(closureList!=null) closureList.closure(convertResults(task, TemporaryExposureKey.class));
            }else{
                if(closureList!= null) closureList.closure(null);
            }
        });
    }

    /**
     * Metodo che scarica tutti i tek positivi dopo una data di threshold.
     * Se la data di threshold è null, vengono scaricati tutti i tek positivi
     * presenti su firebase.
     *
     * @param lastUpdateDate data di threshold da cui partire a scaricare i tek, la data stessa è esclusa.
     * @param closureList closure invocata con la lista di tek se il task ha successo, null altrimenti.
     */
    public static final void downloadPositiveTek(Date lastUpdateDate, ClosureList<TemporaryExposureKey> closureList) {
        if(lastUpdateDate == null) downloadAllPositiveTek(closureList);
        else downloadLatestPositiveTek(lastUpdateDate,closureList);
    }

    /**
     * Metodo che scarica tutti i tek positivi dopo una data di threshold.
     *
     * @param lastUpdateDate data di threshold da cui partire a scaricare i tek, la data stessa è esclusa.
     * @param closureList closure invocata con la lista di tek se il task ha successo, null altrimenti.
     */
    private static final void downloadLatestPositiveTek(Date lastUpdateDate, ClosureList<TemporaryExposureKey> closureList){

        FirestoreHelper.db.collection(POSITIVI_COLLECTION).whereGreaterThan("data",lastUpdateDate).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(closureList!=null) closureList.closure(convertResults(task, TemporaryExposureKey.class));
            }else{
                if(closureList!= null) closureList.closure(null);
            }
        });
    }
}
