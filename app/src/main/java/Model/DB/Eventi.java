package Model.DB;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.*;



public class Eventi extends ResultsConverter {

    private static final String EVENTO_COLLECTION = "Eventi";


    /** Return the Firebase Task which increases the number of participants of a specific event.
     *
     * @param idEvento
     * @return the Firestore Task.
     */
    public static final Task getIncrementNumberOfPartecipantTask(String idEvento){
        return FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).update("numeroPartecipanti", FieldValue.increment(1));
    }

    /** Return the Firebase Task which decreases the number of participants of a specific event.
     *
     * @param idEvento
     * @return the Firestore Task.
     */
    public static final Task getDecrementNumberOfPartecipantTask(String idEvento){
        return FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).update("numeroPartecipanti", FieldValue.increment(-1));
    }

    /** Return a list of all the event created by the logged-in user.
     *
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getMyPartecipationEvents(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            Partecipazioni.getMyPartecipations(list -> {
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();

                for (Partecipazione p : list) {
                    Task t = FirestoreHelper.db.collection(EVENTO_COLLECTION).document(p.getIdEvento()).get();
                    taskList.add(t);
                }

                Task combinedTasks = Tasks.whenAllComplete(taskList).addOnCompleteListener(task -> {
                    if(closureList != null){
                        if(task.isSuccessful()){
                            List<Evento> finalList = new ArrayList<Evento>();
                            for(Task<?> t : task.getResult()){

                                DocumentSnapshot d = (DocumentSnapshot) t.getResult();
                                finalList.add(d.toObject(Evento.class));
                            }
                            closureList.closure(finalList);
                        }else closureList.closure(null);
                    }
                });
            });
        }
    }

    /** Return a specific event
     *
     * @param idEvento
     * @param closureRes the parameter is null in case the task is not successful.
     */
    public static final void getEvent(String idEvento, ClosureResult<Evento> closureRes){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(idEvento).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Evento e = task.getResult().toObject(Evento.class);
                if (closureRes != null) closureRes.closure(e);
            }else{
                if (closureRes != null) closureRes.closure(null);
            }
        });

    }

    /** Return a list of event created by a specific group
     *
     * @param groupId
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getAllGroupEvents(String groupId, ClosureList<Evento> closureList){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).whereEqualTo("idGruppoCreatore",groupId).get().addOnCompleteListener(task -> {
            if (closureList != null){
                if(task.isSuccessful()){
                    closureList.closure(convertResults(task,Evento.class));
                }else closureList.closure(null);
            }
        });
    }

    /** Return a list with all events created by the logged in user
     *
     * @param closureList the parameter list is null in case the task is not successful.
     */
    public static final void getMyEvent(ClosureList<Evento> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(EVENTO_COLLECTION).whereEqualTo("idUtenteCreatore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if (closureList != null){
                    if(task.isSuccessful()){
                        closureList.closure(convertResults(task,Evento.class));
                    }else closureList.closure(null);
                }
            });
        }
    }

    /** Add a new event to Firestore. The idEvent is randomly picked and the id inside the pojo object is avoided.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void addNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).add(e).addOnCompleteListener(task -> {
            if (closureBool != null) closureBool.closure(task.isSuccessful());
        });
    }

    /** Add a new event to Firestore. The idEvent on Firestore is the one inside the pojo object and it is not randomly picked.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void setNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(e.getId()).set(e).addOnCompleteListener(task -> {
            if (closureBool != null) closureBool.closure(task.isSuccessful());
        });
    }

    /** Return a list of all the event on Firestore.
     *
     * @param closureList ClosureList of Event type
     */
    public static final void getAllEvent(ClosureList<Evento> closureList){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).get().addOnCompleteListener(task -> {
            if(closureList != null){
                if(task.isSuccessful() ){
                    List<Evento> events = new ArrayList<>();
                    closureList.closure(convertResults(task,Evento.class));
                }else closureList.closure(null);
            }
        });
    }

    /** Upload the event image to the Firestore Storage. It is placed inside a directory named after the event id.
     * It is replaced if already present.
     * The image is placed inside the following path: Eventi/\idEvento\/locandina
     *
     * The user must be logged in.
     *
     * @param file  file to upload
     * @param idEvento  event whose image you want to change
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void uploadEventImage(Uri file, String idEvento, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = "Eventi/"+idEvento+"/locandina";
        StorageHelper.uploadImage(file,finalChildName,closureBool);
    }

    /** Download the event image from the Firestore Storage.
     *
     * User must be logged;
     *
     * @param idEvento  event whose image you want to download.
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadEventImage(String idEvento, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = EVENTO_COLLECTION + "/" + idEvento + "/locandina";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

}
