package Model.DB;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Collections;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.CodiceComunicazioneTampone;

public class CodiciComunicazioneTampone extends ResultsConverter {

    public static final String CODICI_COMUNICAZIONE_TAMPONE_COLLECTION = "CodiciComunicazioneTampone";


    /**
     * Return a list with all the code genrated by the logged-in ente.
     *
     * @param closureList invoked with the list of the code in case of success, null otherwise.
     */
    public static final void getAllMyGeneratedCode(ClosureList<CodiceComunicazioneTampone> closureList){
        if(!AuthHelper.isLoggedIn()) return;

        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).whereEqualTo("idEnteCreatore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
            if (closureList != null){
                if(task.isSuccessful()){
                    List<CodiceComunicazioneTampone> l = convertResults(task,CodiceComunicazioneTampone.class);

                    //Sorting by creation date, impossible to do into the query on different field
                    Collections.sort(l,Collections.reverseOrder());
                    closureList.closure(l);
                }else closureList.closure(null);
            }
        });
    }

    /**
     * Ask to the server to generate a new code with the given swab result.
     *
     * @param esitoTampone the swab result of the code.
     * @param closureRes invoked with the new code in case of success, null otherwise.
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


    /** Return a list of all code communication swab on Firestore.
     *
     * @param closureList ClosureList of CodiceComunicazioneTampone type.
     */
    public static final void getAllCode(ClosureList<CodiceComunicazioneTampone> closureList){
        FirestoreHelper.db.collection(CODICI_COMUNICAZIONE_TAMPONE_COLLECTION).get().addOnCompleteListener(task -> {
            if(closureList != null){
                if(task.isSuccessful()){
                    closureList.closure(convertResults(task,CodiceComunicazioneTampone.class));
                }else closureList.closure(null);
            }
        });
    }


    /**
     * Ritorna un oggetto CodiceComunicazioneTampone da Firestore
     * @param codice passaggio dell'id del CodiceComunicazioneTampone
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




    /** Update the information about used swab.
     *
     * @param codeCommunicationSwabId the id of the code communication swab
     * @param closureBool get called with true if the task is successful, false otherwise.
     * @param firstField the name of the first field to update
     * @param firstValue tha new value of the first field
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
