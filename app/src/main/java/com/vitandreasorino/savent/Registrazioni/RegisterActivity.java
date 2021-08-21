package com.vitandreasorino.savent.Registrazioni;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vitandreasorino.savent.Utenti.HomeActivity;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Registrazioni.RegistrazioniEnte.RegisterEnteActivity;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Helper.ImageHelper;
import Model.DB.GenericUser;
import Model.DB.Utenti;
import Model.Pojo.Utente;


public class RegisterActivity extends AppCompatActivity implements View.OnFocusChangeListener {

    ImageView imageView;
    RadioButton rbM,rbF,rbU;

    EditText editTextNome,editTextCognome,editTextTelefono,
             editTextEmail,editTextPassword,editTextConfermaPassword,editTextCodiceFiscale;

    TextView textViewBirth;
    ImageView imageViewCalendar;
    DatePickerDialog picker;
    Calendar selectedBirthDate;

    Button bottoneRegistrazione;
    TextView textViewEditImage,textViewRegistrazioneEnte;
    ProgressBar progressBar;

    Uri imageSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = (ImageView) findViewById(R.id.imageView);
        rbM = (RadioButton) findViewById(R.id.radioButtonMale);
        rbF = (RadioButton) findViewById(R.id.radioButtonFemale);
        rbU = (RadioButton) findViewById(R.id.radioButtonUndefined);

        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextCognome = (EditText) findViewById(R.id.editTextCognome);
        editTextTelefono = (EditText) findViewById(R.id.editTextTelefono);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfermaPassword = (EditText) findViewById(R.id.editTextConfermaPassword);
        editTextCodiceFiscale = findViewById(R.id.editTextCodiceFiscale);

        textViewBirth = findViewById(R.id.textViewBirth);
        imageViewCalendar = findViewById(R.id.imageViewCalendar);
        selectedBirthDate = Calendar.getInstance();
        selectedBirthDate.set(Calendar.MINUTE,0);
        selectedBirthDate.set(Calendar.HOUR,0);
        selectedBirthDate.set(Calendar.HOUR_OF_DAY,0);
        selectedBirthDate.set(Calendar.SECOND,0);
        selectedBirthDate.set(Calendar.MILLISECOND,0);
        textViewBirth.setText(selectedBirthDate.get(Calendar.DAY_OF_MONTH) + "/" + ((((selectedBirthDate.get(Calendar.MONTH)+1)+"").length() == 1) ? "0" : "") +((selectedBirthDate.get(Calendar.MONTH)+1)+"") + "/" + selectedBirthDate.get(Calendar.YEAR));

        bottoneRegistrazione = findViewById(R.id.buttonRegistrazione);
        textViewEditImage = findViewById(R.id.textViewEditImage);
        textViewRegistrazioneEnte = findViewById(R.id.textViewRegistrazioneEnte);

        progressBar = findViewById(R.id.progressBar);

        setAllFocusChanged();
    }

    /**
     * Add to all the component in the view the focus change listener
     * to reset the background when an error occur
     */
    private void setAllFocusChanged(){
        editTextCognome.setOnFocusChangeListener(this);
        editTextConfermaPassword.setOnFocusChangeListener(this);
        editTextEmail.setOnFocusChangeListener(this);
        editTextNome.setOnFocusChangeListener(this);
        editTextPassword.setOnFocusChangeListener(this);
        editTextTelefono.setOnFocusChangeListener(this);
        editTextCodiceFiscale.setOnFocusChangeListener(this);
    }

    private void clearAllFocus(){
        editTextCognome.clearFocus();
        editTextConfermaPassword.clearFocus();
        editTextEmail.clearFocus();
        editTextNome.clearFocus();
        editTextPassword.clearFocus();
        editTextTelefono.clearFocus();
        editTextCodiceFiscale.clearFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    public void onRegistrationClick(View view) {
        controlloInputUtenteRegistrazione();
    }



    private void controlloInputUtenteRegistrazione() {

        String nome, cognome, dataNascita, genere, telefono, email, password, confermaPassword, codiceFiscale;
        nome = editTextNome.getText().toString();
        cognome = editTextCognome.getText().toString();
        genere = getString(R.string.genereNonDefinito);
        telefono = editTextTelefono.getText().toString();
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        confermaPassword = editTextConfermaPassword.getText().toString();
        codiceFiscale = editTextCodiceFiscale.getText().toString();


        if( validazioneNome(nome) == false || validazioneCognome(cognome) == false || validazioneCodiceFiscale(codiceFiscale) == false ||
            validazioneTelefono(telefono) == false || selectedBirthDate == null
            || validazioneEmail(email) == false || validazionePassword(password) == false || !password.equals(confermaPassword)
             || password.contains(" ")) {

            if(validazioneNome(nome) == false) {
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCognome(cognome) == false) {
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCodiceFiscale(codiceFiscale) == false) {
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(selectedBirthDate == null){
                Toast.makeText(this, "Data di nascita non selezionata!", Toast.LENGTH_LONG).show();
            }

            if(validazioneTelefono(telefono) == false) {
                editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneEmail(email) == false) {
                editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazionePassword(password) == false || password.contains(" ")) {
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(this, getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else if(!password.equals(confermaPassword)){
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(this, getString(R.string.passwordsNotMatching), Toast.LENGTH_LONG).show();
            } else {
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

        }else{

            backgroundTintEditText();
            clearAllFocus();

            if(rbM.isChecked()) genere = Utente.MALE;
            else if(rbF.isChecked()) genere = Utente.FEMALE;
            else if(rbU.isChecked()) genere = Utente.UNDEFINED;

            Utente u = new Utente();
            u.setCognome(cognome);
            u.setNome(nome);

            u.setDataNascita(selectedBirthDate.getTime());
            u.setGenere(genere);

            disableAllComponents();

            //Check that the phone number is not already taken
            GenericUser.isPhoneNumberAlreadyTaken(telefono, closureBool -> {
                if(!closureBool){
                    u.setNumeroDiTelefono(telefono);

                    //Check the fiscal code
                    Utenti.isFiscalCodeAlreadyUsed(codiceFiscale, isUsed -> {
                        if(isUsed){
                            editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                            Toast.makeText(this,R.string.fiscalCodeAlreadyTaken,Toast.LENGTH_LONG).show();
                            enableAllComponents();
                            return;
                        }else{
                            u.setCodiceFiscale(codiceFiscale.toUpperCase());
                            computeRegistrationToServer(u,email,password);
                        }
                    });
                }else{
                    editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    Toast.makeText(this,R.string.phoneNumberAlreadyTaken,Toast.LENGTH_LONG).show();
                    enableAllComponents();
                    return;
                }
            });
        }
    }

    private void computeRegistrationToServer(Utente user, String email, String psw){
        Utenti.createNewUser(user, email, psw, imageSelected, this, closureBool -> {
            if(closureBool){
                //Creating the firebase notification token document associated to the logged in user
                //with the current token.
                Utenti.createMessagingTokenDocument(null);

                Toast.makeText(this, getString(R.string.registrazioneEffettuataRegister), Toast.LENGTH_LONG).show();
                Intent schermataHome = new Intent(getApplicationContext(), HomeActivity.class);
                schermataHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the stack all the previous Activity.
                startActivity(schermataHome);
                finish();
            }else{
                Toast.makeText(this, getString(R.string.registrazioneErrore), Toast.LENGTH_LONG).show();
                AuthHelper.logOut(getApplicationContext());
                enableAllComponents();
            }
        });
    }

    /**
     * Show and manage the results of the DataPickerDialog.
     */
    public void onClickCalendarImageView(View view){

        Calendar c = Calendar.getInstance();
        if(selectedBirthDate != null){
            c = selectedBirthDate;
        }

        picker = new DatePickerDialog(this,new DatePickerDialog.OnDateSetListener(){
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(selectedBirthDate == null) selectedBirthDate = Calendar.getInstance();
                selectedBirthDate.set(year,month,dayOfMonth);

                textViewBirth.setText(dayOfMonth + "/" + ((((month+1)+"").length() == 1) ? "0" : "") +((month+1)+"") + "/" + year);
            }
        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));

        picker.getDatePicker().setMaxDate(new Date().getTime());
        picker.show();
    }

    /**
     * Re-enable the interaction with all the components and hide the progress bar
     */
    private void enableAllComponents(){
        editTextTelefono.setEnabled(true);
        editTextPassword.setEnabled(true);
        editTextNome.setEnabled(true);
        editTextEmail.setEnabled(true);
        imageViewCalendar.setEnabled(true);
        editTextConfermaPassword.setEnabled(true);
        editTextCognome.setEnabled(true);
        rbF.setEnabled(true);
        rbM.setEnabled(true);
        rbU.setEnabled(true);
        bottoneRegistrazione.setEnabled(true);
        textViewEditImage.setEnabled(true);
        textViewRegistrazioneEnte.setEnabled(true);
        editTextCodiceFiscale.setEnabled(true);

        //Hide the progress bar
        AnimationHelper.fadeOut(progressBar,1000);
    }

    /**
     * Disable the interaction with all the components and shows the progress bar
     */
    private void disableAllComponents(){
        editTextTelefono.setEnabled(false);
        editTextPassword.setEnabled(false);
        editTextNome.setEnabled(false);
        editTextEmail.setEnabled(false);
        imageViewCalendar.setEnabled(false);
        editTextConfermaPassword.setEnabled(false);
        editTextCognome.setEnabled(false);
        rbF.setEnabled(false);
        rbM.setEnabled(false);
        rbU.setEnabled(false);
        bottoneRegistrazione.setEnabled(false);
        textViewEditImage.setEnabled(false);
        textViewRegistrazioneEnte.setEnabled(false);
        editTextCodiceFiscale.setEnabled(false);

        //Show the progress bar
        AnimationHelper.fadeIn(progressBar,1000);
    }

    /**
     * Settaggio dell'underline a tutte le editText nel colore grigio.
     */
    public void backgroundTintEditText() {
        editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
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

    public void onClickRegisterEnte(View view) {

        Intent schermataRegistrazioneEnte = new Intent(this, RegisterEnteActivity.class);
        startActivity(schermataRegistrazioneEnte);
    }



    public void onClickPhoto(View view){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // Compress it before it is shown into the imageView
                    imageView.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(),selectedImageUri,imageView));
                    imageSelected = selectedImageUri;
                }
            }
        }
    }
}



