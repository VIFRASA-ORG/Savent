package Model.DB;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.Pojo.Evento;
import Model.Pojo.Utente;

public class Utenti extends ResultsConverter {

    private static final String UTENTI_COLLECTION = "Utenti";


    /**
     * Return the name and surname of the user with the specified id.
     * The return value is formatted as follow: name + " " + surname.
     *
     * @param idUtente id of the use whose name you want to know
     * @param closureRes  get called with the value if the task is successful, null otherwise.
     */
    public static final void getNameSurnameOfUser(String idUtente, ClosureResult<String> closureRes){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).document(idUtente).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Utente user = task.getResult().toObject(Utente.class);
                    String finalString = user.getNome() + " " + user.getCognome();
                    if (closureRes != null) closureRes.closure(finalString);
                }else{
                    if (closureRes != null) closureRes.closure(null);
                }
            });
        }
    }

    /** Return the user object with the specified phone number.
     *
     * @param phoneNumber   Phone number of the user to search for.
     * @param closureRes    get called with the Utente object if the task is successful, null otherwise.
     */
    public static final void searchUserByPhoneNumber(String phoneNumber, ClosureResult<Utente> closureRes){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).whereEqualTo("numeroDiTelefono",phoneNumber).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    List<Utente> l = convertResults(task,Utente.class);

                    if (l.size() == 1 ){
                        if(closureRes != null) closureRes.closure(l.get(0));
                    }else{
                        if(closureRes != null) closureRes.closure(null);
                    }
                }else{
                    if(closureRes != null) closureRes.closure(null);
                }
            });
        }
    }

    /** Return the user object with the specified phone number.
     *
     * @param phoneNumber   Phone number of the user to search for.
     * @param closureBool    get called with the Utente object if the task is successful, null otherwise.
     */
    public static final void isPhoneNumberAlreadyTaken(String phoneNumber, ClosureBoolean closureBool){
        FirestoreHelper.db.collection(UTENTI_COLLECTION).whereEqualTo("numeroDiTelefono",phoneNumber).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<Utente> l = convertResults(task,Utente.class);

                if (l.size() == 1 ){
                    if(closureBool != null) closureBool.closure(true);
                }else{
                    if(closureBool != null) closureBool.closure(false);
                }
            }else{
                if(closureBool != null) closureBool.closure(false);
            }
        });

    }

    /** Check if the given Utente id is a valid id.
     *
     * @param idUtente id ti check.
     * @param closureBool   get called with true if the id is a valid Utente id, false otherwise.
     */
    public static final void isValidUser(String idUtente, ClosureBoolean closureBool){
        FirestoreHelper.db.collection(UTENTI_COLLECTION).document(idUtente).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if (closureBool != null) closureBool.closure(task.getResult().toObject(Utente.class) != null);
            }else{
                if (closureBool != null) closureBool.closure(false);
            }
        });
    }

    /** Create a new account to the user.
     *
     * @param user  the Utente object to upload.
     * @param email the user email used to log-in
     * @param psw   the user psw used to log-in.
     * @param profileImageUri   The user's profile image.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void createNewUser(Utente user, String email, String psw, Uri profileImageUri, Context context, ClosureBoolean closureBool){
        //First thing first: create the new user as account
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }

            //If the account creation is successful, we need to login
            AuthHelper.singIn(email, psw, isSuccess -> {
                if(!isSuccess){
                    if(closureBool != null) closureBool.closure(false);
                    return;
                }

                updateUserInformation(user, profileImageUri, context, closureBool);
            });
        });
    }

    /** Upload all the user information into Firestore. The user must be logged-in.
     *
     * @param user the Utente object to upload.
     * @param profileImageUri   The profile image of the user.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void updateUserInformation(Utente user, Uri profileImageUri, Context context, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()).set(user).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if (profileImageUri != null) {
                        uploadUserImage(profileImageUri, context, isSuccess -> {
                            if (isSuccess){
                                FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()).update("isProfileImageUploaded",true).addOnCompleteListener(task1 -> {
                                    if(closureBool!= null) closureBool.closure(task1.isSuccessful());
                                });
                            } else if(closureBool != null) closureBool.closure(false);
                        });
                    }else{
                        if(closureBool != null) closureBool.closure(true);
                    }
                }else  {
                    if(closureBool != null) closureBool.closure(false);
                }
            });
        }
    }

    /** Upload the user image to the Firestore Storage. It is placed inside a directory named after the user id.
     * It is replaced if already present.
     * The image is placed inside the following path: Utenti/\idUtente\/immagineProfilo
     *
     * User must be logged-in.
     *
     * @param file file to upload.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void uploadUserImage(Uri file, Context context, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/"+AuthHelper.getUserId()+"/immagineProfilo";
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
