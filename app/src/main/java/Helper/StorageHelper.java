package Helper;

import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;

public class StorageHelper {

    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final StorageReference storageRef = storage.getReference();


    /** Upload a generic image on Firestore Storage. The path is specified as a parameter.
     *
     * User must be logged-in.
     *
     * @param file file to upload.
     * @param child path to save the image.
     * @param closureBool    get called with true if the task is successful, false otherwise.
     */
    public static final void uploadImage(Uri file, String child, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        StorageReference riversRef = storageRef.child(child);
        UploadTask uploadTask = riversRef.putFile(file);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Download a generic image from Firebase Storage. The path is specified as a parameter.
     *
     * The user must be logged-in.
     *
     * @param child path from which to get the image.
     * @param closureBitmap  get called with true if the task is successful, false otherwise.
     */
    public static final void downloadImage(String child, ClosureBitmap closureBitmap){
        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(child);

        pathReference.getBytes(1024*1024).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (closureBitmap != null){
                    if (task.isSuccessful()){
                        closureBitmap.closure(BitmapFactory.decodeByteArray(task.getResult(),0,task.getResult().length));
                    }else closureBitmap.closure(null);
                }

            }
        });
    }
}
