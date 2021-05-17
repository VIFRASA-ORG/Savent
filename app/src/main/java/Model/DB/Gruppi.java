package Model.DB;

import android.net.Uri;

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
import Model.Pojo.Evento;
import Model.Pojo.Gruppo;

public class Gruppi {

    private static final String GRUPPO_COLLECTION = "Gruppi";





    /** Add a new group to Firestore. The idGroup is randomly picked and the id inside the pojo object is avoided.
     *
     * @param g group to add to Firestore
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void addNewGroup(Gruppo g, ClosureBoolean closureBool){

        FirestoreHelper.db.collection(GRUPPO_COLLECTION).add(g).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Return a list of all group on Firestore.
     *
     * @param closureList ClosureList of Gruppo type.
     */
    public static final void getAllGroups(ClosureList<Gruppo> closureList){
        FirestoreHelper.db.collection(GRUPPO_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful() && closureList != null){
                    List<Gruppo> groupList = new ArrayList<>();

                    for(QueryDocumentSnapshot document : task.getResult()){
                        Gruppo g = document.toObject(Gruppo.class);
                        groupList.add(g);
                    }
                    closureList.closure(groupList);
                }
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
        StorageHelper.uploadImage(file,finalChildName,closureBool);
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
