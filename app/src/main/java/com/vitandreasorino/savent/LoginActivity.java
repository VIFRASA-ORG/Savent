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
import com.vitandreasorino.savent.Utenti.BluetoothLEServices.GattServerCrawlerService;
import com.vitandreasorino.savent.Utenti.BluetoothLEServices.GattServerService;
import com.vitandreasorino.savent.Utenti.HomeActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureBoolean;
import Model.DB.Enti;
import Model.DB.TemporaryExposureKeys;
import Model.DB.Utenti;


public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    EditText editTextEmailLogin, editTextPasswordLogin;
    EditText editTextRecoveryEmail;

    TextView textPasswordDimenticata;
    Button buttonAccediLogin;

    ProgressBar progressBar;

    //Dialog for the password recovery
    private AlertDialog pswRecoveryDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmailLogin = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = (EditText) findViewById(R.id.editTextPasswordLogin);

        textPasswordDimenticata = findViewById(R.id.textPasswordDimenticata);
        buttonAccediLogin = findViewById(R.id.buttonAccediLogin);
        progressBar = findViewById(R.id.progressBar);

        editTextEmailLogin.setOnFocusChangeListener(this);
        editTextPasswordLogin.setOnFocusChangeListener(this);
    }

    /**
     * Disable or enable the component if a download is going on.
     * In this case it also shows the progress bar.
     *
     * @param inProgress flag indicating whether the download is in progress.
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
     * Remove the focus from all the components inside the view.
     */
    private void clearAllFocus(){
        editTextEmailLogin.clearFocus();
        editTextPasswordLogin.clearFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Method invoked when the log-in button is pressed
     *
     * @param view the caller view.
     */
    public void eventLoginClick(View view) {
        editTextPasswordLogin.clearFocus();
        editTextEmailLogin.clearFocus();
        controlloInputUtenteLogin();
    }

    /**
     * Method that run a sanity check on all the field.
     */
    private void controlloInputUtenteLogin() {

        String emailLogin, passwordLogin;
        emailLogin = editTextEmailLogin.getText().toString();
        passwordLogin = editTextPasswordLogin.getText().toString();

        clearAllFocus();

        if( validazioneEmail(emailLogin) == false || validazionePassword(passwordLogin) == false || passwordLogin.contains(" ")) {
            //The user made some error

            if(validazioneEmail(emailLogin) == false) {
                editTextEmailLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                editTextEmailLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazionePassword(passwordLogin) == false || passwordLogin.contains(" ")) {
                editTextPasswordLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(this, getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else{
                editTextPasswordLogin.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

        }else{
            //All field are correct

            backgroundTintEditText();
            toggleInProgressEvent(true);

            //Trying to log-in using the given credential
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
     * Method invoked if the login is not successful.
     */
    private final void errorDuringLogin(){
        Toast.makeText(getApplicationContext(),getString(R.string.errorLogin),Toast.LENGTH_SHORT).show();
        toggleInProgressEvent(false);
    }

    /**
     * Method invoked if the login is successful.
     */
    private final void loginEffettuato(){

        //Checking what kind of user is logged in
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
     * Invoked when an Ente user is logged in.
     */
    private void loggedInAsEnte(){

        //Checkinf if the Ente account is enabled by the admin.
        Enti.isEnteEnabled(AuthHelper.getUserId(),closureBool ->{
            if(closureBool){
                //Going to the Ente Home
                Log.i("AUTH","Loggato come ente");
                Intent i = new Intent(this, HomeActivityEnte.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the task all the previous Activity.
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
     * Invoked when a normal user is logged in
     */
    private void loggedInAsUser(){
        //Communicating the new notification token to the server
        Utenti.setMessagingToken(isSucc -> {
            if (!isSucc){
                Utenti.createMessagingTokenDocument(null);
            }
        });

        //Generating the first Temporary exposure key
        TemporaryExposureKeys.generateNewTEK(this, newTek -> {
            //Starting the gatt server only after the first tek is generated
            startService(new Intent(getBaseContext(), GattServerService.class));
            startService(new Intent(getBaseContext(), GattServerCrawlerService.class));
        });

        //Going to the normal user Home
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
     * On click event for the password recovery
     * It shows the password recovery dialog
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
     * Method invoked when the send recovery email button from the password recovery dialog.
     *
     * @param v the caller view.
     */
    public void onClickSendRecoveryEmail(View v){
        String value = editTextRecoveryEmail.getText().toString();
        editTextRecoveryEmail.clearFocus();

        //Checking if the given email is a valid email.
        if(value != null && !value.equals("") && validazioneEmail(value)){
            Log.i("LOG",editTextRecoveryEmail.getText().toString());

            //Executing the request to the server to send a psw reset email.
            AuthHelper.sendPswResetEmail(value,closureBool -> {
                pswRecoveryDialog.dismiss();
                Toast.makeText(this,R.string.pswRecoveryToast,Toast.LENGTH_LONG).show();
            });
        }else{
            //If the email is not valid, show an error.
            editTextRecoveryEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pswRecoveryDialog != null) pswRecoveryDialog.dismiss();
    }
}