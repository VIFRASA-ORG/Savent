package com.vitandreasorino.savent.Utenti.AccountTab;


import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.vitandreasorino.savent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
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
    TextView textViewChangeCredential;

    EditText editTextNome, editTextCognome, editTextPhone;
    EditText editTextCodiceFiscale;
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
        editTextCodiceFiscale = rootView.findViewById(R.id.editTextCodiceFiscale);
        textViewtBirth = (TextView) rootView.findViewById(R.id.editTextBirth);

        buttonSaveData = (Button) rootView.findViewById(R.id.buttonSaveData);
        imageViewCalendar = (ImageView) rootView.findViewById(R.id.imageViewCalendar);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        textViewChangeCredential = (TextView) rootView.findViewById(R.id.textViewChangeCredential);

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
                //Viene passata la costante per il confronto con il valore di ritorno requestCode
                startActivityForResult(Intent.createChooser(imageProfile, "Select Picture"), 200);
            }
        });

        radioGroupSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            /**
             * Si verifica se il contatto è stato selezionato. Se è stato selezionato questo viene aggiunto
             * alla lista dei contatti selezionati, altrimenti viene rimosso da questa lista
             * @param group :la vista del pulsante che è stato cambiato.
             * @param checkedId :il nuovo stato selezionato di radioGroup.
             */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onCheckedChangedRadioGroup(group,checkedId);
            }
        });

        textViewChangeCredential.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(),ChangeCredentialAccountActivity.class);
                startActivity(i);
            }
        });

        editTextCognome.addTextChangedListener(textWatcher);
        editTextNome.addTextChangedListener(textWatcher);
        editTextPhone.addTextChangedListener(textWatcher);
        editTextCodiceFiscale.addTextChangedListener(textWatcher);

        editTextPhone.setOnFocusChangeListener(this);
        editTextNome.setOnFocusChangeListener(this);
        editTextCognome.setOnFocusChangeListener(this);
        editTextCodiceFiscale.setOnFocusChangeListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(savedInstanceState != null){
            newSelectedImage = null;
            newSelectedDate = Calendar.getInstance();
        }

        disableAllComponent();

        //Scarica tutte le informazioni relative agli utenti
        Utenti.getUser(AuthHelper.getUserId(),closureRes -> {
            if(closureRes != null){
                userModel = closureRes;
                editTextNome.setText(closureRes.getNome());
                editTextCognome.setText(closureRes.getCognome());
                editTextPhone.setText(closureRes.getNumeroDiTelefono());
                textViewtBirth.setText(closureRes.getNeutralData());
                newSelectedDate.setTime(closureRes.getDataNascita());
                editTextCodiceFiscale.setText(userModel.getCodiceFiscale());

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

    /**
     * Viene richiamato quando lo stato di attivazione di una vista è cambiato.
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
     * Si verifica se l'utente sta scrivendo alcune nuove informazioni differenti da quelle presenti sul server
     */
    TextWatcher textWatcher = new TextWatcher() {

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
    };

    /**
     *Si attiva il cambio dello stato della radiogroup. Se sono state apportate modifiche
     *si passa poi a rendere attivo il button del Salva Modifiche.
     *@param group :la vista del pulsante che è stato cambiato.
     *@param checkedId :il nuovo stato selezionato di radioGroup.
    **/
    private void onCheckedChangedRadioGroup(RadioGroup group, int checkedId){
        checkSaveButtonActivation();
    }

    /**
     * Si abilita/disabilita il button per salvare il cambio solo se sono state apportate modifiche alle info dell'account
     */
    private void checkSaveButtonActivation(){
        if(userModel == null) return;

        Calendar storedDate = Calendar.getInstance();
        storedDate.setTime(userModel.getDataNascita());
        if(!editTextCodiceFiscale.getText().toString().equalsIgnoreCase(userModel.getCodiceFiscale()) || !editTextNome.getText().toString().equals(userModel.getNome()) || !editTextCognome.getText().toString().equals(userModel.getCognome()) || !editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())
            || !getSelectedGenere().equals(userModel.getGenere()) || newSelectedImage != null ||
            storedDate.get(Calendar.YEAR) != newSelectedDate.get(Calendar.YEAR) || storedDate.get(Calendar.MONTH) != newSelectedDate.get(Calendar.MONTH) || storedDate.get(Calendar.DAY_OF_MONTH) != newSelectedDate.get(Calendar.DAY_OF_MONTH)){
            buttonSaveData.setEnabled(true);
        }else{
            buttonSaveData.setEnabled(false);
        }
    }

    /**
     * Si verificano tutte le nuove informazioni nei campi del nome, cognome e numero di telefono.
     * @return true, se tutti i valori sono stati inseriti in forma corretta, false altrimenti.
     */
    private boolean checkAllNewValues(){
        boolean flag = true;

        String nome,cognome,phoneNumber, codiceFiscale;
        nome = editTextNome.getText().toString();
        cognome = editTextCognome.getText().toString();
        phoneNumber = editTextPhone.getText().toString();
        codiceFiscale = editTextCodiceFiscale.getText().toString().toUpperCase();

        if( !validazioneCognome(cognome) || !validazioneNome(nome) || !validazioneTelefono(phoneNumber) || !validazioneCodiceFiscale(codiceFiscale)){
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

            if(validazioneCodiceFiscale(codiceFiscale) == false) {
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else{
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }
        }

        return flag;
    }

    /**
     * Viene rimosso lo stato attivo da tutti i componenti e a reimpostare il colore di sfondo.
     */
    private void clearAllFocusAndColor(){
        editTextCognome.clearFocus();
        editTextNome.clearFocus();
        editTextPhone.clearFocus();
        editTextPhone.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
    }

    /**
     *
     * Lo si invoca quando viene premuto il pulsante per salvare le informazioni sul server.
     * @param view: la nuova vista con i dati aggiornati.
     */
    private void onSaveDataButtonClick(View view){

        clearAllFocusAndColor();

        //Si disabilitano tutti i componenti e si mostrano le progress bar
        disableAllComponent();
        buttonSaveData.setEnabled(false);

        if(checkAllNewValues()){

            //Si verifica se il nuovo numero di telefono sia stato inserito correttamente
            if(!editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())){
                GenericUser.isPhoneNumberAlreadyTaken(editTextPhone.getText().toString(),closureBool -> {
                    if(!closureBool){

                        Utenti.isFiscalCodeAlreadyUsed(editTextCodiceFiscale.getText().toString().toUpperCase(), isTaken -> {
                            if(isTaken){
                                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                                enableAllComponent();
                                buttonSaveData.setEnabled(true);
                                Toast.makeText(getContext(),R.string.fiscalCodeAlreadyTaken,Toast.LENGTH_LONG).show();
                                return;
                            }else{
                                updateFieldToServer();
                            }
                        });
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
     *Si esegue la query per aggiornare le nuove informazioni sul server.
     */
    private void updateFieldToServer(){
        //C'è sicuramente qualcosa di diverso da caricare sul server
        List<Object> listOfUpdates = new ArrayList<>();

        //Modifica per il nome
        if(!editTextNome.getText().toString().equals(userModel.getNome())){
            listOfUpdates.add(Utenti.NOME_FIELD);
            listOfUpdates.add(editTextNome.getText().toString());
        }

        //Modifica per il cognome
        if(!editTextCognome.getText().toString().equals(userModel.getCognome())){
            listOfUpdates.add(Utenti.COGNOME_FIELD);
            listOfUpdates.add(editTextCognome.getText().toString());
        }

        //Modifica per il telefono
        if(!editTextPhone.getText().toString().equals(userModel.getNumeroDiTelefono())){
            listOfUpdates.add(Utenti.NUMERO_TELEFONO_FIELD);
            listOfUpdates.add(editTextPhone.getText().toString());
        }

        //Modifica per il codice fiscale
        if(!editTextCodiceFiscale.getText().toString().equalsIgnoreCase(userModel.getCodiceFiscale())){
            listOfUpdates.add(Utenti.CODICE_FISCALE_FIELD);
            listOfUpdates.add(editTextCodiceFiscale.getText().toString().toUpperCase());
        }

        //Modifica per la data di nascita
        Calendar storedDate = Calendar.getInstance();
        storedDate.setTime(userModel.getDataNascita());
        if(storedDate.get(Calendar.YEAR) != newSelectedDate.get(Calendar.YEAR) || storedDate.get(Calendar.MONTH) != newSelectedDate.get(Calendar.MONTH) || storedDate.get(Calendar.DAY_OF_MONTH) != newSelectedDate.get(Calendar.DAY_OF_MONTH)){
            listOfUpdates.add(Utenti.DATA_NASCITA_FIELD);
            listOfUpdates.add(newSelectedDate.getTime());
        }

        //Modifica per il genere
        if(!getSelectedGenere().equals(userModel.getGenere())){
            listOfUpdates.add(Utenti.GENERE_FIELD);
            listOfUpdates.add(getSelectedGenere());
        }

        //Questo significa che nessun'altra informazione è diversa dall'immagine.
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
                //Questo codice non dovrebbe mai essere eseguito perché il pulsante dovrebbe essere disabilitato in caso di assenza di nuovi dati.
                enableAllComponent();
                buttonSaveData.setEnabled(false);
                Toast.makeText(getContext(),R.string.noUpdates,Toast.LENGTH_SHORT).show();
            }
            return;
        };

        //Metti le prime due come primo parametro
        String firstField = (String) listOfUpdates.get(0);;
        Object firstValue = listOfUpdates.get(1);;
        if(listOfUpdates.size() > 2){
            listOfUpdates.remove(0);
            listOfUpdates.remove(0);
        }

        Utenti.updateFields(AuthHelper.getUserId(), closureBool -> {
            if(closureBool){
                //Si ontrolla se si deve caricare anche l'immagine
                if(newSelectedImage != null){
                    Utenti.uploadUserImage(newSelectedImage,getContext(),closureBool1 -> {
                        if(closureBool1){
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
     *Viene aggiornato il modello con le nuove informazioni dopo l'aggiornamento del server.
     */
    private void updateModel(){
        userModel.setNumeroDiTelefono(editTextPhone.getText().toString());
        userModel.setNome(editTextNome.getText().toString());
        userModel.setCognome(editTextCognome.getText().toString());
        userModel.setGenere(getSelectedGenere());
        userModel.setDataNascita(newSelectedDate.getTime());
        userModel.setCodiceFiscale(editTextCodiceFiscale.getText().toString().toUpperCase());
    }

    /**
     * Controlla che la stringa in input sia conforme alla formattazione e alla lunghezza
     * di un comune codice fiscale.
     * @param controlloCodiceFiscaleFreelance stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneCodiceFiscale(String controlloCodiceFiscaleFreelance) {

        if(controlloCodiceFiscaleFreelance == null)  {
            return false;
        }

        Pattern p = Pattern.compile("[a-zA-Z]{6}\\d\\d[a-zA-Z]\\d\\d[a-zA-Z]\\d\\d\\d[a-zA-Z]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloCodiceFiscaleFreelance);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }

    /**
     * Controlla che il numero di telefono rispetti le seguenti caratteristiche:
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
     * Controlla che il nome in input rispetti le seguenti caratteristiche:
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
     * Controlla che il cognome in input rispetti le seguenti caratteristiche:
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
     * Si ottiene il nuovo genere dalla RadioGroup
     * @return il nuovo genere, sotto forma di stringa
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
     * Vengono mostrati e si gestiscono i risultati del DataPickerDialog.
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

    /**
     * Vengono disabilitati tutti i componenti del layout
     */
    private void disableAllComponent(){
        editTextNome.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextCognome.setEnabled(false);
        radioButtonUndefinedProfile.setEnabled(false);
        radioButtonMaleProfile.setEnabled(false);
        radioButtonFemaleProfile.setEnabled(false);
        textEditProfilePhoto.setEnabled(false);
        imageViewCalendar.setEnabled(false);
        editTextCodiceFiscale.setEnabled(false);

        AnimationHelper.fadeIn(progressBar,1000);
    }

    /**
     * Si abilitano tutti i componenti del layout
     */
    private void enableAllComponent(){
        editTextNome.setEnabled(true);
        editTextPhone.setEnabled(true);
        editTextCognome.setEnabled(true);
        radioButtonUndefinedProfile.setEnabled(true);
        radioButtonMaleProfile.setEnabled(true);
        radioButtonFemaleProfile.setEnabled(true);
        textEditProfilePhoto.setEnabled(true);
        imageViewCalendar.setEnabled(true);
        editTextCodiceFiscale.setEnabled(true);

        AnimationHelper.fadeOut(progressBar,1000);
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative alla modifica dell'immagine profilo.
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati della modifica al chiamante.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {

            // confronta il resultCode con la costante SELECT_PICTURE
            if (requestCode == 200) {
                // Ottieni l'URL dell'immagine dai dati
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Aggiorna l'immagine di anteprima nel layout
                    imageViewProfile.setImageURI(selectedImageUri);
                    newSelectedImage = selectedImageUri;
                    checkSaveButtonActivation();
                }
            }
        }
    }

}
