package Helper;

import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import Helper.LocalStorage.SQLiteHelper;
import Helper.LocalStorage.SharedPreferencesHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.DAO.Enti;
import Model.DAO.Utenti;
import Services.BluetoothLEServices.GattServerCrawlerService;
import Services.BluetoothLEServices.GattServerService;
import Services.DailyJob.DailyJobReceiver;


/**
 * Classe Helper con metodi a supporto dell'autenticazione
 * degli utenti su Firebase utilizzando FirebaseAuth.
 */
public class AuthHelper {

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    /**
     * Enum che definisce tutti i tipi di utenti che potrebbero
     * fare il login all'interno dell'applicazione.
     */
    public enum UserType{
        Utente,
        Ente,
        None
    }


    /**
     * Ritorna l'email dell'utente loggato.
     *
     * @return l'email dell'utente loggato se esiste, null altrimenti.
     */
    public static final String getUserLoggedEmail(){
        if(!isLoggedIn()) return null;

        return mAuth.getCurrentUser().getEmail();
    }

    /**
     * Ritorna il tipo di utente loggato.
     *
     * @param closureRes closure invocata con il tipo di utente loggato.
     */
    public static final void getLoggedUserType(ClosureResult<UserType> closureRes){
        if(!isLoggedIn()){
            if(closureRes != null) closureRes.closure(UserType.None);
            return;
        }

        //Controllo che l'id dell'utente loggato è un Utente.
        Utenti.isValidUser(getUserId(), isSuccess -> {
           if(isSuccess){
               if(closureRes != null) closureRes.closure(UserType.Utente);
           }else{

               //Controllo se è un ente.
               Enti.isValidEnte(getUserId(), isSuccess1 -> {
                   if(isSuccess1){
                       if(closureRes != null) closureRes.closure(UserType.Ente);
                   }else{
                       if(closureRes != null) closureRes.closure(UserType.None);
                   }
               });
           }
        });
    }

    /**
     * Controlla se è presente un utente loggato.
     *
     * @return true se è loggato un utente, false altrimenti.
     */
    public static final boolean isLoggedIn(){
        FirebaseUser cU = mAuth.getCurrentUser();
        return cU != null;
    }

    /**
     * Ritorna l'id dell'utente loggato.
     *
     * @return id dell'utente loggato se presente, null altrimenti.
     */
    public static final String getUserId(){
        return isLoggedIn() ? mAuth.getCurrentUser().getUid() : null;
    }

    /**
     * Metodo che permette di creare un nuovo utente all'interno di FirebaseAuth.
     *
     * @param email email del nuovo utente.
     * @param psw psw del nuovo utente.
     * @param closureResult invocato con l'id dell'utente creato, null se si è verificato un errore.
     */
    public static final void createNewAccount(String email, String psw, ClosureResult<String> closureResult){
        mAuth.createUserWithEmailAndPassword(email,psw).addOnCompleteListener(task -> {
            if (closureResult == null) return;

            if (task.isSuccessful()) closureResult.closure(task.getResult().getUser().getUid());
            else closureResult.closure(null);
        });
    }

    /**
     * Metodo che permette di effettuare il login tramite FirebaseAuth.
     *
     * @param email email dell'utente che vuole effettuare il login.
     * @param psw password dell'utente che vuole effetture il login.
     * @param closureBool invocato con true nel caso in cui l'operazione si conclude con successo, false altrimenti.
     */
    public static final void singIn(String email,String psw,ClosureBoolean closureBool){
        mAuth.signInWithEmailAndPassword(email,psw).addOnCompleteListener(task -> {
            if (closureBool == null) return;

            if (task.isSuccessful()) closureBool.closure(true);
            else closureBool.closure(false);
        });
    }

    /**
     * Metodo che permette di cambiare la password dell'utente logato.
     * IMPORANTE: Richiede che ci sia stata un operazione di login o di reautenticazione recentemente.
     *
     * @param newPsw nuova password
     * @param closureBool invocato con true se l'operazione è andata a buon fine, false altrimenti.
     */
    public static final void updatePsw(String newPsw, ClosureBoolean closureBool){
        if(isLoggedIn()){
            mAuth.getCurrentUser().updatePassword(newPsw).addOnCompleteListener(task -> {
                if (closureBool == null) return;

                if (task.isSuccessful()) closureBool.closure(true);
                else closureBool.closure(false);
            });
        }
    }

    /**
     * Metodo che permette di cambiare l'email dell'utente logato.
     * IMPORANTE: Richiede che ci sia stata un operazione di login o di reautenticazione recentemente.
     *
     * @param newEmail nuova email
     * @param closureBool invocato con true se l'operazione è andata a buon fine, false altrimenti.
     */
    public static final void updateEmail(String newEmail, ClosureBoolean closureBool){
        if(isLoggedIn()){
            mAuth.getCurrentUser().updateEmail(newEmail).addOnCompleteListener(task -> {
                if (closureBool == null) return;

                if (task.isSuccessful()) closureBool.closure(true);
                else closureBool.closure(false);
            });
        }
    }

    /**
     * Metodo che permette di inviare una email di reset della password.
     *
     * @param email email dell'account di cui si vuole effettare il reset.
     * @param closureBool invocato con true se l'operazione è andata a buon fine, false altrimenti.
     */
    public static final void sendPswResetEmail(String email,ClosureBoolean closureBool){
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (closureBool == null) return;

            if (task.isSuccessful()) closureBool.closure(true);
            else closureBool.closure(false);
        });
    }

    /**
     * Metodo che riautentica l'utente loggato per le operazioni ad alto livello di sicurezza.
     * (Cambio email-psw).
     *
     * @param email email dell'account loggato.
     * @param psw password dell'account loggato.
     * @param closureBool invocato con true se l'operazione è andata a buon fine, false altrimenti.
     */
    public static final void reAuthenticate(String email, String psw, ClosureBoolean closureBool){
        if(isLoggedIn()){
            FirebaseUser currentUser = mAuth.getCurrentUser();
            AuthCredential cred = EmailAuthProvider.getCredential(email,psw);

            currentUser.reauthenticate(cred).addOnCompleteListener(task -> {
                if (closureBool == null) return;

                if (task.isSuccessful()) closureBool.closure(true);
                else closureBool.closure(false);
            });
        }
    }

    /**
     * Metodo che esegue tutte le operazioni di logout per l'utente.
     *
     * @param context contesto dell'applicazione
     */
    public static final void logOut(Context context){
        if(isLoggedIn()){

            //Rimozione del token di notifica su firebase
            Utenti.setMessagingToken("",null);

            //Sing out
            mAuth.signOut();

            //effettua il drop delle tabelle SQLlite
            SQLiteHelper dbDrop = new SQLiteHelper(context);
            dbDrop.dropDatabase();

            //Resetta shared preferences
            SharedPreferencesHelper.resetSharedPreferences(context);

            //Rimozione del daily task
            DailyJobReceiver.removeDailyTask(context);

            //Stop del gatt server e crawler
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(GattServerService.STOP_GATT_SERVER_INTENT));
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(GattServerCrawlerService.STOP_GATT_CRAWLER_INTENT));
        }
    }

    /**
     * Metodo per effettuare il logout dell'Ente.
     */
    public static final void logOutEnte(){
        mAuth.signOut();
    }
}
