package com.vitandreasorino.savent.Account;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.vitandreasorino.savent.R;

import Helper.AuthHelper;

public class ChangeCredentialAccountActivity extends AppCompatActivity implements TextWatcher {

    enum ChangeType{
        EMAIL,
        PASSWORD,
        NONE;
    }

    EditText editTextEmail;
    EditText editTextOldPassword;
    EditText editTextNewPassword;
    EditText editTextConfirmPassword;

    Button buttonSaveChanges;
    ChangeType changeType = ChangeType.NONE;

    String oldEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_credential_account);

        inflateAll();

        //Setting the current email
        oldEmail = AuthHelper.getUserLoggedEmail();
        editTextEmail.setText(oldEmail);

        checkSaveButtonActivation();

    }

    private void toggleEmail(boolean enabled){
        editTextEmail.setEnabled(enabled);
    }

    private void togglePasswordFields(boolean enabled){
        editTextConfirmPassword.setEnabled(enabled);
        editTextNewPassword.setEnabled(enabled);
        editTextOldPassword.setEnabled(enabled);
    }

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
    }

    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

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