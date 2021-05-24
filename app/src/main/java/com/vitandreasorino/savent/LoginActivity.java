package com.vitandreasorino.savent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginActivity extends AppCompatActivity {

    EditText editTextEmailLogin, editTextPasswordLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmailLogin = (EditText) findViewById(R.id.editTextEmailLogin);
        editTextPasswordLogin = (EditText) findViewById(R.id.editTextPasswordLogin);

    }

    public void eventLoginClick(View view) {

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

            Toast.makeText(this, getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{
            backgroundTintEditText();
            Intent schermataHome = new Intent(this, HomeActivity.class);
            startActivity(schermataHome);
        }


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


}