package Model.DB;

import android.net.Uri;

import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.FirebaseStorage.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.Pojo.Ente;

import static Model.DB.ResultsConverter.convertResults;

public class Enti {

    private static final String ENTI_COLLECTION = "Enti";


    /** Check if the ente with the given id is enabled from the provider.
     *
     * @param enteId    id of the Ente you want to check
     * @param closureBool   get called with true if the ente is enabled, false otherwise.
     */
    public static final void isEnteEnabled(String enteId, ClosureBoolean closureBool){
        FirestoreHelper.db.collection(ENTI_COLLECTION).document(enteId).get().addOnCompleteListener(task ->{
            if(task.isSuccessful()){
                Ente e = task.getResult().toObject(Ente.class);
                if(e != null){
                    if (closureBool != null) closureBool.closure(e.getAbilitazione());
                }else{
                    if (closureBool != null) closureBool.closure(false);
                }
            }else{
                if (closureBool != null) closureBool.closure(false);
            }
        });
    }

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
                //if(closureBool != null) closureBool.closure(task.isSuccessful());
                if(task.isSuccessful()){
                    if (ente.getCertificatoPIVA() != null) {

                        //Upload the Partita iva certificate
                        uploadCertificatoPIVA(ente.getCertificatoPIVA(), isSuccess -> {
                            if(isSuccess){
                                FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).update("isCertificatoPIVAUploaded",true).addOnCompleteListener(task1 -> {
                                    if(closureBool!= null) closureBool.closure(task1.isSuccessful());
                                });
                            }else{
                                if(closureBool != null) closureBool.closure(false);
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

                            //Set the flag to true
                            if(isSuccess){
                                FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).update("isCertificatoPIVAUploaded",true).addOnCompleteListener(task1 -> {
                                    //if(closureBool!= null) closureBool.closure(task1.isSuccessful());
                                    if(task1.isSuccessful()){

                                        //upload the visura camerale
                                        uploadVisuraCamerale(ente.getVisuraCamerale(), isSuccess1 -> {

                                            //Set the flag to true
                                            if(isSuccess1){
                                                FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).update("isVisuraCameraleUploaded",true).addOnCompleteListener(task2 -> {
                                                    if(closureBool!= null) closureBool.closure(task2.isSuccessful());
                                                });
                                            }else{
                                                if(closureBool != null) closureBool.closure(false);
                                            }
                                        });

                                    }else{
                                        if(closureBool != null) closureBool.closure(false);
                                    }
                                });
                            }else{
                                if(closureBool != null) closureBool.closure(false);
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


    /** Return a list of all enti on Firestore.
     *
     * @param closureList ClosureList of enti type.
     */
    public static final void getAllEnti(ClosureList<Ente> closureList){
        FirestoreHelper.db.collection(ENTI_COLLECTION).get().addOnCompleteListener(task -> {
            if(closureList != null){
                if(task.isSuccessful()){
                    closureList.closure(convertResults(task,Ente.class));
                }else closureList.closure(null);
            }
        });
    }


    /**
     * Ritorna l'ente corrispondente all'id inserito
     * @param idEnte id dell'ente da ricercare
     * @param closureResult invocata quando il task è stato eseguito con successo per convertire nell'oggetto Ente, altrimenti ritorna null
     */
    public static final void getEnte(String idEnte, ClosureResult<Ente> closureResult ){
        FirestoreHelper.db.collection(ENTI_COLLECTION).document(idEnte).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                if (closureResult != null) closureResult.closure(task.getResult().toObject(Ente.class));
            }else{
                if (closureResult != null) closureResult.closure(null);
            }
        });
    }




}
