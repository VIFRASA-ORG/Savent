package com.vitandreasorino.savent.Registrazioni;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.vitandreasorino.savent.HomeActivity;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Registrazioni.RegistrazioniEnte.RegisterEnteActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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

    EditText editTextNome,editTextCognome,editTextDataNascita,editTextTelefono,
             editTextEmail,editTextPassword,editTextConfermaPassword;

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
        editTextDataNascita = (EditText) findViewById(R.id.editTextDataNascita);
        editTextTelefono = (EditText) findViewById(R.id.editTextTelefono);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfermaPassword = (EditText) findViewById(R.id.editTextConfermaPassword);

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
        editTextDataNascita.setOnFocusChangeListener(this);
        editTextEmail.setOnFocusChangeListener(this);
        editTextNome.setOnFocusChangeListener(this);
        editTextPassword.setOnFocusChangeListener(this);
        editTextTelefono.setOnFocusChangeListener(this);
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

        String nome, cognome, dataNascita, genere, telefono, email, password, confermaPassword;
        nome = editTextNome.getText().toString();
        cognome = editTextCognome.getText().toString();
        dataNascita = editTextDataNascita.getText().toString();
        genere = getString(R.string.genereNonDefinito);
        telefono = editTextTelefono.getText().toString();
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        confermaPassword = editTextConfermaPassword.getText().toString();


        if( validazioneNome(nome) == false || validazioneCognome(cognome) == false  || validazioneDataNascita(dataNascita) == false ||
            dataNascita.length() < 9 || dataNascita.contains(".") || dataNascita.contains("-") || (validazioneDataNascita(dataNascita) == true && validazioneDataNascitaDue(dataNascita) == false)  ||
            validazioneTelefono(telefono) == false
            ||validazioneEmail(email) == false || validazionePassword(password) == false || !password.equals(confermaPassword)
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

            if(validazioneDataNascita(dataNascita) == false || dataNascita.length() < 9 ||
                    dataNascita.contains(".") || dataNascita.contains("-") || (validazioneDataNascita(dataNascita) == true &&
                    validazioneDataNascitaDue(dataNascita) == false) ) {
                editTextDataNascita.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else{
                editTextDataNascita.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
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

            if(rbM.isChecked()) genere = Utente.MALE;
            else if(rbF.isChecked()) genere = Utente.FEMALE;
            else if(rbU.isChecked()) genere = Utente.UNDEFINED;

            Utente u = new Utente();
            u.setCognome(cognome);
            u.setNome(nome);

            //Trying to convert the string to a Date object
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date = format.parse(dataNascita);
                u.setDataNascita(date);
            } catch (ParseException e) {
                editTextDataNascita.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                return;
            }
            u.setGenere(genere);

            disableAllComponents();

            //Check that the phone number is not already taken
            GenericUser.isPhoneNumberAlreadyTaken(telefono, closureBool -> {
                if(!closureBool){
                    u.setNumeroDiTelefono(telefono);
                    computeRegistrationToServer(u,email,password);
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
                Toast.makeText(this, getString(R.string.registrazioneEffettuataRegister), Toast.LENGTH_LONG).show();
                Intent schermataHome = new Intent(getApplicationContext(), HomeActivity.class);
                schermataHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the stack all the previous Activity.
                startActivity(schermataHome);
                finish();
            }else{
                Toast.makeText(this, getString(R.string.registrazioneErrore), Toast.LENGTH_LONG).show();
                AuthHelper.logOut();
                enableAllComponents();
            }
        });
    }

    /**
     * Re-enable the interaction with all the components and hide the progress bar
     */
    private void enableAllComponents(){
        editTextTelefono.setEnabled(true);
        editTextPassword.setEnabled(true);
        editTextNome.setEnabled(true);
        editTextEmail.setEnabled(true);
        editTextDataNascita.setEnabled(true);
        editTextConfermaPassword.setEnabled(true);
        editTextCognome.setEnabled(true);
        rbF.setEnabled(true);
        rbM.setEnabled(true);
        rbU.setEnabled(true);
        bottoneRegistrazione.setEnabled(true);
        textViewEditImage.setEnabled(true);
        textViewRegistrazioneEnte.setEnabled(true);

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
        editTextDataNascita.setEnabled(false);
        editTextConfermaPassword.setEnabled(false);
        editTextCognome.setEnabled(false);
        rbF.setEnabled(false);
        rbM.setEnabled(false);
        rbU.setEnabled(false);
        bottoneRegistrazione.setEnabled(false);
        textViewEditImage.setEnabled(false);
        textViewRegistrazioneEnte.setEnabled(false);

        //Show the progress bar
        AnimationHelper.fadeIn(progressBar,1000);
    }

    /**
     * Settaggio dell'underline a tutte le editText nel colore grigio.
     */
    public void backgroundTintEditText() {
        editTextNome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCognome.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextDataNascita.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
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
     * Controllo che la data di nascita in input rispetti i seguenti formati
     * gg/mm/aaaa
     * gg-mm-aaaa
     * gg.mm.aaaa
     * @param controlloDataNascita stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneDataNascita(String controlloDataNascita) {

        if(controlloDataNascita == null )  {
            return false;
        }

        Pattern p = Pattern.compile("^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[13-9]|1[0-2])\\2))" +
                                    "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?" +
                                    "(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?" +
                                    "[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$");
        Matcher m = p.matcher(controlloDataNascita);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Metodo per effettuare la convalida della data inserita da parte dell'utente, nello specifico
     * si controlla se l'anno inserito dall'utente rispetta la formattazione prevista "aaaa" e inoltre
     * si controlla che l'anno inserito dall'utente è più piccolo dell'anno attuale.
     *
     * @param controlloDataNascita stringa da controllare
     * @return false se il valore inserito dall'utente è incorretto, altrimenti true
     */
    public boolean validazioneDataNascitaDue(String controlloDataNascita) {

        // Ottengo inizialmente la data del sistema e la formatto nel seguente modo "dd-mm-yyyy"
        Calendar calendario = Calendar.getInstance();
        SimpleDateFormat formattazione = new SimpleDateFormat("dd-MM-yyyy");

        // la data formattata l'assegno ad un oggetto String
        String dataAttuale = null;
        dataAttuale = formattazione.format(calendario.getTime());

        String definizioneDataAttuale = "";
        String definizioneDataInserita = "";
        int valoreRisultanteInserito = 0, valoreRisultanteCorrente = 0;

        // Memorizzo in due oggetti scritta creati i due anni, rispettivamente il primo
        // coincide con l'anno attuale fornito dal sistema, il secondo coincide con l'inserimento dell'utente
        // infine li metto a confronto per stabilire se l'anno corrente è maggiore dell'anno inserito.
        definizioneDataAttuale += dataAttuale.charAt(6);
        definizioneDataAttuale += dataAttuale.charAt(7);
        definizioneDataAttuale += dataAttuale.charAt(8);
        definizioneDataAttuale += dataAttuale.charAt(9);
        valoreRisultanteCorrente = Integer.parseInt(definizioneDataAttuale);


        definizioneDataInserita += controlloDataNascita.charAt(6);
        definizioneDataInserita += controlloDataNascita.charAt(7);
        definizioneDataInserita += controlloDataNascita.charAt(8);
        definizioneDataInserita += controlloDataNascita.charAt(9);
        valoreRisultanteInserito = Integer.parseInt(definizioneDataInserita);


        if (valoreRisultanteCorrente <= valoreRisultanteInserito) {
            return false;
        }

        return true;
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



