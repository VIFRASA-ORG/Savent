package Model.DB;

import android.net.Uri;

import com.google.firebase.firestore.FirebaseFirestore;

import Helper.AuthHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;

public class Utenti {

    private static final String UTENTI_COLLECTION = "Utenti";

    /** Upload the user image to the Firestore Storage. It is placed inside a directory named after the user id.
     * It is replaced if already present.
     * The image is placed inside the following path: Utenti/\idUtente\/immagineProfilo
     *
     * User must be logged-in.
     *
     * @param file file to upload.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void uploadUserImage(Uri file, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = "Utenti/"+AuthHelper.getUserId()+"/immagineProfilo";
        StorageHelper.uploadImage(file,finalChildName,closureBool);
    }

    /** Download the user image from Firebase Storage.
     *
     * User must be logged-in.
     *
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadUserImage(ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }
}
