package Model.DB;

import android.net.Uri;
import android.net.wifi.aware.DiscoverySession;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import Helper.AuthHelper;
import Helper.FirestoreHelper;
import Helper.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.Pojo.Ente;
import Model.Pojo.Utente;

public class Enti {

    private static final String ENTI_COLLECTION = "Enti";


    /** Check if the Ente id given is a valid id.
     *
     * @param idEnte id to check
     * @param closureBool   get called with true if the id is a valid Ente id, false otherwise.
     */
    public static final void isValidEnte(String idEnte, ClosureBoolean closureBool){
        FirestoreHelper.db.collection(ENTI_COLLECTION).document(idEnte).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if (closureBool != null) closureBool.closure(task.getResult().toObject(Ente.class) != null);
            }else{
                if (closureBool != null) closureBool.closure(false);
            }
        });
    }

    /** Create a new account for the Libero professionista.
     *
     * @param ente Ente object with all the informations.
     * @param email email used to log-in.
     * @param psw   psw used to log-in.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void createNewEnteLiberoProfessionista(Ente ente, String email, String psw, ClosureBoolean closureBool){
        //First thing first: create the new user as account
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }

            //If the account creation is successful, we need to login
            AuthHelper.singIn(email, psw, new ClosureBoolean() {
                @Override
                public void closure(boolean isSuccess) {
                    if(!isSuccess){
                        if(closureBool != null) closureBool.closure(false);
                        return;
                    }

                    updateLiberoProfessionistaInformation(ente, closureBool);
                }
            });
        });
    }

    /** Upload all the Libero professionista informations into Firestore. The user must be logged-in.
     *
     * @param ente Ente object with all the informations.
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void updateLiberoProfessionistaInformation(Ente ente, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).set(ente).addOnCompleteListener(task -> {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /** Create a new account for the Ditta.
     *
     * @param ente  Ente object with all the informations.
     * @param email Email used to log-in.
     * @param psw   Psw used to log-in
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    public static final void createNewEnteDitta(Ente ente, String email, String psw, ClosureBoolean closureBool){
        //First thing first: create the new user as account
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }

            //If the account creation is successful, we need to login
            AuthHelper.singIn(email, psw, new ClosureBoolean() {
                @Override
                public void closure(boolean isSuccess) {
                    if(!isSuccess){
                        if(closureBool != null) closureBool.closure(false);
                        return;
                    }

                    updateDittaInformation(ente, closureBool);
                }
            });
        });
    }

    /** Upload all the Ditta informations into Firestore. The user must be logged-in.
     *
     * @param ente the ente object with all the ditta informations.
     * @param closureBool get called with true if the task is successful, false otherwise.
     */
    public static final void updateDittaInformation(Ente ente, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).set(ente).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if (ente.getCertificatoPIVA() != null) {

                        //Upload the Partita iva certificate
                        uploadCertificatoPIVA(ente.getCertificatoPIVA(), isSuccess -> {

                            if(isSuccess == false){
                                if(closureBool != null) closureBool.closure(false);
                            }else{
                                if (ente.getVisuraCamerale() != null) {
                                    //upload the visura camerale
                                    uploadVisuraCamerale(ente.getVisuraCamerale(), isSuccess1 -> {
                                        if (closureBool != null) closureBool.closure(isSuccess1);
                                    });
                                }else{
                                    if(closureBool != null) closureBool.closure(true);
                                }
                            }
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

    /** Upload the visura camerale to the Firestore Storage.
     * It is replaced if already present.
     * The image is placed inside the following path: Enti/\idEnte\/visuraCamerale
     *
     * @param visuraCameraleUri file to upload.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    private static final void uploadVisuraCamerale(Uri visuraCameraleUri, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/"+AuthHelper.getUserId()+"/visuraCamerale";
        StorageHelper.uploadImage(visuraCameraleUri,finalChildName,closureBool);
    }

    /** Upload the PIva certificate to the Firestore Storage.
     * It is replaced if already present.
     * The image is placed inside the following path: Enti/\idEnte\/certificatoPIVA
     *
     * @param certificatoPIVAUri    file to upload.
     * @param closureBool   get called with true if the task is successful, false otherwise.
     */
    private static final void uploadCertificatoPIVA(Uri certificatoPIVAUri, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/"+AuthHelper.getUserId()+"/certificatoPIVA";
        StorageHelper.uploadImage(certificatoPIVAUri,finalChildName,closureBool);
    }

    /** Download the Visura camerale from the Firestore Storage.
     *
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadVisuraCamerale(ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/visuraCamerale";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

    /** Download the PIVA certificate from the Firestore Storage.
     *
     * @param closureBitmap get called with the Bitmap if the task is successful, null otherwise.
     */
    public static final void downloadCertificatoPIVA(ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/certificatoPIVA";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

}
