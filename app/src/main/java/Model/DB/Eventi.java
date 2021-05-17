package Model.DB;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Pojo.*;



public class Eventi{

    private static final String EVENTO_COLLECTION = "Eventi";

    /** Add a new event to Firestore. The idEvent is randomly picked and the id inside the pojo object is avoided.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void addNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).add(e).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Add a new event to Firestore. The idEvent on Firestore is the one inside the pojo object and it is not randomly picked.
     *
     * @param e event to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void setNewEvent(Evento e, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(EVENTO_COLLECTION).document(e.getId()).set(e).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Return a list of all the event on Firestore.
     *
     * @param closureList ClosureList of Event type
     */
    public static final void getAllEvent(ClosureList<Evento> closureList){
        FirestoreHelper.db.collection(EVENTO_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && closureList != null){
                    List<Evento> events = new ArrayList<>();
                    for(QueryDocumentSnapshot document : task.getResult()){
                        Evento e = document.toObject(Evento.class);
                        events.add(e);
                    }
                    closureList.closure(events);
                }
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
