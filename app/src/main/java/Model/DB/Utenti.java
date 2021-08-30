package Model.DB;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.FirebaseStorage.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.Contact;
import Model.Pojo.Utente;



public class Utenti extends ResultsConverter {

    public static final String UTENTI_COLLECTION = "Utenti";
    private static final String MESSAGING_TOKEN_COLLECTION = "MessagingToken";

    /**
     * Used to update the information of the user into the updateField method.
     */
    public static final String NOME_FIELD = "nome";
    public static final String COGNOME_FIELD = "cognome";
    public static final String DATA_NASCITA_FIELD = "dataNascita";
    public static final String NUMERO_TELEFONO_FIELD = "numeroDiTelefono";
    public static final String GENERE_FIELD = "genere";
    public static final String STATUS_SANITARIO_FIELD = "statusSanitario";
    public static final String IS_PROFILE_IMAGE_FIELD = "isProfileImageUploaded";
    public static final String CODICE_FISCALE_FIELD = "codiceFiscale";

    public static final String TOKEN_FIELD = "token";


    /**
     * Method that sum the given offset to the logged in user health status.
     * If the sum is > 100, the health status is setted to 100.
     *
     * @param offset the value to sum to the logged in user health status.
     * @param closureBoolean invoked with true if the task is successful, false otherwise.
     */
    public static final void sumValueToHealthStatus(int offset, ClosureBoolean closureBoolean){
        if(AuthHelper.isLoggedIn()) {
            FirestoreHelper.db.runTransaction(transaction -> {
                DocumentSnapshot document = transaction.get(FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()));
                Utente utente = document.toObject(Utente.class);

                int actualHealt = utente.getStatusSanitario();
                int newHealthValue = (actualHealt + offset < 100) ? actualHealt + offset : 100;

                transaction.update(FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()), "statusSanitario", newHealthValue);

                return null;
            }).addOnCompleteListener( task -> {
                if(closureBoolean != null) closureBoolean.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Method used to get the current firebase messaging token associated to the device.
     *
     * @param closureRes invoked with the token string if the task is successful, null otherwise
     */
    private static final void getCurrentToken(@Nullable ClosureResult<String> closureRes){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w("MESSAGING", "Fetching FCM registration token failed", task.getException());
                if(closureRes != null) closureRes.closure(null);
            }

            // Get new FCM registration token
            String token = task.getResult();

            // Log and toast
            if(closureRes != null) closureRes.closure(token);
        });
    }

    /**
     * Create a new document into "MessagingToken" collection associated to the logged in user.
     * The new record contains the logged in user id and the messaging token associated to the device.
     *
     * @param closureBool invoked with true if the task is successful, false otherwise.
     */
    public static final void createMessagingTokenDocument( @Nullable ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()) {

            getCurrentToken( token -> {
                if (token == null) {
                    if (closureBool != null) closureBool.closure(false);
                    return;
                }

                Map<String, String> data = new HashMap<>();
                data.put(TOKEN_FIELD, token);
                FirestoreHelper.db.collection(MESSAGING_TOKEN_COLLECTION).document(AuthHelper.getUserId()).set(data).addOnCompleteListener( task -> {
                    if(closureBool != null) closureBool.closure((task.isSuccessful()));
                });
            });
        }
    }

    /**
     * Send to the server the new messaging token
     * The new token is associated to the logged in user and is taken directly from the FirebaseMessaging API.
     *
     * @param closureBool invoked with true if the task is successful, false otherwise.
     */
    public static final void setMessagingToken(@Nullable ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()) {
            getCurrentToken( token -> {
                FirestoreHelper.db.collection(MESSAGING_TOKEN_COLLECTION).document(AuthHelper.getUserId()).update(TOKEN_FIELD, token).addOnCompleteListener(task -> {
                    if (closureBool != null) closureBool.closure(task.isSuccessful());
                });
            });
        }
    }

    /**
     * Send to the server the new messaging token
     * The new token is associated to the logged in user
     *
     * @param newToken the new token to communicate to the server
     * @param closureBool invoked with true if the task is successful, false otherwise.
     */
    public static final void setMessagingToken(String newToken, @Nullable ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()) {
            FirestoreHelper.db.collection(MESSAGING_TOKEN_COLLECTION).document(AuthHelper.getUserId()).update(TOKEN_FIELD, newToken).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Check if the given fiscal code is already used by another account
     *
     * @param fiscalCode fiscal code of the user to search for.
     * @param closureBool invoked with true if the fiscal code is already used, false otherwise.
     */
    public static final void isFiscalCodeAlreadyUsed(String fiscalCode, @Nullable ClosureBoolean closureBool){
        FirestoreHelper.db.collection(UTENTI_COLLECTION).whereEqualTo(CODICE_FISCALE_FIELD,fiscalCode.toUpperCase()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<Utente> l = convertResults(task,Utente.class);

                if(closureBool != null) closureBool.closure(l.size() > 0);
            }else{
                if(closureBool != null) closureBool.closure(true);
            }
        });
    }

    /**
     * Add a listener to all the updates from the server to the logged user.
     *
     * @param activity the context of the owner activity
     * @param closureResult user that will be invoked every time an update is found. it will give the user object if found, null otherwise.
     */
    public static final void addDocumentListener( Activity activity, ClosureResult<Utente> closureResult){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()).addSnapshotListener(activity, (value, error) -> {
                if (error != null) {
                    if(closureResult != null) closureResult.closure(null);
                }

                if (value != null && value.exists()) {
                    if(closureResult != null) closureResult.closure(value.toObject(Utente.class));
                } else {
                    if(closureResult != null) closureResult.closure(null);
                }
            });
        }

    }

    /**
     * Search a list of contacts taken, from the phone contact list, among all the users on the firestore.
     *
     * @param contactsList the list of contact to search on the database
     * @param closureList the list of users found.
     */
    public static final void searchContactsInPhoneBooks(List<Contact> contactsList, ClosureList<Utente> closureList){
        if(AuthHelper.isLoggedIn()){
            List<String> contactsPhone = new ArrayList<>();

            //Exrapolating the phone number, the search is based on that.
            for (Contact c : contactsList) contactsPhone.add(c.getNumber());

            //The in query support only a list of maximum 10 elements
            //Dividing the list in chucks of length 10
            if(contactsPhone.size() > 10){
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();
                int numbersOfChucks = contactsPhone.size() / 10;

                //Adding the chunked query to the list
                for(int i=0; i <= numbersOfChucks; i++){
                    Task t;
                    if(i == numbersOfChucks) t = FirestoreHelper.db.collection(UTENTI_COLLECTION).whereIn(NUMERO_TELEFONO_FIELD,contactsPhone.subList(10*i,contactsPhone.size())).get();
                    else t = FirestoreHelper.db.collection(UTENTI_COLLECTION).whereIn(NUMERO_TELEFONO_FIELD,contactsPhone.subList(10*i,10*i+10)).get();
                    taskList.add(t);
                }

                Task combinedTask = Tasks.whenAllComplete(taskList).addOnCompleteListener(task -> {
                    if(closureList != null){
                        if(task.isSuccessful()){
                            List<Utente> finalList = new ArrayList<>();
                            for(Task<?> t : task.getResult()){
                                finalList.addAll(convertResults((Task<QuerySnapshot>) t,Utente.class));
                            }
                            closureList.closure(finalList);
                        }else closureList.closure(null);
                    }
                });
            }else{
                FirestoreHelper.db.collection(UTENTI_COLLECTION).whereIn(NUMERO_TELEFONO_FIELD,contactsPhone).get().addOnCompleteListener( task -> {
                    if(!task.isSuccessful()){
                        if(closureList != null) closureList.closure(null);
                        return;
                    }

                    if(closureList != null) closureList.closure(convertResults(task,Utente.class));
                });
            }
        }

    }

    /** Update the information of the user.
     *
     * @param userId the id of the user
     * @param closureBool get called with true if the task is successful, false otherwise.
     * @param firstField the name of the first field to update
     * @param firstValue tha new value of the first field
     * @param otherFieldAndValues an array of object with other field and values.
     */
    public static final void updateFields(String userId,ClosureBoolean closureBool, String firstField, Object firstValue, Object... otherFieldAndValues ){
        FirestoreHelper.db.collection(UTENTI_COLLECTION).document(userId).update(firstField,firstValue,otherFieldAndValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /** Return the Utente object with the given id
     *
     * @param idUtente id of the user
     * @param closureRes get called with the object if the task is successful, null otherwise.
     */
    public static final void getUser(String idUtente, ClosureResult<Utente> closureRes){
        if(AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).document(idUtente).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if (closureRes != null) closureRes.closure(task.getResult().toObject(Utente.class));
                }else{
                    if (closureRes != null) closureRes.closure(null);
                }
            });
        }
    }

    /**
     * Return the name and surname of the user with the specified id.
     * The return value is formatted as follow: name + " " + surname.
     *
     * @param idUtente id of the user whose name you want to know
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
        StorageHelper.uploadImage(file,finalChildName,closureBoolean -> {
            if (closureBoolean){
                FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()).update("isProfileImageUploaded",true).addOnCompleteListener(task1 -> {
                    if(closureBool!= null) closureBool.closure(task1.isSuccessful());
                });
            } else if(closureBool != null) closureBool.closure(false);
        });
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

    public static final void downloadUserImage(String userId, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + userId + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }
  
    /** Download the user image from Firebase Storage.
     *
     * User must be logged-in.
     *
     * @param closureResult get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadUserImage(ClosureResult<File> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureResult);
    }

    /** Download the user image from Firebase Storage.
     *
     * User must be logged-in.
     *
     * @param closureResult get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadUserImage(String userId,ClosureResult<File> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + userId + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureResult);
    }
}
