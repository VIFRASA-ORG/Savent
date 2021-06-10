package com.vitandreasorino.savent;


import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.DB.GenericUser;
import Model.DB.Utenti;
import Model.Pojo.Utente;


public class AccountFragment extends Fragment implements View.OnFocusChangeListener{

    RadioButton radioButtonMaleProfile;
    RadioButton radioButtonFemaleProfile;
    RadioButton radioButtonUndefinedProfile;
    RadioGroup radioGroupSex;

    ImageView imageViewProfile;
    TextView textEditProfilePhoto;
    TextView textViewtBirth;

    EditText editTextNome, editTextCognome, editTextPhone;
    Button buttonSaveData;
    ImageView imageViewCalendar;
    ProgressBar progressBar;

    DatePickerDialog picker;

    Calendar newSelectedDate = Calendar.getInstance();
    Uri newSelectedImage;

    View rootView;

    Utente userModel=null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_account, container, false);

        radioButtonMaleProfile = (RadioButton) rootView.findViewById(R.id.radioButtonMaleProfile);
        radioButtonFemaleProfile = (RadioButton) rootView.findViewById(R.id.radioButtonFemaleProfile);
        radioButtonUndefinedProfile = (RadioButton) rootView.findViewById(R.id.radioButtonUndefinedProfile);
        radioGroupSex = (RadioGroup) rootView.findViewById(R.id.radioGroupSex);

        imageViewProfile = (ImageView) rootView.findViewById(R.id.imageViewProfile);
        textEditProfilePhoto = (TextView) rootView.findViewById(R.id.textEditProfilePhoto);

        editTextCognome = (EditText) rootView.findViewById(R.id.editTextCognome);
        editTextNome = (EditText) rootView.findViewById(R.id.editTextNome);
        editTextPhone = (EditText) rootView.findViewById(R.id.editTexPhone);
        textViewtBirth = (TextView) rootView.findViewById(R.id.editTextBirth);

        buttonSaveData = (Button) rootView.findViewById(R.id.buttonSaveData);
        imageViewCalendar = (ImageView) rootView.findViewById(R.id.imageViewCalendar);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        buttonSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveDataButtonClick(v);
            }
        });

        imageViewCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDateDialog();
            }
        });

        textEditProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent imageProfile = new Intent();
                imageProfile.setType("image/*");
                imageProfile.setAction(Intent.ACTION_GET_CONTENT);
                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imageProfile, "Select Picture"), 200);
            }
        });

        radioGroupSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onCheckedChangedRadioGroup(group,checkedId);
            }
        });

        editTextCognome.addTextChangedListener(textWatcher);
        editTextNome.addTextChangedListener(textWatcher);
        editTextPhone.addTextChangedListener(textWatcher);

        editTextPhone.setOnFocusChangeListener(this);
        editTextNome.setOnFocusChangeListener(this);
        editTextCognome.setOnFocusChangeListener(this);

        return rootView;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Used to check if the user is writing some new information that are different from the one on the server
     */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkSaveButtonActivation();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    private void onCheckedChangedRadioGroup(RadioGroup group, int checkedId){
        checkSaveButtonActivation();
    }

    /**
     * Enable or disable the button to save the changes only if there are some changes in the account info.
     */
    private void checkSaveButtonActivation(){
        if(userModel == null) return;

        Calendar storedDate = Calendar.getInstance();
        storedDate.setTime(userModel.getDataNascita());
        if(!editTextNome.getText().toString().equals(userModel.getNome()) || !editTextCognome.getText().toString().equals(userModel.getCognome()) || !editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())
            || !getSelectedGenere().equals(userModel.getGenere()) || newSelectedImage != null ||
            storedDate.get(Calendar.YEAR) != newSelectedDate.get(Calendar.YEAR) || storedDate.get(Calendar.MONTH) != newSelectedDate.get(Calendar.MONTH) || storedDate.get(Calendar.DAY_OF_MONTH) != newSelectedDate.get(Calendar.DAY_OF_MONTH)){
            buttonSaveData.setEnabled(true);
        }else{
            buttonSaveData.setEnabled(false);
        }
    }

    /**
     * Check all the new information entered in the nome, cognome and phoneNumer fields.
     * @return true if all the new values are of the correct pattern, false otherwise.
     */
    private boolean checkAllNewValues(){
        boolean flag = true;

        String nome,cognome,phoneNumber;
        nome = editTextNome.getText().toString();
        cognome = editTextCognome.getText().toString();
        phoneNumber = editTextPhone.getText().toString();

        if( !validazioneCognome(cognome) || !validazioneNome(nome) || !validazioneTelefono(phoneNumber)){
            if(validazioneNome(nome) == false) {
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else {
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCognome(cognome) == false) {
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else {
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneTelefono(phoneNumber) == false) {
                editTextPhone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else{
                editTextPhone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }
        }

        return flag;
    }

    /**
     * Remove the focus from all the components and reset the background color.
     */
    private void clearAllFocusAndColor(){
        editTextCognome.clearFocus();
        editTextNome.clearFocus();
        editTextPhone.clearFocus();
        editTextPhone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
    }

    /**
     * Event called when is pressed the button to save the information to the server
     * @param view
     */
    private void onSaveDataButtonClick(View view){

        clearAllFocusAndColor();

        //Disabling all the component and showing the progress bar
        disableAllComponent();
        buttonSaveData.setEnabled(false);

        if(checkAllNewValues()){

            //Check if the new phone number is a correct phone number only if is different
            if(!editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())){
                GenericUser.isPhoneNumberAlreadyTaken(editTextPhone.getText().toString(),closureBool -> {
                    if(!closureBool){
                        updateFieldToServer();
                    }else{
                        editTextPhone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                        enableAllComponent();
                        buttonSaveData.setEnabled(true);
                        Toast.makeText(getContext(),R.string.phoneNumberAlreadyTaken,Toast.LENGTH_LONG).show();
                    }
                });
            }else{
                updateFieldToServer();
            }
        }else{
            enableAllComponent();
            buttonSaveData.setEnabled(true);
        }
    }

    /**
     * Execute the query to update the new information to the server.
     */
    private void updateFieldToServer(){
        //There's for sure something different to upload to the server
        List<Object> listOfUpdates = new ArrayList<>();

        //nome
        if(!editTextNome.getText().toString().equals(userModel.getNome())){
            listOfUpdates.add(Utenti.NOME_FIELD);
            listOfUpdates.add(editTextNome.getText().toString());
        }

        //cognome
        if(!editTextCognome.getText().toString().equals(userModel.getCognome())){
            listOfUpdates.add(Utenti.COGNOME_FIELD);
            listOfUpdates.add(editTextCognome.getText().toString());
        }

        //telefono
        if(!editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())){
            listOfUpdates.add(Utenti.NUMERO_TELEFONO_FIELD);
            listOfUpdates.add(editTextPhone.getText().toString());
        }

        //data di nascita
        Calendar storedDate = Calendar.getInstance();
        storedDate.setTime(userModel.getDataNascita());
        if(storedDate.get(Calendar.YEAR) != newSelectedDate.get(Calendar.YEAR) || storedDate.get(Calendar.MONTH) != newSelectedDate.get(Calendar.MONTH) || storedDate.get(Calendar.DAY_OF_MONTH) != newSelectedDate.get(Calendar.DAY_OF_MONTH)){
            listOfUpdates.add(Utenti.DATA_NASCITA_FIELD);
            listOfUpdates.add(newSelectedDate.getTime());
        }

        //genere
        if(!getSelectedGenere().equals(userModel.getGenere())){
            listOfUpdates.add(Utenti.GENERE_FIELD);
            listOfUpdates.add(getSelectedGenere());
        }

        //This means that no other information is different rather than the image.
        if(listOfUpdates.size() == 0) {
            //Check if we need to upload the image
            if(newSelectedImage != null){
                Utenti.uploadUserImage(newSelectedImage,getContext(),closureBool -> {
                    if(closureBool){
                        enableAllComponent();
                        buttonSaveData.setEnabled(false);
                        newSelectedImage = null;
                        Toast.makeText(getContext(),R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                    }else{
                        enableAllComponent();
                        Toast.makeText(getContext(),R.string.errorUpload,Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //This code should never be executed because the button should be disabled in case of no new data.
                enableAllComponent();
                buttonSaveData.setEnabled(false);
                Toast.makeText(getContext(),R.string.noUpdates,Toast.LENGTH_SHORT).show();
            }
            return;
        };

        //put the first two as first parameters
        String firstField = (String) listOfUpdates.get(0);;
        Object firstValue = listOfUpdates.get(1);;
        if(listOfUpdates.size() > 2){
            listOfUpdates.remove(0);
            listOfUpdates.remove(0);
        }

        Utenti.updateFields(AuthHelper.getUserId(), closureBool -> {
            if(closureBool){
                //check if we have to upload also the image
                if(newSelectedImage != null){
                    Utenti.uploadUserImage(newSelectedImage,getContext(),closureBool1 -> {
                        if(closureBool1){
                            enableAllComponent();
                            buttonSaveData.setEnabled(false);
                            newSelectedImage = null;
                            Toast.makeText(getContext(),R.string.informationUploaded,Toast.LENGTH_SHORT);
                        }else{
                            enableAllComponent();
                            Toast.makeText(getContext(),R.string.errorUpload,Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    enableAllComponent();
                    buttonSaveData.setEnabled(false);
                    updateModel();
                    Toast.makeText(getContext(),R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                }
            }else{
                enableAllComponent();
                Toast.makeText(getContext(),R.string.errorUpload,Toast.LENGTH_SHORT).show();
            }
        }, firstField,firstValue,listOfUpdates.toArray());
    }

    /**
     * Update the model with the new information after the server update.
     */
    private void updateModel(){
        userModel.setNumeroDiTelefono(editTextPhone.getText().toString());
        userModel.setNome(editTextNome.getText().toString());
        userModel.setCognome(editTextCognome.getText().toString());
        userModel.setGenere(getSelectedGenere());
        userModel.setDataNascita(newSelectedDate.getTime());
    }

    /**
     * Controllo che il numero di telefono rispetti le seguenti caratteristiche:
     * lunghezza compresa tra 9 e 11, consentiti solo caratteri numerici
     * @param controlloTelefono stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneTelefono(String controlloTelefono) {

        if(controlloTelefono == null) {
            return false;
        }

        Pattern p = Pattern.compile("^[0-9]{9,11}$");
        Matcher m = p.matcher(controlloTelefono);
        boolean matchTrovato = m.matches();

        return matchTrovato ;
    }

    /**
     * Controllo che il nome in input rispetti le seguenti caratteristiche:
     * stringa non vuota, lunghezza compresa tra 3 e 15 caratteri,
     * solo caratteri letterali, stringa priva di spazi.
     * @param controlloNome stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNome(String controlloNome) {

        if(controlloNome == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-z]{3,15}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloNome);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Controllo che il cognome in input rispetti le seguenti caratteristiche:
     * stringa non vuota, lunghezza compresa tra 3 e 15 caratteri,
     * solo caratteri letterali, stringa priva di spazi.
     * @param controlloCognome stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneCognome(String controlloCognome) {

        if(controlloCognome == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-z]{3,15}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloCognome);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }

    /**
     * Return the new genere from the RadioGroup
     * @return the new genere as a String
     */
    private String getSelectedGenere(){
        switch (radioGroupSex.getCheckedRadioButtonId()){
            case R.id.radioButtonMaleProfile:
                return Utente.MALE;
            case R.id.radioButtonFemaleProfile:
                return Utente.FEMALE;
            case R.id.radioButtonUndefinedProfile:
                return Utente.UNDEFINED;
            default:
                return "";
        }
    }

    /**
     * Show and manage the results of the DataPickerDialog.
     */
    private void openDateDialog(){

        Calendar c = Calendar.getInstance();
        if(userModel != null){
            c = newSelectedDate;
        }

        picker = new DatePickerDialog(getActivity(),new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                newSelectedDate.set(year,month,dayOfMonth);
                textViewtBirth.setText(dayOfMonth + "/" + ((((month+1)+"").length() == 1) ? "0" : "") +((month+1)+"") + "/" + year);
                checkSaveButtonActivation();
            }
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMaxDate(new Date().getTime());
        picker.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null){
            newSelectedImage = null;
            newSelectedDate = Calendar.getInstance();
        }

        disableAllComponent();

        //Download all the user informations
        Utenti.getUser(AuthHelper.getUserId(),closureRes -> {
            if(closureRes != null){
                userModel = closureRes;
                editTextNome.setText(closureRes.getNome());
                editTextCognome.setText(closureRes.getCognome());
                editTextPhone.setText(closureRes.getNumeroDiTelefono());
                textViewtBirth.setText(closureRes.getNeutralData());
                newSelectedDate.setTime(closureRes.getDataNascita());

                switch (closureRes.getGenere()){
                    case Utente.MALE:
                        radioButtonMaleProfile.setChecked(true);
                        break;
                    case Utente.FEMALE:
                        radioButtonFemaleProfile.setChecked(true);
                        break;
                    case Utente.UNDEFINED:
                        radioButtonUndefinedProfile.setChecked(true);
                        break;
                }

                if(userModel.getIsProfileImageUploaded()){
                    Utenti.downloadUserImage(new ClosureResult<File>() {
                        @Override
                        public void closure(File result) {
                            if(result != null){
                                imageViewProfile.setImageURI(Uri.fromFile(result));
                            }
                        }
                    });
                }

                enableAllComponent();
                checkSaveButtonActivation();
            }
        });

    }

    private void disableAllComponent(){
        editTextNome.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextCognome.setEnabled(false);
        radioButtonUndefinedProfile.setEnabled(false);
        radioButtonMaleProfile.setEnabled(false);
        radioButtonFemaleProfile.setEnabled(false);
        textEditProfilePhoto.setEnabled(false);
        imageViewCalendar.setEnabled(false);

        AnimationHelper.fadeIn(progressBar,1000);
    }

    private void enableAllComponent(){
        editTextNome.setEnabled(true);
        editTextPhone.setEnabled(true);
        editTextCognome.setEnabled(true);
        radioButtonUndefinedProfile.setEnabled(true);
        radioButtonMaleProfile.setEnabled(true);
        radioButtonFemaleProfile.setEnabled(true);
        textEditProfilePhoto.setEnabled(true);
        imageViewCalendar.setEnabled(true);

        AnimationHelper.fadeOut(progressBar,1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageViewProfile.setImageURI(selectedImageUri);
                    newSelectedImage = selectedImageUri;
                    checkSaveButtonActivation();
                }
            }
        }
    }

}
