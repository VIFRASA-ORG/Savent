package Model.DAO;

import android.app.Activity;
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
import Helper.Firebase.FirestoreHelper;
import Helper.Firebase.StorageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureList;
import Model.Closures.ClosureResult;
import Model.POJO.Contact;
import Model.POJO.Utente;

/**
 * Classe DAO (Data Access Object) che fornisce tutti i metodi
 * per ritrovare informazioni o dati riguardanti gli Utenti
 * memorizzati su firestore.
 *
 * Molti valori di ritorno fanno uso appunto della relativa classe POJO Utente.
 *
 * Implementa la classe astratta ResulConverter per permettere una immediata conversione
 * dei result provenienti dai task di Firebase in oggetti di classe Utente.
 */
public class Utenti extends ResultsConverter {

    /**
     * NOMI DELLE COLLECTION SU FIREBASE
     */
    public static final String UTENTI_COLLECTION = "Utenti";
    private static final String MESSAGING_TOKEN_COLLECTION = "MessagingToken";

    /**
     * CONSTANTI CHE INDICANO I NOMI DEI CAMPI SU FIREBASE
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
     * Metodo che permette di sommare il valore passato come parametro allo status sanitario corrente dell'utente loggato.
     *
     * @param offset valore da sommare.
     * @param closureBoolean invocato con true se il task va a buon fine, false altrimenti.
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
     * Metodod che ritorna il Firebase messagin token associato con questo device
     * diretamente con le API di Firebase Messaging.
     *
     * @param closureRes invocato con true se il task va a buon fine, false altrimenti.
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
     * Metodo che crea un nuovo documento nella tabella "MessaginToken" con id l'id dell'utente appena registrato.
     * All'interno del nuovo documento viene impostato il token di notifiche relativo a questo device.
     *
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
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
     * Metodo che aggiorna il valore del Token delle notifiche relativo all'utente loggato.
     * Il token verra caricato nella tabella MessagingToken/idUtente/token
     *
     * Il token viene preso direttamente dalle API FirebaseNotification.
     *
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
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
     * Metodo che aggiorna il valore del Token delle notifiche relativo all'utente loggato.
     * Il token verra caricato nella tabella MessagingToken/idUtente/token
     *
     * @param newToken il nuovo token da caricare
     * @param closureBool invocato con true se il task va a buon fine, false altrimenti.
     */
    public static final void setMessagingToken(String newToken, @Nullable ClosureBoolean closureBool){
        if(AuthHelper.isLoggedIn()) {
            FirestoreHelper.db.collection(MESSAGING_TOKEN_COLLECTION).document(AuthHelper.getUserId()).update(TOKEN_FIELD, newToken).addOnCompleteListener(task -> {
                if (closureBool != null) closureBool.closure(task.isSuccessful());
            });
        }
    }

    /**
     * Metodo che controlla se il codice fiscale passato come parametro è stato gia utilizzato.
     *
     * @param fiscalCode codice fiscale da controllare.
     * @param closureBool invocato con true se il codice viene trovato, false altrimenti.
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
     * Metodo che aggiunge un lister sul documento Utente dell'utente loggato.
     *
     * @param activity il contesto dell'apllicazione o dell'activity.
     * @param closureResult invocato con il nuovo oggetto aggiornato tutte le volte che si verifica un aggiornamento, null altrimenti.
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
     * Metodo che permette di cercare una lista di contatti, generalmente presi dalla rubrica, all'interno
     * del database di Firebase per controllare quali di loro sono correttamente registrati all'applicazione.
     *
     * @param contactsList lista di contatti da ricercare su Firebase.
     * @param closureList invocato con una lista di utenti trovati.
     */
    public static final void searchContactsInPhoneBooks(List<Contact> contactsList, ClosureList<Utente> closureList){
        if(AuthHelper.isLoggedIn()){
            List<String> contactsPhone = new ArrayList<>();

            //Estrapolo il numero di telefono in quanto serve per la ricerca.
            for (Contact c : contactsList) contactsPhone.add(c.getNumber());

            //La query IN supporta una lista di massimo 10 elementi
            //Quindi bisogna dividere la lista di numeri di telefono in chuck di lunghezza massima 10
            if(contactsPhone.size() > 10){
                Collection<Task<?>> taskList = new ArrayList<Task<?>>();
                int numbersOfChucks = contactsPhone.size() / 10;

                //Aggiungo la query del singolo chuck alla lista di task
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

    /**
     * Metodo che permette di modificare uno o più campi dell'utente con id passato come parametro.
     *
     * @param userId id dell'utente di cui si vole cambiare i valori.
     * @param closureBool invocato con true se l'esecuzione va a buon fine, false altrimenti.
     * @param firstField il nome del primo campo da aggiornare
     * @param firstValue nuovo valore da inserire nel primo campo sopra citato.
     * @param otherFieldAndValues array di oggetti con altri campi e valori da sostituire.
     */
    public static final void updateFields(String userId,ClosureBoolean closureBool, String firstField, Object firstValue, Object... otherFieldAndValues ){
        FirestoreHelper.db.collection(UTENTI_COLLECTION).document(userId).update(firstField,firstValue,otherFieldAndValues).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(closureBool != null) closureBool.closure(task.isSuccessful());
            }
        });
    }

    /**
     * Metodo che restituisce l'oggetto dell'utente con l'id passato come parametro.
     *
     * @param idUtente id dell'utente da cercare.
     * @param closureRes invocato con l'oggetto dell'utene se trovato, null altrimenti.
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
     * Metodo che ritorna il nome e il cognome dell'utente con l'id passato come parametro.
     *
     * @param idUtente id dell'utente di cui si vuole avere nome e cognome.
     * @param closureRes invocato con una stringa cosi formattata: name + " " + surname.
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

    /**
     * Metodo che restituisce l'istanza dell'utente avente come numero
     * di telefono quello passato come parametro, se esiste.
     *
     * @param phoneNumber numero di telefono dell'utente da cercare.
     * @param closureRes invocato con l'oggetto dll'utente se viene trovato un match, null altrimenti.
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

    /**
     * Metodo che controlla se l'id dato come paramentro è effettivamente
     * l'id di un utente su Firestore presente nella tabella Utenti.
     *
     * @param idUtente id dell'utente da controllare.
     * @param closureBool invocato con true se l'id è valido, false altrimenti.
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

    /**
     * Metodo che crea un nuovo utente su Firebase.
     *
     * @param user oggetto Utente con tutte le informazioni dell'utente.
     * @param email email con cui l'utente effettua il login.
     * @param psw password di accesso.
     * @param profileImageUri uri dell'immagine del profilo.
     * @param closureBool  invocato con true se l'upload va a buon fine, false altrimenti.
     */
    public static final void createNewUser(Utente user, String email, String psw, Uri profileImageUri, ClosureBoolean closureBool){
        //Per prima cosa creamo l'account per l'autenticazione
        AuthHelper.createNewAccount(email, psw, result -> {
            if (result == null){
                if(closureBool != null) closureBool.closure(false);
                return;
            }
            //Se la creazione dell'account va a buon fine, effettuiamo il login.
            AuthHelper.singIn(email, psw, isSuccess -> {
                if(!isSuccess){
                    if(closureBool != null) closureBool.closure(false);
                    return;
                }

                //Carichiamo tutte le informazioni dell'utente su Firebase.
                updateUserInformation(user, profileImageUri, closureBool);
            });
        });
    }

    /**
     * Metodo che permette di caricare tutte le informazioni di un nuovo utente su Firestore.
     * L'utente deve essere loggato.
     * Effettua anche il caricamento dell'immagine del profilo su Firestore Storage.
     *
     * @param user oggetto Utente contenente tutte le informazioni.
     * @param profileImageUri uri dell'immagine del profilo.
     * @param closureBool  invocato con true se l'upload va a buon fine, false altrimenti.
     */
    public static final void updateUserInformation(Utente user, Uri profileImageUri, ClosureBoolean closureBool){
        if (AuthHelper.isLoggedIn()){
            FirestoreHelper.db.collection(UTENTI_COLLECTION).document(AuthHelper.getUserId()).set(user).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    if (profileImageUri != null) {
                        uploadUserImage(profileImageUri, isSuccess -> {
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

    /**
     * Metodo che permette di caricare l'immagine del profilo dell'utente loggato su Firestore Storage.
     * L'immagine viene salvata in una directory chiamata con lo stesso id dell'utente
     * e viene sostituita se gia esistente.
     *
     * L'immagine viene messa nella seguente direcotry: Utenti/\idUtente\/immagineProfilo
     *
     * @param file uri dell'immagine da caricare.
     * @param closureBool invocato con true se l'upload va a buon fine, false altrimenti.
     */
    public static final void uploadUserImage(Uri file, ClosureBoolean closureBool){
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

    /**
     * Metodo che permette di scaricare l'immagine di profilo dell'utente loggato da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param closureBitmap invocato con una bitmap e se il download va a buon fine, null altrimenti.
     */
    public static final void downloadUserImage(ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

    /**
     * Metodo che permette di scaricare l'immagine di profilo dell'utente con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param userId id dell'utente di cui si vuole scaricare l'immagine del profilo
     * @param closureBitmap invocato con una bitmap e se il download va a buon fine, null altrimenti.
     */
    public static final void downloadUserImage(String userId, ClosureBitmap closureBitmap){
        if (!AuthHelper.isLoggedIn()){
            if (closureBitmap != null) closureBitmap.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + userId + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureBitmap);
    }

    /**
     * Metodo che permette di scaricare l'immagine di profilo dell'utente loggato da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param closureResult invocato con un temp file nella chache se il download va a buon fine, null altrimenti.
     */
    public static final void downloadUserImage(ClosureResult<File> closureResult){
        if (!AuthHelper.isLoggedIn()){
            if (closureResult != null) closureResult.closure(null);
            return;
        }

        String finalChildName = UTENTI_COLLECTION + "/" + AuthHelper.getUserId() + "/immagineProfilo";
        StorageHelper.downloadImage(finalChildName,closureResult);
    }

    /**
     * Metodo che permette di scaricare l'immagine di profilo dell'utente con
     * l'id passato come parametro da Firebase Storage.
     *
     * L'immagine deve esistere altrimenti non è garantito il corretto funzionamento.
     *
     * @param userId id dell'utente di cui si vuole scaricare l'immagine del profilo
     * @param closureResult invocato con un temp file nella chache se il download va a buon fine, null altrimenti.
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
