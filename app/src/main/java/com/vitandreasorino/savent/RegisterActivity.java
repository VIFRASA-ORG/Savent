package com.vitandreasorino.savent;



import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {

    ImageView imageView;
    CheckBox cb1,cb2,cb3;

    EditText editTextNome,editTextCognome,editTextDataNascita,editTextTelefono,
             editTextEmail,editTextPassword,editTextConfermaPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        imageView = (ImageView) findViewById(R.id.imageView);
        cb1 = (CheckBox) findViewById(R.id.checkBox1);
        cb2 = (CheckBox) findViewById(R.id.checkBox2);
        cb3 = (CheckBox) findViewById(R.id.checkBox3);

        editTextNome = (EditText) findViewById(R.id.editTextNome);
        editTextCognome = (EditText) findViewById(R.id.editTextCognome);
        editTextDataNascita = (EditText) findViewById(R.id.editTextDataNascita);
        editTextTelefono = (EditText) findViewById(R.id.editTextTelefono);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextConfermaPassword = (EditText) findViewById(R.id.editTextConfermaPassword);


        /**
         * Metodo utilizzato per gestire la selezione di una singola checkbox per volta.
         */
        cb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    cb2.setChecked(false);
                    cb3.setChecked(false);
                }
            }
        });

        cb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    cb1.setChecked(false);
                    cb3.setChecked(false);
                }
            }
        });

        cb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    cb1.setChecked(false);
                    cb2.setChecked(false);
                }
            }
        });


    }

    public void onRegistrationClick(View view) {

        controlloInputUtenteRegistrazione();
    }



    private void controlloInputUtenteRegistrazione() {

        String nome, cognome, dataNascita, genere, telefono, email, password, confermaPassword;
        nome = editTextNome.getText().toString();
        cognome = editTextCognome.getText().toString();
        dataNascita = editTextDataNascita.getText().toString();
        genere = null;
        telefono = editTextTelefono.getText().toString();
        email = editTextEmail.getText().toString();
        password = editTextPassword.getText().toString();
        confermaPassword = editTextConfermaPassword.getText().toString();


        if( validazioneNome(nome) == false || validazioneCognome(cognome) == false  || validazioneDataNascita(dataNascita) == false ||
            dataNascita.length() < 9 || dataNascita.contains(".") || dataNascita.contains("-") || (validazioneDataNascita(dataNascita) == true && validazioneDataNascitaDue(dataNascita) == false)  ||
           (!cb1.isChecked() && !cb2.isChecked() && !cb3.isChecked()) || validazioneTelefono(telefono) == false
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

            if(validazionePassword(password) == false || password.contains(" ") || !password.equals(confermaPassword )) {
                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(this, getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else{

                editTextPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPassword.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            Toast.makeText(this, getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{

            backgroundTintEditText();

            if(cb1.isChecked()){
                genere = getString(R.string.genereMaschile);
            }
            if(cb2.isChecked()){
                genere = getString(R.string.genereFemminile);
            }
            if(cb3.isChecked()){
                genere = getString(R.string.genereNonDefinito);
            }

            Toast.makeText(this, getString(R.string.registrazioneEffettuataRegister), Toast.LENGTH_LONG).show();

        }
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
                    imageView.setImageURI(selectedImageUri);
                }
            }
        }
    }




}



