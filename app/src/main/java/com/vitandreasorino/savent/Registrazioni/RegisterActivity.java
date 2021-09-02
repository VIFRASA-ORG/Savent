package com.vitandreasorino.savent.Registrazioni;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import Model.DAO.GenericUser;
import Model.DAO.Utenti;
import Model.POJO.Utente;


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
     * Si aggiunge a tutti i componenti nella vista il listener di cambio focus
     * per ripristinare lo sfondo quando si verifica un errore
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

    /**
     * Si ripuliscono i focus da tutti i componenti
     */
    private void clearAllFocus(){
        editTextCognome.clearFocus();
        editTextConfermaPassword.clearFocus();
        editTextEmail.clearFocus();
        editTextNome.clearFocus();
        editTextPassword.clearFocus();
        editTextTelefono.clearFocus();
        editTextCodiceFiscale.clearFocus();
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
     * Viene richiamto il controllo dell'input per la registrazione
     * @param view
     */
    public void onRegistrationClick(View view) {
        controlloInputUtenteRegistrazione();
    }

    /**
     * Si controllano i dati inseriti in input per la registrazione di un utente
     */
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

        //Se i valori inseriti per la registrazione dell'utente non sono corretti e/o contengono spazi
        if( validazioneNome(nome) == false || validazioneCognome(cognome) == false || validazioneCodiceFiscale(codiceFiscale) == false ||
            validazioneTelefono(telefono) == false || selectedBirthDate == null
            || validazioneEmail(email) == false || validazionePassword(password) == false || !password.equals(confermaPassword)
             || password.contains(" ")) {

            if(validazioneNome(nome) == false) {
                //Se il nome inserito è errato, il color state list cambia colore e diventa rosso
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il nome inserito è corretto, il color state list rimane grigio
                editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCognome(cognome) == false) {
                //Se il cognome inserito è errato, il color state list cambia colore e diventa rosso
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il cognome inserito è corretto, il color state list rimane grigio
                editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCodiceFiscale(codiceFiscale) == false) {
                //Se il cf inserito è errato, il color state list cambia colore e diventa rosso
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il cf inserito è corretto, il color state list rimane grigio
                editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(selectedBirthDate == null){
                //Se la data non è stata inserita, comunica all'utente che la data non è stata inserita
                Toast.makeText(this, "Data di nascita non selezionata!", Toast.LENGTH_LONG).show();
            }

            if(validazioneTelefono(telefono) == false) {
                //Se il numero di telefono inserito è errato, il color state list cambia colore e diventa rosso
                editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                //Se il numero di telefono inserito è corretto, il color state list rimane grigio
                editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneEmail(email) == false) {
                //Se il'email inserita è errata, il color state list cambia colore e diventa rosso
                editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                //Se l'email inserita è corretta, il color state list rimane grigio
                editTextEmail.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            //Dopo si controlla la password inserita se è corretta e se non contiene spazi
            if(validazionePassword(password) == false || password.contains(" ")) {
                //Se la password inserita è errata segna il color state list cambia colore e diventa rosso
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                //Comunica i caratteri da inserire per una password corretta
                Toast.makeText(this, getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else if(!password.equals(confermaPassword)){
                //Si segna il color state list di rosso se si sbaglia a reinserire la conferma della password
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                //Comunica all'utente che le due password non corrispondono
                Toast.makeText(this, getString(R.string.passwordsNotMatching), Toast.LENGTH_LONG).show();
            } else {
                //altrimenti si segna il color state list di grigio se la password e la riconferma della sua password sono corrette
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

            //Verifica se il numero di telefono inserito è già presente nel database
            GenericUser.isPhoneNumberAlreadyTaken(telefono, closureBool -> {
                if(!closureBool){
                    u.setNumeroDiTelefono(telefono);

                    //Verifica se codice fiscale inserito dall'utente sia stato già immesso nel database
                    Utenti.isFiscalCodeAlreadyUsed(codiceFiscale, isUsed -> {
                        if(isUsed){
                            editTextCodiceFiscale.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                            //Comunica all'utente che il cf è già stato utilizzato
                            Toast.makeText(this,R.string.fiscalCodeAlreadyTaken,Toast.LENGTH_LONG).show();
                            enableAllComponents();
                            return;
                        }else{
                            //altrimenti si accetta il cf inserito se non è presente nel db
                            u.setCodiceFiscale(codiceFiscale.toUpperCase());
                            computeRegistrationToServer(u,email,password);
                        }
                    });
                }else{
                    //altrimenti si comunics all'utente che il numero di telefono è già stato utilizzato
                    editTextTelefono.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    Toast.makeText(this,R.string.phoneNumberAlreadyTaken,Toast.LENGTH_LONG).show();
                    enableAllComponents();
                    return;
                }
            });
        }
    }

    /**
     * Si passano e si caricano i dati inseriti sul server
     * @param user : si riferisce all'utente in questione
     * @param email : stringa che indica l'email di questo nuovo ente
     * @param psw : stringa che indica la password di questo nuovo ente
     */
    private void computeRegistrationToServer(Utente user, String email, String psw){
        Utenti.createNewUser(user, email, psw, imageSelected, closureBool -> {
            if(closureBool){
                //Creazione del documento token di notifica firebase associato all'utente connesso
                //con il token corrente.
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
     * Vengono mostrati e gestiti i risultati del DataPickerDialog
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
     * Si riattiva l'interazione con tutti i componenti e nasconde la progress bar
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

        //Nasconde la progress bar
        AnimationHelper.fadeOut(progressBar,1000);
    }

    /**
     * Si disabilita l'interazione con tutti i componenti e mostra la progress bar
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

        //Mostra la progress bar
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
     * Controlla che l'email in input rispetti la forma standard delle email.
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
     * Metodo che serve per definire l'intent che serve per richiamare la parte dedicata alla registrazione degli enti
     * @param view : la nuova vista da vedere
     */
    public void onClickRegisterEnte(View view) {

        Intent schermataRegistrazioneEnte = new Intent(this, RegisterEnteActivity.class);
        startActivity(schermataRegistrazioneEnte);
    }

    /**
     * Metodo che serve per impostare l'immagine del profilo dell'utente
     * @param view : la nuova vista con l'immagine del profilo aggiornata
     */
    public void onClickPhoto(View view){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        //Si passa la costante per il confronto con il requestCode ritornato come valore
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative alla modifica dell'immagine profilo dell'utente.
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati della modifica al chiamante.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Si confronta i resultCode con la costante SELECT_PICTURE
            if (requestCode == 200) {
                //Si ottiene l'url dell'immagine dai dati
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Si modifica l'anteprima dell'immagine nel layout
                    //Si comprime tutto prima che sia mostrata questa immagine nel componente imageView
                    imageView.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(),selectedImageUri,imageView));
                    imageSelected = selectedImageUri;
                }
            }
        }
    }
}



