package Model.DAO;

import android.net.Uri;

import Helper.AuthHelper;
import Helper.FirebaseStorage.FirestoreHelper;
import Helper.FirebaseStorage.StorageHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.POJO.Ente;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti gli Enti
 * memorizzati su firestore.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO Ente.
 *
 */
public class Enti {

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    private static final String ENTI_COLLECTION = "Enti";


    /**
     * Metodo che controlla che l'ente con l'id passato come parametro è stato abilitato
     * all'utilizzo dell'applicazione.
     * L'abilitazione viene fatta manualmente da dei controllori, dopo aver controllato la
     * veridicità delle informazioni.
     *
     * @param enteId id dell'ente da controllare.
     * @param closureBool invocato con true se l'id è abilitato, false altrimenti.
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

    /**
     * Metodo che controlla se l'id dato come paramentro è effettivamente
     * l'id di un ente su Firestore presente nella tabella Enti.
     *
     * @param idEnte id dell'Ente da controllare.
     * @param closureBool invocato con true se l'id è valido, false altrimenti.
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

    /**
     * Metodo che permette di creare un nuovo account Ente - libero professionista.
     * Crea un nuovo oggetto Ente nella tabella Ente su Firebase.
     *
     * @param ente oggetto Ente contenente tutte le informazioni del libero professionista.
     * @param email email utilizzata per il login nella fase di registrazione.
     * @param psw password per eseguire l'accesso.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void createNewEnteLiberoProfessionista(Ente ente, String email, String psw, ClosureBoolean closureBool){
        //Per prima cosa creamo l'account per l'autenticazione
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }

            //Se la creazione dell'account va a buon fine, effettuiamo il login.
            AuthHelper.singIn(email, psw, new ClosureBoolean() {
                @Override
                public void closure(boolean isSuccess) {
                    if(!isSuccess){
                        if(closureBool != null) closureBool.closure(false);
                        return;
                    }

                    //Carichiamo tutte le informazioni dell'utente su Firebase.
                    updateLiberoProfessionistaInformation(ente, closureBool);
                }
            });
        });
    }

    /**
     * Metodo che carica tutte le informazioni dell libero professionista su Firebase. L'Ente deve essere loggato.
     * L'oggetto Ente deve essere correttamente formattato come un libero professionista.
     *
     * @param ente l'oggetto dell'Ente da caricare con tutte le informazioni del libero professionista.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void updateLiberoProfessionistaInformation(Ente ente, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).set(ente).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    if (ente.getCertificatoPIVA() != null) {

                        //Upload del certivicato di partita iva.
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

    /**
     * Metodo che permette di creare un nuovo account Ente - Ditta.
     * Crea un nuovo oggetto Ente nella tabella Ente su Firebase.
     *
     * @param ente oggetto Ente contenente tutte le informazioni della Ditta.
     * @param email email utilizzata per il login nella fase di registrazione.
     * @param psw password per eseguire l'accesso.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void createNewEnteDitta(Ente ente, String email, String psw, ClosureBoolean closureBool){
        //Per prima cosa creamo l'account per l'autenticazione
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }

            //Se la creazione dell'account va a buon fine, effettuiamo il login.
            AuthHelper.singIn(email, psw, new ClosureBoolean() {
                @Override
                public void closure(boolean isSuccess) {
                    if(!isSuccess){
                        if(closureBool != null) closureBool.closure(false);
                        return;
                    }

                    //Carichiamo tutte le informazioni dell'utente su Firebase.
                    updateDittaInformation(ente, closureBool);
                }
            });
        });
    }

    /**
     * Metodo che carica tutte le informazioni della ditta su Firebase. L'Ente deve essere loggato.
     * L'oggetto Ente deve essere correttamente formattato come una Ditta.
     *
     * @param ente l'oggetto dell'Ente da caricare con tutte le informazioni della ditta.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void updateDittaInformation(Ente ente, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).set(ente).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if (ente.getCertificatoPIVA() != null) {

                        //Upload del certificato della partita iva
                        uploadCertificatoPIVA(ente.getCertificatoPIVA(), isSuccess -> {

                            if(isSuccess){
                                //Imposto il flag di upload della partita iva a true.
                                FirestoreHelper.db.collection(ENTI_COLLECTION).document(AuthHelper.getUserId()).update("isCertificatoPIVAUploaded",true).addOnCompleteListener(task1 -> {
                                    if(task1.isSuccessful()){

                                        //upload della visura camerale
                                        uploadVisuraCamerale(ente.getVisuraCamerale(), isSuccess1 -> {

                                            //Imposto il flag di upload della visura camerale a true.
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

    /**
     * Metodo che carica sul Firestore Storage il la visura camerale dell'ente loggato.
     * Il certificato viene rimpiazzato se gia esistente.
     * Viene messo all'interno del seguente path: Enti/\idEnte\/visuraCamerale.
     *
     * @param visuraCameraleUri file da caricare.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    private static final void uploadVisuraCamerale(Uri visuraCameraleUri, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/"+AuthHelper.getUserId()+"/visuraCamerale";
        StorageHelper.uploadImage(visuraCameraleUri,finalChildName,closureBool);
    }

    /**
     * Metodo che carica sul Firestore Storage il certificato di Partita iva dell'ente loggato.
     * Il certificato viene rimpiazzato se gia esistente.
     * Viene messo all'interno del seguente path: Enti/\idEnte\/certificatoPIVA.
     *
     * @param certificatoPIVAUri file da caricare.
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    private static final void uploadCertificatoPIVA(Uri certificatoPIVAUri, ClosureBoolean closureBool){
        if (!AuthHelper.isLoggedIn()){
            if (closureBool != null) closureBool.closure(false);
            return;
        }

        String finalChildName = ENTI_COLLECTION + "/"+AuthHelper.getUserId()+"/certificatoPIVA";
        StorageHelper.uploadImage(certificatoPIVAUri,finalChildName,closureBool);
    }

    /**
     * Metodo che restituisce l'ente corrispondente all'id passato come parametro.
     *
     * @param idEnte id dell'ente da ricercare
     * @param closureResult invocata con l'oggetto dell'ente se trovato, altrimenti ritorna null
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
