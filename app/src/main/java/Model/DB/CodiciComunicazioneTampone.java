package Model.DB;

import java.util.Collections;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.CodiceComunicazioneTampone;
import Model.Pojo.Evento;

public class CodiciComunicazioneTampone extends ResultsConverter {

    private static final String CODICI_COMUNICAZIONE_TAMPONE_COLLECTION = "CodiciComunicazioneTampone";


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

}
