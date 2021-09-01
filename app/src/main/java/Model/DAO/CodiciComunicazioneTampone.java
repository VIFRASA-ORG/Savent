package Model.DAO;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import java.util.Collections;
import java.util.List;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.CodiceComunicazioneTampone;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti i CodiciComunicazioneTampone
 * memorizzati su firestore.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO CodiceComunicazioneTampone.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe CodiceComunicazioneTampone.
 */
public class CodiciComunicazioneTampone extends ResultsConverter {

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    public static final String CODICI_COMUNICAZIONE_TAMPONE_COLLECTION = "CodiciComunicazioneTampone";


    /**
     * Metodo che ritorna tutti i codici generati dall'ente loggato.
     *
     * @param closureList invocato con la lista di oggetti di classe CodiceComunicazioneTampone in caso di successo, null altrimenti.
     */
    public static final void getAllMyGeneratedCode(ClosureList<CodiceComunicazioneTampone> closureList){
        if(!AuthHelper.isLoggedIn()) return;

        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).whereEqualTo("idEnteCreatore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
            if (closureList != null){
                if(task.isSuccessful()){
                    List<CodiceComunicazioneTampone> l = convertResults(task,CodiceComunicazioneTampone.class);

                    //Ordino la lista in base alla data di creazione, infattibile direttamente nella query
                    Collections.sort(l,Collections.reverseOrder());
                    closureList.closure(l);
                }else closureList.closure(null);
            }
        });
    }

    /**
     * Metodo che richiede al server Firestore di generare un nuovo CodiceComunicazioneTampone dato il risultato del tampone.
     * Viene creato un nuovo oggetto all'interno della collection CodiciComunicazioneTampone.
     *
     * @param esitoTampone esito del tampone.
     * @param closureRes invocato con il nuovo codice in caso di successo, null altrimenti.
     */
    public static final void generateNewCode(boolean esitoTampone, ClosureResult<CodiceComunicazioneTampone> closureRes){
        if(!AuthHelper.isLoggedIn()) return;

        CodiceComunicazioneTampone codice = new CodiceComunicazioneTampone(esitoTampone, AuthHelper.getUserId());

        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).add(codice).addOnCompleteListener( task -> {
            if(task.isSuccessful()){
                if(closureRes != null){
                    codice.setId(task.getResult().getId());
                    closureRes.closure(codice);
                }
            }else{
                if(closureRes != null){
                    closureRes.closure(null);
                }
            }
        });
    }

    /**
     * Metodo che ritorna un oggetto CodiceComunicazioneTampone da Firestore con l'id passato come parametro.
     *
     * @param codice  id del CodiceComunicazioneTampone che si vuole.
     * @param closureResult in caso di successo ritorna un oggetto CodiceComunicazioneTampone, altrimenti null.
     */
    public static final void getCode(String codice, ClosureResult<CodiceComunicazioneTampone> closureResult ){
        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(codice).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if (closureResult != null) closureResult.closure(task.getResult().toObject(CodiceComunicazioneTampone.class));
            }else{
                if (closureResult != null) closureResult.closure(null);
            }
        });
    }

    /**
     * Metodo che permette di modificare un campo del CodiceComunicazioneTampone con id passato come parametro.
     *
     * @param codeCommunicationSwabId id del CodiceComunicazioneTampone di cui si vole cambiare il valore.
     * @param closureBool invocato con true se l'esecuzione va a buon fine, false altrimenti.
     * @param firstField il nome del campo da aggiornare
     * @param firstValue nuovo valore da inserire nel campo sopra citato.
     */
    public static final void updateFields(String codeCommunicationSwabId, ClosureBoolean closureBool, String firstField, Object firstValue){
        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).document(codeCommunicationSwabId).update(firstField,firstValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }
}
