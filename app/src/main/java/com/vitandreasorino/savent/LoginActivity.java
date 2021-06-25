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
import android.widget.EditText;
import android.widget.Toast;

import com.vitandreasorino.savent.Utenti.HomeActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AuthHelper;
import Model.Closures.ClosureBoolean;
import Model.DB.Enti;


public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    EditText editTextEmailLogin, editTextPasswordLogin;
    EditText editTextRecoveryEmail;

    //Dialog for the password recovery
    private AlertDialog pswRecoveryDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmailLogin = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = (EditText) findViewById(R.id.editTextPasswordLogin);

        editTextEmailLogin.setOnFocusChangeListener(this);
        editTextPasswordLogin.setOnFocusChangeListener(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    public void eventLoginClick(View view) {
        editTextPasswordLogin.clearFocus();
        editTextEmailLogin.clearFocus();
        controlloInputUtenteLogin();
    }

    private void controlloInputUtenteLogin() {

        String emailLogin, passwordLogin;
        emailLogin = editTextEmailLogin.getText().toString();
        passwordLogin = editTextPasswordLogin.getText().toString();

        if( validazioneEmail(emailLogin) == false || validazionePassword(passwordLogin) == false || passwordLogin.contains(" ")) {


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
            backgroundTintEditText();
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

    private final void errorDuringLogin(){
        Toast.makeText(getApplicationContext(),getString(R.string.errorLogin),Toast.LENGTH_SHORT).show();
    }

    private final void loginEffettuato(){

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

    private void loggedInAsEnte(){
        Enti.isEnteEnabled(AuthHelper.getUserId(),closureBool ->{
            if(closureBool){
                //Start the activity for the ente
                Log.i("AUTH","Loggato come ente");
            }else{
                Toast.makeText(getApplicationContext(),R.string.accountNotActivated,Toast.LENGTH_SHORT).show();
                AuthHelper.logOut();
            }
        });
    }

    private void loggedInAsUser(){
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

    public void onClickSendRecoveryEmail(View v){
        String value = editTextRecoveryEmail.getText().toString();
        editTextRecoveryEmail.clearFocus();

        if(value != null && !value.equals("") && validazioneEmail(value)){
            Log.i("LOG",editTextRecoveryEmail.getText().toString());
            AuthHelper.sendPswResetEmail(value,closureBool -> {
                pswRecoveryDialog.dismiss();
                Toast.makeText(this,R.string.pswRecoveryToast,Toast.LENGTH_LONG).show();
            });
        }else{
            editTextRecoveryEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pswRecoveryDialog != null) pswRecoveryDialog.dismiss();
    }
}