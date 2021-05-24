package Model.DB;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Date;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Pojo.Partecipazione;

public class Partecipazioni extends ResultsConverter {

    private static final String PARTECIPAZIONE_COLLECTION = "Partecipazioni";


    /** Remove the participation from a specifi event.
     *  The removed participation belogns to the logged in user.
     *
     * @param idEvento  event from which you want to remove the participation.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void removeMyPartecipation(String idEvento,ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idEvento",idEvento).whereEqualTo("idUtente",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                List<Partecipazione> p = convertResults(task,Partecipazione.class);

                if(p.size() == 1){
                    Task deletePartecipaizione = FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).document(p.get(0).getDocumentId()).delete();
                    Task decrementNumberOfPartecipant = Eventi.getDecrementNumberOfPartecipantTask(idEvento);

                    Task combinedTask = Tasks.whenAllComplete(deletePartecipaizione,decrementNumberOfPartecipant).addOnCompleteListener(task1 -> {
                        if(closureBool != null) closureBool.closure(task1.isSuccessful());
                    });

                }else{
                    if(closureBool != null) closureBool.closure(false);
                }
            });
        }
    }

    /** Add the partecipation to a specific event.
     *  The added user is the logged one.
     *
     * @param idEvento  Event whose participation you want to add.
     * @param accettazione  Flag indicating whether the user is accepted for the event.
     * @param listaAttesa   Flag indicating whether the user is placed on the waiting list.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void addMyPartecipation(String idEvento,boolean accettazione, boolean listaAttesa, ClosureBoolean closureBool){

        //Creating the Partecipazione instance
        Partecipazione p = new Partecipazione();
        p.setDataOra(new Date());
        p.setIdUtente(AuthHelper.getUserId());
        p.setIdEvento(idEvento);
        p.setAccettazione(accettazione);
        p.setListaAttesa(listaAttesa);

        Task incrementPartNum = Eventi.getIncrementNumberOfPartecipantTask(idEvento);
        Task addPartecipation = FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).add(p);

        Task combinedTask = Tasks.whenAllComplete(incrementPartNum,addPartecipation).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Return a list of Partecipazione with all the user's participation to events.
     *
     * @param closureList get called with a List of Partecipazione if the task is successful, null otherwise.
     */
    public static final void getMyPartecipations(ClosureList<Partecipazione> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(PARTECIPAZIONE_COLLECTION).whereEqualTo("idUtente",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if(closureList != null){
                    if(task.isSuccessful()) closureList.closure(convertResults(task,Partecipazione.class));
                    else closureList.closure(null);
                }
            });
        }
    }
}
