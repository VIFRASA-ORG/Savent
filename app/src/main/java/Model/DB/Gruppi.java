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

import java.util.ArrayList;
import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.Evento;
import Model.Pojo.Gruppo;
import Model.Pojo.Utente;

public class Gruppi extends ResultsConverter{

    private static final String GRUPPO_COLLECTION = "Gruppi";

    /**
     * Return the name of the group given as parameter.
     *
     * @param groupId id of the group whose name you want to know.
     * @param closureRes get called with the value if the task is successful, null otherwise.
     */
    public static final void getGroupName(String groupId, ClosureResult<String> closureRes){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(groupId).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if (closureRes != null){
                        Gruppo g = task.getResult().toObject(Gruppo.class);
                        closureRes.closure(g.getNome());
                    }
                }else {
                    if(closureRes != null) closureRes.closure(null);
                }
            });
        }
    }

    /** Remove a user from a group.
     *
     * @param idUser    id of the user to remove from the group.
     * @param idGroup   group from which the user is to be removed.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void removeUserFromGroup(String idUser, String idGroup, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGroup).update("idComponenti", FieldValue.arrayRemove(idUser)).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /** Add a new user to a group.
     *
     * @param idNewUser id of the new user to insert into the group
     * @param idGroup   group in which the new user is to be entered
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void addUserToGroup(String idNewUser, String idGroup, ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGroup).update("idComponenti", FieldValue.arrayUnion(idNewUser)).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /** Return a list of Gruppo of which the user is the admin or a member.
     *
     * @param closureList get called with a List of Gruppo if the task is successful, null otherwise.
     */
    public static final void getAllMyGroups(ClosureList<Gruppo> closureList){
        if(AuthHelper.isLoggedIn()){
            //Group of which the user is the administrator
            Task firstTask = FirestoreHelper.db.collection(GRUPPO_COLLECTION).whereEqualTo("idAmministratore",AuthHelper.getUserId()).get();
            Task secondTask = FirestoreHelper.db.collection(GRUPPO_COLLECTION).whereArrayContains("idComponenti",AuthHelper.getUserId()).get();

            Task combinedTask = Tasks.whenAllComplete(firstTask,secondTask).addOnCompleteListener(task -> {
                if(task.isSuccessful() && closureList != null){
                    List<Gruppo> finalList = new ArrayList<Gruppo>();
                    for(Task<?> t : task.getResult()){
                        finalList.addAll(convertResults((Task<QuerySnapshot>) t,Gruppo.class));
                    }
                    closureList.closure(finalList);
                }
            });
        }
    }

    /** Return the list of Gruppo of which the user is the admin.
     *
     * @param closureList get called with a List of Gruppo if the task is successful, null otherwise.
     */
    public static final void getAdministrationGroups(ClosureList<Gruppo> closureList){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(GRUPPO_COLLECTION).whereEqualTo("idAmministratore",AuthHelper.getUserId()).get().addOnCompleteListener(task -> {
                if (closureList != null){
                    if(task.isSuccessful() ){
                        closureList.closure(convertResults(task,Gruppo.class));
                    }else closureList.closure(null);
                }

            });
        }
    }

    /** Add a new group to Firestore. The idGroup is randomly picked and the id inside the pojo object is avoided.
     *
     * @param g group to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void addNewGroup(Gruppo g, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(GRUPPO_COLLECTION).add(g).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String groupId = task.getResult().getId();
                uploadGroupImage(g.getImmagine(),groupId,closureBool);
            }else{
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            }

        });
    }

    /** Return a list of all group on Firestore.
     *
     * @param closureList ClosureList of Gruppo type.
     */
    public static final void getAllGroups(ClosureList<Gruppo> closureList){
        FirestoreHelper.db.collection(GRUPPO_COLLECTION).get().addOnCompleteListener(task -> {
            if(closureList != null){
                if(task.isSuccessful()){
                    closureList.closure(convertResults(task,Gruppo.class));
                }else closureList.closure(null);
            }
        });
    }

    /** Upload the group image to the Firestore Storage. It is placed inside a directory named after the group id.
     * It is replaced if already present.
     * The image is placed inside the following path: Gruppi/\idGruppo\/immagineProfilo
     *
     * User must be logged-in.
     *
     * @param file file to upload
     * @param idGruppo  group whose image you want to change
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void uploadGroupImage(Uri file, String idGruppo, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = "Gruppi/" + idGruppo + "/immagineProfilo";
        StorageHelper.uploadImage(file, finalChildName, new ClosureBoolean() {
            @Override
            public void closure(boolean isSuccess) {
                if(!isSuccess){
                    if(closureBool != null) closureBool.closure(false);
                } else {
                    FirestoreHelper.db.collection(GRUPPO_COLLECTION).document(idGruppo).update("isImmagineUploaded", true).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(closureBool != null) closureBool.closure(task.isSuccessful());
                        }
                    });
                }
            }
        });
    }

    /** Download group image from the Firebase Firestore.
     *
     *  User must be logged-in.
     *
     * @param idGruppo  group whose image you want to download.
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadGroupImage(String idGruppo, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = GRUPPO_COLLECTION + "/" + idGruppo + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

}
