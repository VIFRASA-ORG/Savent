package Helper.Firebase;

import android.graphics.BitmapFactory;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.IOException;
import Helper.AuthHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;

/**
 * Classe Helper contente metodi di più alto livello
 * per caricare e scaricare immagini su e da Firebase Storage.
 */
public class StorageHelper {

    /**
     * COSTANTI STATICHE CONTENENTI I RIFERIMENTI AI SERVICE DI FIREBASE STORAGE.
     */
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();
    private static final StorageReference storageRef = storage.getReference();


    /**
     * Metodo che permette di effetuare l'upload di una immagine su Firebase Storage.
     * Il path dell'immagine da caricare viene dato come parametro.
     *
     * @param file uri del file da caricare.
     * @param child path di destinazione del file su Firebase Storage.
     * @param closureBool invocato con true se l'upload viene fatto con successo, false altrimenti.
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

    /**
     * Metodo che permette di scaricare una immagine generica da Firebase Storage.
     * Il path dell'immagine viene dato come parametro.
     * L'imagine verrà restituita come bitmap.
     *
     * L'utente deve essere loggato.
     *
     * @param child path dove si trova l'immagine.
     * @param closureBitmap invocato con la bitmap dell'immagine trovata, null altrimenti.
     */
    public static final void downloadImage(String child, ClosureBitmap closureBitmap){
        //Creazione di un riferimento al file con il path passato come parametro
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

    /**
     * Metodo che permette di scaricare una immagine generica da Firebase Storage.
     * Il path dell'immagine viene dato come parametro.
     * L'imagine verrà restituita come File memorizzato in maniera temporanea nella cache.
     *
     * L'utente deve essere loggato.
     *
     * @param child path dove si trova l'immagine.
     * @param closureResult invocato con il file dell'immagine trovata, null altrimenti.
     */
    public static final void downloadImage(String child, ClosureResult<File> closureResult){
        //Creazione di un riferimento al file con il path passato come parametro
        StorageReference pathReference = storageRef.child(child);

        try {
            File localFile = File.createTempFile("images", "");

            pathReference.getFile(localFile).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if(closureResult != null) closureResult.closure(localFile);
                }else{
                    if(closureResult != null) closureResult.closure(null);
                }
            });
        } catch (IOException e) {
            if(closureResult != null) closureResult.closure(null);
        }
    }

    /**
     * Metodo che permette di trovare l'Uri di una immagine generica da Firebase Storage.
     * Il path dell'immagine viene dato come parametro.
     *
     * L'utente deve essere loggato.
     *
     * @param child path dove si trova l'immagine.
     * @param closureResult invocato con l'Uri dell'immagine trovata, null altrimenti.
     */
    public static final void downloadImageUri(String child, ClosureResult<Uri> closureResult){
        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(child);

        pathReference.getDownloadUrl().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if(closureResult != null) closureResult.closure(task.getResult());
            }else{
                if(closureResult != null) closureResult.closure(null);
            }
        });
    }
}
