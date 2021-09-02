package com.vitandreasorino.savent.Utenti.AccountTab;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vitandreasorino.savent.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;

public class ChangeCredentialAccountActivity extends AppCompatActivity implements TextWatcher, View.OnFocusChangeListener {

    //Definizione di un nuovo tipo di enum per la tipologia di dato da resettare
    enum ChangeType{
        EMAIL,
        PASSWORD,
        NONE;
    }

    EditText editTextEmail;
    EditText editTextOldPassword;
    EditText editTextNewPassword;
    EditText editTextConfirmPassword;

    ProgressBar progressBar;
    Button buttonSaveChanges;
    ChangeType changeType = ChangeType.NONE;

    String oldEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_credential_account);

        inflateAll();

        editTextEmail.setOnFocusChangeListener(this);
        editTextOldPassword.setOnFocusChangeListener(this);
        editTextNewPassword.setOnFocusChangeListener(this);
        editTextConfirmPassword.setOnFocusChangeListener(this);

        //Definizione dell' email corrente
        oldEmail = AuthHelper.getUserLoggedEmail();
        editTextEmail.setText(oldEmail);

        checkSaveButtonActivation();

    }

    /**
     * Metodo chiamato quando lo stato di attivazione di una vista è cambiato.
     * @param v : la vista il cui stato è cambiato.
     * @param hasFocus : Il nuovo stato di messa a fuoco del v.
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Si abilitano/disabilitano tutti i componenti relativi al cambio dell'email
     * @param enabled true, per abilitare tutti i componenti, false per disabilitarli
     */
    private void toggleEmail(boolean enabled){
        editTextEmail.setEnabled(enabled);
    }

    /**
     * Si abilitano/disabilitano tutti i componenti relativi al cambio della password
     * @param enabled true, per abilitare tutti i componenti, false per disabilitarli
     */
    private void togglePasswordFields(boolean enabled){
        editTextConfirmPassword.setEnabled(enabled);
        editTextNewPassword.setEnabled(enabled);
        editTextOldPassword.setEnabled(enabled);
    }

    /**
     * Si abilita/disabilita il button per il salavataggio delle modifiche relative ai nuovi dati cambiati
     * @param enabled true, per abilitare tutti i componenti, false per disabilitarli
     */
    private void toggleButtonSaveChanges(boolean enabled){
        buttonSaveChanges.setEnabled(enabled);
    }

    /**
     * Si procede con la dichiarazione e inizializzazione di tutti i componenti
     */
    private void inflateAll(){
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        editTextEmail.addTextChangedListener(this);
        editTextOldPassword.addTextChangedListener(this);
        editTextNewPassword.addTextChangedListener(this);
        editTextConfirmPassword.addTextChangedListener(this);

        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Metodo per tornare alla schermata precedente
     * @param view : la vista che precede quella presente
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

    /**
     *Si provvede a salvare dinamicamente il tipo di modifica che l'utente desidera apportare.
     */
    private void checkSaveButtonActivation(){

        if(!oldEmail.equals(editTextEmail.getText().toString())){
            togglePasswordFields(false);
            toggleButtonSaveChanges(true);
            changeType = ChangeType.EMAIL;
        }else{
            togglePasswordFields(true);
            toggleButtonSaveChanges(false);
            changeType = ChangeType.NONE;
        }

        String oldPsw = editTextOldPassword.getText().toString();
        String newPsw = editTextNewPassword.getText().toString();
        String confirmNewPsw = editTextConfirmPassword.getText().toString();

        if(changeType != ChangeType.EMAIL){
            if(!oldPsw.isEmpty() || !newPsw.isEmpty() || !confirmNewPsw.isEmpty()){
                toggleEmail(false);
                toggleButtonSaveChanges(true);
                changeType = ChangeType.PASSWORD;
            }else{
                toggleEmail(true);
                toggleButtonSaveChanges(false);
                changeType = ChangeType.NONE;
            }
        }
    }

    private void resetAllBackground(){
        editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextOldPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNewPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
    }

    private void clearAllFocus(){
        editTextEmail.clearFocus();
        editTextConfirmPassword.clearFocus();
        editTextNewPassword.clearFocus();
        editTextOldPassword.clearFocus();
    }

    /**
     * Metodo che definisce le attività che susseguono il clic del pulsante SaveChanges.
     * @param view : il button.
     */
    public void onClickSaveChanges(View view){
        switch (changeType){
            case NONE:
                break;

            case EMAIL:

                String email = editTextEmail.getText().toString();

                if(!validazioneEmail(email)){
                    editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    clearAllFocus();
                    return;
                }

                //Occorre ri-autenticarsi per fare questa tipologia di operazione.
                //Viene aperta una finestra di dialogo.
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                //modifica il testo della finestra di dialogo
                alert.setView(input);
                //modifica il titolo della finestra di dialogo
                alert.setTitle(getString(R.string.passwordRequired));
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        confirmPasswordForEmail(input.getText().toString());
                        dialog.cancel();
                    }
                });
                alert.setNegativeButton(getString(R.string.cancelButton),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                final AlertDialog alertDialog = alert.create();
                alertDialog.show();
                break;

            case PASSWORD:

                resetAllBackground();
                clearAllFocus();

                String oldPsw,newPsw,confirmNewPsw;
                oldPsw = editTextOldPassword.getText().toString();
                newPsw = editTextNewPassword.getText().toString();
                confirmNewPsw = editTextConfirmPassword.getText().toString();

                //Verifica tutti i caratteri della password
                if(!validazionePassword(newPsw) || oldPsw.isEmpty() || confirmNewPsw.isEmpty() || !newPsw.equals(confirmNewPsw)){

                    if(oldPsw.isEmpty()) editTextOldPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));

                    if(!validazionePassword(newPsw)) editTextNewPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));

                    if(confirmNewPsw.isEmpty()) {
                        editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                        return;
                    }

                    if(!confirmNewPsw.equals(newPsw)){
                        editTextConfirmPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                        editTextNewPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                        Toast.makeText(this,R.string.passwordsNotMatching,Toast.LENGTH_LONG).show();
                    }

                    return;
                }

                //Se tutti i caratteri della password sono corretti
                asyncEventInProgress(true);

                //Ci si ri-autentica per cambiare la passwrod
                AuthHelper.reAuthenticate(AuthHelper.getUserLoggedEmail(),oldPsw,closureBool ->{
                    if(closureBool){
                        //Si inserisce la nuova password corretta
                        AuthHelper.updatePsw(newPsw, closureBool1 -> {
                            if (closureBool1) {
                                Toast.makeText(this, R.string.passwordChangeSuccess, Toast.LENGTH_LONG).show();
                                clearAllPswText();
                                checkSaveButtonActivation();
                                asyncEventInProgress(false);
                            } else {
                                Toast.makeText(this, R.string.passwordChangeError, Toast.LENGTH_LONG).show();
                                asyncEventInProgress(false);
                            }
                        });
                    }else{
                        //Se la password non è corretta
                        Toast.makeText(this,R.string.oldPasswordError,Toast.LENGTH_LONG).show();
                        asyncEventInProgress(false);
                    }
                });

        }
    }

    /**
     * Vengono resettate tutte le stringhe contenute nei campi della password
     */
    private void clearAllPswText(){
        editTextOldPassword.setText("");
        editTextNewPassword.setText("");
        editTextConfirmPassword.setText("");
    }

    /**
     *Richiamato quando l'utente inserisce la password di conferma nella finestra di dialogo
     *
     * @param password :password scritta dall'utente nella finestra di dialogo
     */
    private void confirmPasswordForEmail(String password){
        asyncEventInProgress(true);

        AuthHelper.reAuthenticate(AuthHelper.getUserLoggedEmail(),password,closureBool ->{
            if(closureBool){
                //Se la password è corretta
                AuthHelper.updateEmail(editTextEmail.getText().toString(), cloosureBool1 -> {
                    if (cloosureBool1) {
                        Toast.makeText(this, R.string.emailChangeSuccess, Toast.LENGTH_LONG).show();
                        oldEmail = editTextEmail.getText().toString();
                        checkSaveButtonActivation();
                        asyncEventInProgress(false);
                    } else {
                        Toast.makeText(this, R.string.emailChangeError, Toast.LENGTH_LONG).show();
                        asyncEventInProgress(false);
                    }
                });
            }else{
                //Se la password è errata
                Toast.makeText(this,R.string.wrongPassword,Toast.LENGTH_LONG).show();
                asyncEventInProgress(false);
            }
        });
    }

    /**
     * Si abilita/mostra o disabilita/nascondie il pulsante SaveChanges e la progress bar.
     * @param inProgress true, se disabilita il button e mostra la progress bar, false altrimenti.
     */
    private void asyncEventInProgress(boolean inProgress){
        if (inProgress) {
            AnimationHelper.fadeIn(progressBar,500);
            buttonSaveChanges.setEnabled(false);
        }else{
            AnimationHelper.fadeOut(progressBar,500);
            buttonSaveChanges.setEnabled(true);
        }
    }

    /**
     * Controlla che l'email in input rispetti la forma standard delle email.
     * @param controlloEmail stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false.
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
     * Controlla che la password in input rispetti le seguenti caratteristiche:
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
     * Metodo che viene chiamato per avvisarti che, all'interno di s, i countcaratteri che iniziano a start stanno per
     * essere sostituiti da un nuovo testo con lunghezza after.
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    /**
     * Metodo che viene chiamato per avvisarti che, all'interno di s, i countcaratteri che iniziano a start hanno appena sostituito
     * il vecchio testo che aveva lunghezza before. È un errore tentare di apportare modifiche ad s da questo callback.
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkSaveButtonActivation();
    }

    /**
     * Metodo che viene chiamato per informarti che, da qualche parte all'interno di s, il testo è stato modificato.
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) { }
}