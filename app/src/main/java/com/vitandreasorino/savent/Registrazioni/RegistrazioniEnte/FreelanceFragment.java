package com.vitandreasorino.savent.Registrazioni.RegistrazioniEnte;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vitandreasorino.savent.LogSingInActivity;
import com.vitandreasorino.savent.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.DAO.Enti;
import Model.DAO.GenericUser;
import Model.POJO.Ente;


public class FreelanceFragment extends Fragment implements View.OnFocusChangeListener {


    private static final int RESULT_OK = -1;
    private Button buttonCertificatoPartitaIvaFreelance, buttonRegistrazioneFreelance;

    private EditText editTextPartitaIvaFreelance, editTextNomeFreelance, editTextCognomeFreelance, editTextCodiceFiscaleFreelance,
               editTextResidenzaFreelance, editTextNumeroIscrizioneAlboFreelance, editTextTelefonoFreelance,
               editTextEmailFreelance, editTextPasswordFreelance, editTextConfermaPasswordFreelance;

    TextView textViewLabelCaricamento;

    private Uri pathCertificatoPartitaIva=null;
    private ProgressBar progressBar=null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Definizione e inizializzazione del fragment
        View view = inflater.inflate(R.layout.fragment_freelance, container, false);

        buttonCertificatoPartitaIvaFreelance = (Button) view.findViewById(R.id.buttonCertificatoPartitaIvaFreelance);
        buttonRegistrazioneFreelance = (Button) view.findViewById(R.id.buttonRegistrazioneFreelance);

        editTextPartitaIvaFreelance = (EditText) view.findViewById(R.id.editTextPartitaIvaFreelance);
        editTextNomeFreelance = (EditText) view.findViewById(R.id.editTextNomeFreelance);
        editTextCognomeFreelance = (EditText) view.findViewById(R.id.editTextCognomeFreelance);
        editTextCodiceFiscaleFreelance = (EditText) view.findViewById(R.id.editTextCodiceFiscaleFreelance);
        editTextResidenzaFreelance = (EditText) view.findViewById(R.id.editTextResidenzaFreelance);
        editTextNumeroIscrizioneAlboFreelance = (EditText) view.findViewById(R.id.editTextNumeroIscrizioneAlboFreelance);
        editTextTelefonoFreelance = (EditText) view.findViewById(R.id.editTextTelefonoFreelance);
        editTextEmailFreelance = (EditText) view.findViewById(R.id.editTextEmailFreelance);
        editTextPasswordFreelance = (EditText) view.findViewById(R.id.editTextPasswordFreelance);
        editTextConfermaPasswordFreelance = (EditText) view.findViewById(R.id.editTextConfermaPasswordFreelance);

        textViewLabelCaricamento = (TextView) view.findViewById(R.id.textViewLabelCaricamento);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);


        buttonCertificatoPartitaIvaFreelance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagePartitaIvaFreelance = new Intent();
                imagePartitaIvaFreelance.setType("image/*");
                imagePartitaIvaFreelance.setAction(Intent.ACTION_GET_CONTENT);

                //Si passa la costante per il confronto con il requestCode ritornato
                startActivityForResult(Intent.createChooser(imagePartitaIvaFreelance, "Select Picture"), 200);
            }
        });


        buttonRegistrazioneFreelance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAllFocus();
                controlloInputUtenteRegistrazioneFreelance();
            }

        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAllFocusChanged();
    }

    /**
     * Si aggiunge a tutti i componenti nella vista il listener di cambio focus
     * per ripristinare lo sfondo quando si verifica un errore
     */
    private void setAllFocusChanged(){
        editTextPartitaIvaFreelance.setOnFocusChangeListener(this);
        editTextNomeFreelance.setOnFocusChangeListener(this);
        editTextCognomeFreelance.setOnFocusChangeListener(this);
        editTextCodiceFiscaleFreelance.setOnFocusChangeListener(this);
        editTextResidenzaFreelance.setOnFocusChangeListener(this);
        editTextNumeroIscrizioneAlboFreelance.setOnFocusChangeListener(this);
        editTextTelefonoFreelance.setOnFocusChangeListener(this);
        editTextEmailFreelance.setOnFocusChangeListener(this);
        editTextPasswordFreelance.setOnFocusChangeListener(this);
        editTextConfermaPasswordFreelance.setOnFocusChangeListener(this);
    }

    /**
     * Si rimuovono i focus da tutti i componenti
     */
    private void removeAllFocus(){
        editTextPartitaIvaFreelance.clearFocus();
        editTextNomeFreelance.clearFocus();
        editTextCognomeFreelance.clearFocus();
        editTextCodiceFiscaleFreelance.clearFocus();
        editTextResidenzaFreelance.clearFocus();
        editTextNumeroIscrizioneAlboFreelance.clearFocus();
        editTextTelefonoFreelance.clearFocus();
        editTextEmailFreelance.clearFocus();
        editTextPasswordFreelance.clearFocus();
        editTextConfermaPasswordFreelance.clearFocus();
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
     * Si controllano i dati inseriti in input per la registrazione del libero professionista
     */
    private void controlloInputUtenteRegistrazioneFreelance() {

        String numeroPartitaIva, nomeFreelance, cognomeFreelance, codiceFiscaleFreelance, residenzaFreelance,
               numeroIscrizioneAlbo, telefonoFreelance, emailFreelance, passwordFreelance, confermaPasswordFreelance;

        numeroPartitaIva = editTextPartitaIvaFreelance.getText().toString();
        nomeFreelance = editTextNomeFreelance.getText().toString();
        cognomeFreelance = editTextCognomeFreelance.getText().toString();
        codiceFiscaleFreelance = editTextCodiceFiscaleFreelance.getText().toString();
        residenzaFreelance = editTextResidenzaFreelance.getText().toString();
        numeroIscrizioneAlbo = editTextNumeroIscrizioneAlboFreelance.getText().toString();
        telefonoFreelance = editTextTelefonoFreelance.getText().toString();
        emailFreelance = editTextEmailFreelance.getText().toString();
        passwordFreelance = editTextPasswordFreelance.getText().toString();
        confermaPasswordFreelance = editTextConfermaPasswordFreelance.getText().toString();


        if(validazionePartitaIva(numeroPartitaIva) == false || validazioneNomeFreelance(nomeFreelance) == false ||
           validazioneCognomeFreelance(cognomeFreelance) == false || validazioneCodiceFiscale(codiceFiscaleFreelance) == false ||
           validazioneResidenzaFreelance(residenzaFreelance) == false || validazioneNumeroIscrizioneAlbo(numeroIscrizioneAlbo) == false ||
           validazioneTelefonoFreelance(telefonoFreelance) == false || validazioneEmailFreelance(emailFreelance) == false ||
           validazionePasswordFreelance(passwordFreelance) == false || !passwordFreelance.equals(confermaPasswordFreelance) ||
           passwordFreelance.contains(" ") || pathCertificatoPartitaIva == null) {


            if(validazionePartitaIva(numeroPartitaIva) == false) {
                //Se la partita iva inserita è errata, il color state list cambia colore e diventa rosso
                editTextPartitaIvaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se la partita iva inserita è corretta, il color state list rimane grigio
                editTextPartitaIvaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneNomeFreelance(nomeFreelance) == false) {
                //Se il nome del libero professionista inserito è errato, il color state list cambia colore e diventa rosso
                editTextNomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il nome del libero professionista inserito è corretto, il color state list rimane grigio
                editTextNomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCognomeFreelance(cognomeFreelance) == false) {
                //Se il cognome del libero professionista inserito è errato, il color state list cambia colore e diventa rosso
                editTextCognomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il cognome del libero professionista inserito è corretto, il color state list rimane grigio
                editTextCognomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCodiceFiscale(codiceFiscaleFreelance) == false) {
                //Se il cf inserito è errato, il color state list cambia colore e diventa rosso
                editTextCodiceFiscaleFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se  il cf inserito è corretto, il color state list rimane grigio
                editTextCodiceFiscaleFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneResidenzaFreelance(residenzaFreelance) == false) {
                //Se la residenza inserita è errata, il color state list cambia colore e diventa rosso
                editTextResidenzaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se la residenza inserita è corretta, il color state list rimane grigio
                editTextResidenzaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneNumeroIscrizioneAlbo(numeroIscrizioneAlbo) == false) {
                //Se il numero di iscrizione all'albo inserito è errato, il color state list cambia colore e diventa rosso
                editTextNumeroIscrizioneAlboFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il numero di iscrizione all'albo inserito è corretto, il color state list rimane grigio
                editTextNumeroIscrizioneAlboFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneTelefonoFreelance(telefonoFreelance) == false) {
                //Se il numero di telefono inserito è errato, il color state list cambia colore e diventa rosso
                editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se il numero di telefono inserito è corretto, il color state list rimane grigio
                editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneEmailFreelance(emailFreelance) == false) {
                //Se l'email inserita è errata, il color state list cambia colore e diventa rosso
                editTextEmailFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                //Se  l'email inserita è corretta, il color state list rimane grigio
                editTextEmailFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            //Dopo si controlla la password inserita se è corretta e se non contiene spazi
            if(validazionePasswordFreelance(passwordFreelance) == false || passwordFreelance.contains(" ") ) {
                //Se la password inserita è errata segna il color state list cambia colore e diventa rosso
                editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                //Comunica i caratteri da inserire per una password corretta
                Toast.makeText(getActivity(), getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
                return;
            }else if(!passwordFreelance.equals(confermaPasswordFreelance )){
                //Si segna il color state list di rosso se si sbaglia a reinserire la conferma della password
                editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                //Comunica all'utente che le due password non corrispondono
                Toast.makeText(getActivity(), getString(R.string.passwordsNotMatching), Toast.LENGTH_LONG).show();
                return;
            }else{
                //altrimenti si segna il color state list di grigio se la password e la riconferma della sua password sono corrette
                editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            //Se il certificato per la partita iva non è stato selezionato comunica all'utente che deve inserirlo
            if(pathCertificatoPartitaIva == null) {
                textViewLabelCaricamento.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(),R.string.errorPIVA,Toast.LENGTH_LONG).show();
            }else{
                textViewLabelCaricamento.setVisibility(View.VISIBLE);
            }
        }else{

            disableAllComponents();

            backgroundTintEditTextFreelanceFragment();
            Ente e = new Ente();
            e.setNomeLP(nomeFreelance);
            e.setCognomeLP(cognomeFreelance);
            e.setPartitaIva(numeroPartitaIva);
            e.setCodiceFiscaleLP(codiceFiscaleFreelance);
            e.setResidenzaLP(residenzaFreelance);
            e.setNumeroIscrizioneAlboLP(numeroIscrizioneAlbo);
            e.setCertificatoPIVA(pathCertificatoPartitaIva);

            //Verifica se il numero di telefono inserito è già stato inserito nel database
            GenericUser.isPhoneNumberAlreadyTaken(telefonoFreelance,closureBool -> {
                if(!closureBool){
                    e.setNumeroTelefono(telefonoFreelance);
                    computeRegistrationToServer(e,emailFreelance,passwordFreelance);
                }else{
                    editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    Toast.makeText(getActivity(),R.string.phoneNumberAlreadyTaken,Toast.LENGTH_LONG).show();
                    enableAllComponents();
                    return;
                }
            });
        }
    }

    /**
     * Si passano e si caricano i dati inseriti sul server
     * @param ente : si riferisce all'ente in questione
     * @param email : stringa che indica l'email di questo nuovo ente
     * @param password : stringa che indica la password di questo nuovo ente
     */
    private void computeRegistrationToServer(Ente ente, String email, String password){
        Enti.createNewEnteLiberoProfessionista(ente,email,password,closureBool ->{
            if(closureBool){
                //Se la creazione dell'ente va a buon fine, esegui il logout dall'account perché
                //deve essere abilitato dal fornitore del servizio dopo aver verificato tutte le informazioni

                AuthHelper.logOut(getContext());
                //Viene comunicato all'utente che la creazione dell'ente (libero professionista) è andata a buon fine
                Toast.makeText(getActivity(),R.string.enteCreated,Toast.LENGTH_LONG).show();

                //Ritorna alla pagina principale del login/Sign In activity
                Intent firstPage = new Intent(getActivity(), LogSingInActivity.class);
                //Si rimuovono dallo stack tutte le precedenti attività
                firstPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(firstPage);
                getActivity().finish();
            }else{
                //altrimenti si comunica che la registrazione dell'ente(libero professionista) è fallita
                Toast.makeText(getActivity(), getString(R.string.registrazioneErrore), Toast.LENGTH_LONG).show();
                AuthHelper.logOut(getContext());
                enableAllComponents();
            }
        });
    }

    /**
     * Si disabilitano tutte le componenti presenti nel layout
     */
    private void disableAllComponents(){
        editTextPartitaIvaFreelance.setEnabled(false);
        editTextNomeFreelance.setEnabled(false);
        editTextCognomeFreelance.setEnabled(false);
        editTextCodiceFiscaleFreelance.setEnabled(false);
        editTextResidenzaFreelance.setEnabled(false);
        editTextNumeroIscrizioneAlboFreelance.setEnabled(false);
        editTextTelefonoFreelance.setEnabled(false);
        editTextEmailFreelance.setEnabled(false);
        editTextPasswordFreelance.setEnabled(false);
        editTextConfermaPasswordFreelance.setEnabled(false);

        buttonCertificatoPartitaIvaFreelance.setEnabled(false);
        buttonRegistrazioneFreelance.setEnabled(false);

        AnimationHelper.fadeIn(progressBar,1000);
    }

    /**
     * Si abilitano tutte le componenti presenti nel layout
     */
    private void enableAllComponents(){
        editTextPartitaIvaFreelance.setEnabled(true);
        editTextNomeFreelance.setEnabled(true);
        editTextCognomeFreelance.setEnabled(true);
        editTextCodiceFiscaleFreelance.setEnabled(true);
        editTextResidenzaFreelance.setEnabled(true);
        editTextNumeroIscrizioneAlboFreelance.setEnabled(true);
        editTextTelefonoFreelance.setEnabled(true);
        editTextEmailFreelance.setEnabled(true);
        editTextPasswordFreelance.setEnabled(true);
        editTextConfermaPasswordFreelance.setEnabled(true);

        buttonCertificatoPartitaIvaFreelance.setEnabled(true);
        buttonRegistrazioneFreelance.setEnabled(true);

        AnimationHelper.fadeOut(progressBar,1000);
    }


    /**
     * Settaggio dell'underline a tutte le editText nel colore grigio.
     */
    public void backgroundTintEditTextFreelanceFragment() {
        editTextPartitaIvaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCognomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextCodiceFiscaleFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextResidenzaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNumeroIscrizioneAlboFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEmailFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));

        if(pathCertificatoPartitaIva != null) {
            textViewLabelCaricamento.setVisibility(View.VISIBLE);
        }

    }


    /**
     * Controlla che il numero di partita Iva rispetti le seguenti caratteristiche:
     * lunghezza di 11, consentiti solo caratteri numerici.
     * @param numeroPartitaIva stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazionePartitaIva(String numeroPartitaIva) {

        if(numeroPartitaIva == null) {
            return false;
        }

        Pattern p = Pattern.compile("^[0-9]{11}$");
        Matcher m = p.matcher(numeroPartitaIva);
        boolean matchTrovato = m.matches();

        return matchTrovato ;
    }


    /**
     * Controlla che il nome in input rispetti le seguenti caratteristiche:
     * stringa non vuota, lunghezza compresa tra 3 e 15 caratteri,
     * solo caratteri letterali, stringa priva di spazi.
     * @param controlloNomeFreelance stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNomeFreelance(String controlloNomeFreelance) {

        if(controlloNomeFreelance == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-z]{3,15}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloNomeFreelance);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Controlla che il cognome in input rispetti le seguenti caratteristiche:
     * stringa non vuota, lunghezza compresa tra 3 e 15 caratteri,
     * solo caratteri letterali, stringa priva di spazi.
     * @param controlloCognomeFreelance stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneCognomeFreelance(String controlloCognomeFreelance) {

        if(controlloCognomeFreelance == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-z]{3,15}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloCognomeFreelance);
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
     * Controlla che la residenza dell'azienda sia formattata correttamente
     * Va inserito prima il numero civico e poi successivamente la via
     * @param controlloResidenzaFreelance stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneResidenzaFreelance(String controlloResidenzaFreelance) {

        if(controlloResidenzaFreelance == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^\\d{1}[a-z-A-Z\\ \\d#-.\\/'()]+$");
        Matcher m = p.matcher(controlloResidenzaFreelance);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Controlla che il numero di iscrizione all'albo rispetti le seguenti caratteristiche:
     * lunghezza compresa tra 1 e 8, consentiti solo caratteri numerici
     * @param controlloNumeroIscrizioneAlbo stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNumeroIscrizioneAlbo(String controlloNumeroIscrizioneAlbo) {

        if(controlloNumeroIscrizioneAlbo == null) {
            return false;
        }

        Pattern p = Pattern.compile("^[0-9]{1,8}$");
        Matcher m = p.matcher(controlloNumeroIscrizioneAlbo);
        boolean matchTrovato = m.matches();

        return matchTrovato ;
    }


    /**
     * Controlla che il numero di telefono rispetti le seguenti caratteristiche:
     * lunghezza compresa tra 9 e 11, consentiti solo caratteri numerici
     * @param controlloTelefono stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneTelefonoFreelance(String controlloTelefono) {

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
    public boolean validazioneEmailFreelance(String controlloEmail) {

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
    public boolean validazionePasswordFreelance(String controlloPassword) {

        if(controlloPassword == null)  {
            return false;
        }

        Pattern p = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})");
        Matcher m = p.matcher(controlloPassword);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative alla modifica dell'immagine profilo dell'azienda.
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati della modifica al chiamante.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            //Si confronta il resultCode con la costante SELECT_PICTURE
            if (requestCode == 200) {
                //Si ottiene l'url dell'immagine del profilo dell'ente dai dati
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Si modifica l'anteprima dell'immagine nel layout
                    pathCertificatoPartitaIva = selectedImageUri;
                    textViewLabelCaricamento.setVisibility(View.VISIBLE);
                }else{
                    textViewLabelCaricamento.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}