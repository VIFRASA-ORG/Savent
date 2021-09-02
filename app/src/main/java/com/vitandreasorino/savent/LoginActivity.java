package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.vitandreasorino.savent.Enti.HomeActivityEnte;
import Model.LogDebug;
import Services.BluetoothLEServices.GattServerCrawlerService;
import Services.BluetoothLEServices.GattServerService;
import com.vitandreasorino.savent.Utenti.HomeActivity;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureBoolean;
import Model.DAO.Enti;
import Model.DAO.TemporaryExposureKeys;
import Model.DAO.Utenti;
import Services.DailyJob.DailyJobReceiver;

/**
 * Activity inerente alla gestione del login. Permette la visualizzazione e l'inserimento dei dati per effettuare l'accesso oppure
 * di richiedere il recupero della nuova password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    EditText editTextEmailLogin, editTextPasswordLogin;
    EditText editTextRecoveryEmail;

    TextView textPasswordDimenticata;
    Button buttonAccediLogin;

    ProgressBar progressBar;

    //Dialog per il recupero della password
    private AlertDialog pswRecoveryDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //riferimenti xml
        editTextEmailLogin = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = (EditText) findViewById(R.id.editTextPasswordLogin);
        textPasswordDimenticata = findViewById(R.id.textPasswordDimenticata);
        buttonAccediLogin = findViewById(R.id.buttonAccediLogin);
        progressBar = findViewById(R.id.progressBar);

        editTextEmailLogin.setOnFocusChangeListener(this);
        editTextPasswordLogin.setOnFocusChangeListener(this);
    }


    /**
     * Disabilita o abilita il componente se è in corso un download.
     * In questo caso mostra anche la barra di avanzamento.
     * @param inProgress flag che indica se il download è in corso.
     */
    private void toggleInProgressEvent(boolean inProgress){
        editTextEmailLogin.setEnabled(!inProgress);
        editTextPasswordLogin.setEnabled(!inProgress);
        textPasswordDimenticata.setEnabled(!inProgress);
        buttonAccediLogin.setEnabled(!inProgress);

        if(inProgress) AnimationHelper.fadeIn(progressBar,500);
        else AnimationHelper.fadeOut(progressBar,500);
    }

    /**
     * Rimuovere il focus da tutti i componenti all'interno della vista.
     */
    private void clearAllFocus(){
        editTextEmailLogin.clearFocus();
        editTextPasswordLogin.clearFocus();
    }

    /**
     * Metodo che permette di visualizzare un focus differente degli attributi "email" e "password", quando sono in fase di modifica\scrittura.
     * Se tali campi sono in fase di modifica\scrittura allora tale colore associato passerà da blu al grigio, fino quando gli verrà tolto il focus
     * di tale campo.
     * Il metodo viene chiamato dal onFocusChangeListener, che viene associato all'email e alla password.
     * @param v: vista del focus.
     * @param hasFocus: impostazione del nuovo focus di stato della vista.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Metodo richiamato quando viene premuto il pulsante di accesso (login)
     * @param view
     */
    public void eventLoginClick(View view) {
        editTextPasswordLogin.clearFocus();
        editTextEmailLogin.clearFocus();
        controlloInputUtenteLogin();
    }

    /**
     * Metodo che esegue un controllo di integrità sui campi.
     */
    private void controlloInputUtenteLogin() {

        String emailLogin, passwordLogin;
        emailLogin = editTextEmailLogin.getText().toString();
        passwordLogin = editTextPasswordLogin.getText().toString();

        clearAllFocus();

        //Controllo per verificare se l'utente ha commesso errori nell'inserrimento dati, con l'attribuzione del colore rosso
        if( validazioneEmail(emailLogin) == false || validazionePassword(passwordLogin) == false || passwordLogin.contains(" ")) {

            //controllo l'email inserita nel campo...
            if(validazioneEmail(emailLogin) == false) {
                editTextEmailLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                editTextEmailLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            //controllo della password inserita nel campo...
            if(validazionePassword(passwordLogin) == false || passwordLogin.contains(" ")) {
                editTextPasswordLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(this, getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else{
                editTextPasswordLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }
        }else{ //Se tutti i campi sono corretti..

            backgroundTintEditText();
            toggleInProgressEvent(true);

            //Cercando di accedere utilizzando le credenziali fornite
            AuthHelper.singIn(emailLogin, passwordLogin, new ClosureBoolean() {
                @Override
                public void closure(boolean isSuccess) {
                    if(isSuccess){
                        loginEffettuato();
                    }else{
                        errorDuringLogin();
                    }
                }
            });
        }
    }

    /**
     * Metodo richiamato nel caso l'accesso non ha avuto esito positivo.
     */
    private final void errorDuringLogin(){
        Toast.makeText(getApplicationContext(),getString(R.string.errorLogin),Toast.LENGTH_SHORT).show();
        toggleInProgressEvent(false);
    }

    /**
     * Metodo richiamato se l'accesso ha avuto esito positivo.
     */
    private final void loginEffettuato(){

        //Controllo che tipo di utente ha effettuato l'accesso
        AuthHelper.getLoggedUserType(closureRes -> {
            switch (closureRes){
                case Utente:
                    loggedInAsUser();
                    break;
                case Ente:
                    loggedInAsEnte();
            }
        });
    }


    /**
     * Richiamato quando un utente è loggato come Ente.
     */
    private void loggedInAsEnte(){

        //Controlla se l'account Ente è abilitato dall'amministratore.
        Enti.isEnteEnabled(AuthHelper.getUserId(),closureBool ->{
            if(closureBool){
                //Vai all'home ente
                Log.i("AUTH","Loggato come ente");
                Intent i = new Intent(this, HomeActivityEnte.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //Rimozione dal task di tutte le activity precedenti
                startActivity(i);
                finish();
            }else{
                Toast.makeText(getApplicationContext(),R.string.accountNotActivated,Toast.LENGTH_SHORT).show();
                AuthHelper.logOutEnte();
            }
            toggleInProgressEvent(false);
        });
    }

    /**
     * Richiamato quando un utente normale è loggato
     */
    private void loggedInAsUser(){
        //Comunicazione del nuovo token di notifica al server
        Utenti.setMessagingToken(isSucc -> {
            if (!isSucc){
                Utenti.createMessagingTokenDocument(null);
            }
        });

        //Generazione della prima Tek
        TemporaryExposureKeys.generateNewTEK(this, newTek -> {

            try {
                //Avvio del server gatt solo dopo che è stato generato il primo tek
                startService(new Intent(getBaseContext(), GattServerService.class));
                startService(new Intent(getBaseContext(), GattServerCrawlerService.class));
            }catch (IllegalArgumentException e){
                Log.w(LogDebug.GAT_ERROR, "Failed to restart Ble Service (process is idle).");
            }catch (IllegalStateException e){
                Log.w(LogDebug.GAT_ERROR, "Failed to restart Ble Service (app is in background, foregroundAllowed == false).");
            }
        });

        //Pianificazione dell'attività quotidiana
        DailyJobReceiver.scheduleDailyTask(this);

        //Vai all'home utente normale
        Toast.makeText(getApplicationContext(),getString(R.string.correctLogin),Toast.LENGTH_SHORT).show();
        Intent schermataHome = new Intent(getApplicationContext(), HomeActivity.class);
        schermataHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the task all the previous Activity.
        startActivity(schermataHome);
        finish();
    }

    /**
     * Settaggio dell'underline a tutte le editText nel colore grigio.
     */
    public void backgroundTintEditText() {
        editTextEmailLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextPasswordLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
    }


    /**
     * Controllo che l'email in input rispetti la forma standard delle email.
     * @param controlloEmail stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneEmail(String controlloEmail) {

        if(controlloEmail == null) {
            return false;
        }

        Pattern p = Pattern.compile(".+@.+\\.[a-z]+", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloEmail);
        boolean matchTrovato = m.matches();

        String  espressioneAggiuntiva ="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pAggiuntiva = Pattern.compile(espressioneAggiuntiva, Pattern.CASE_INSENSITIVE);
        Matcher mAggiuntiva = pAggiuntiva.matcher(controlloEmail);
        boolean matchTrovatoAggiuntivo = mAggiuntiva.matches();

        return matchTrovato && matchTrovatoAggiuntivo;
    }


    /**
     * Controllo che la password in input rispetti le seguenti caratteristiche:
     * contenta un carattere maiuscolo, contenga un carattere minuscolo e contenga
     * un carattere numerico e essa deve essere di lunghezza compresa tra 8 e 20 caratteri.
     * @param controlloPassword stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazionePassword(String controlloPassword) {

        if(controlloPassword == null)  {
            return false;
        }

        Pattern p = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})");
        Matcher m = p.matcher(controlloPassword);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }

    /**
     * Evento su clic per il recupero della password
     * Mostra la finestra di dialog per il recupero della password
     * @param v
     */
    public void onClickPasswordForgot(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View view = layoutInflaterAndroid.inflate(R.layout.password_forgot_dialog, null);
        builder.setView(view);

        editTextRecoveryEmail = view.findViewById(R.id.recoveryPswEmail);
        editTextRecoveryEmail.setOnFocusChangeListener(this);

        pswRecoveryDialog = builder.create();
        pswRecoveryDialog.show();
    }

    /**
     * Metodo invocato quando il pulsante invia un'e-mail di recupero dalla finestra del dialog per il recupero della password.
     * @param v la vista del chiamante.
     */
    public void onClickSendRecoveryEmail(View v){
        String value = editTextRecoveryEmail.getText().toString();
        editTextRecoveryEmail.clearFocus();

        //Controllo se l'e-mail fornita è un'e-mail valida.
        if(value != null && !value.equals("") && validazioneEmail(value)){
            Log.i("LOG",editTextRecoveryEmail.getText().toString());

            //Esecuzione della richiesta al server di inviare un'e-mail di ripristino psw.
            AuthHelper.sendPswResetEmail(value,closureBool -> {
                pswRecoveryDialog.dismiss();
                Toast.makeText(this,R.string.pswRecoveryToast,Toast.LENGTH_LONG).show();
            });
        }else{
            //Se l'e-mail non è valida, mostra un errore.
            editTextRecoveryEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pswRecoveryDialog != null) pswRecoveryDialog.dismiss();
    }
}