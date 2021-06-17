package com.vitandreasorino.savent.AccountTab;

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
import android.util.Log;
import android.view.Gravity;
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

        //Setting the current email
        oldEmail = AuthHelper.getUserLoggedEmail();
        editTextEmail.setText(oldEmail);

        checkSaveButtonActivation();

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Enable or disable all the component relative to the email change
     *
     * @param enabled true to enable the components, false to disable them
     */
    private void toggleEmail(boolean enabled){
        editTextEmail.setEnabled(enabled);
    }

    /**
     * Enable or disable all the component relative to the password change
     *
     * @param enabled true to enable the components, false to disable them
     */
    private void togglePasswordFields(boolean enabled){
        editTextConfirmPassword.setEnabled(enabled);
        editTextNewPassword.setEnabled(enabled);
        editTextOldPassword.setEnabled(enabled);
    }

    /**
     * Enable or disable the button to save all the changes.
     *
     * @param enabled true to enable the component, false to disable it
     */
    private void toggleButtonSaveChanges(boolean enabled){
        buttonSaveChanges.setEnabled(enabled);
    }

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

    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

    /**
     * Used to dynamically select which type of change the user wants to make.
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
     * On click of the saveChanges button.
     *
     * @param view the button.
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

                //You have to reautenticate to do this kind of operation.
                //Let's open an alert dialog
                final AlertDialog.Builder alert = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                input.setInputType(InputType.TYPE_CLASS_TEXT| InputType.TYPE_TEXT_VARIATION_PASSWORD);
                input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                alert.setView(input);    //edit text added to alert
                alert.setTitle(getString(R.string.passwordRequired));   //title setted
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

                //Check all the password fields
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

                //All the password fields are correct
                asyncEventInProgress(true);

                //ReAuthenticate to change the password
                AuthHelper.reAuthenticate(AuthHelper.getUserLoggedEmail(),oldPsw,closureBool ->{
                    if(closureBool){
                        //Password correct
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
                        //Password incorrect
                        Toast.makeText(this,R.string.oldPasswordError,Toast.LENGTH_LONG).show();
                        asyncEventInProgress(false);
                    }
                });

        }
    }

    /**
     * Reset all the string inside all the psw fields
     */
    private void clearAllPswText(){
        editTextOldPassword.setText("");
        editTextNewPassword.setText("");
        editTextConfirmPassword.setText("");
    }

    /**
     * Invoked when the user enters the confirmation password in the dialog
     *
     * @param password the password written by the user in the dialog.
     */
    private void confirmPasswordForEmail(String password){
        asyncEventInProgress(true);

        AuthHelper.reAuthenticate(AuthHelper.getUserLoggedEmail(),password,closureBool ->{
            if(closureBool){
                //Password correct
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
                //Password incorrect
                Toast.makeText(this,R.string.wrongPassword,Toast.LENGTH_LONG).show();
                asyncEventInProgress(false);
            }
        });
    }

    /**
     * Enable/Show or Disable/Hide the saveChanges button and the progress bar.
     *
     * @param inProgress true to disable the button and show the progress bar, false for the opposite.
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







    /*

        OVERRIDE OF ALL THE METHOD OF THE TextWatcher INTERFACE TO MANAGE ALL THE TEXT CHANGE EVENT
        INSIDE ALL THE EDITTEX.

     */

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        checkSaveButtonActivation();
    }
    @Override
    public void afterTextChanged(Editable s) { }
}