package com.vitandreasorino.savent.RegistrazioniEnte;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vitandreasorino.savent.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.DB.Utenti;


public class FreelanceFragment extends Fragment {


    private static final int RESULT_OK = -1;
    private Button buttonCertificatoPartitaIvaFreelance, buttonRegistrazioneFreelance;

    private EditText editTextPartitaIvaFreelance, editTextNomeFreelance, editTextCognomeFreelance, editTextCodiceFiscaleFreelance,
               editTextResidenzaFreelance, editTextNumeroIscrizioneAlboFreelance, editTextTelefonoFreelance,
               editTextEmailFreelance, editTextPasswordFreelance, editTextConfermaPasswordFreelance;

    TextView textViewLabelCaricamento;

    private Uri pathCertificatoPartitaIva;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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


        buttonCertificatoPartitaIvaFreelance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagePartitaIvaFreelance = new Intent();
                imagePartitaIvaFreelance.setType("image/*");
                imagePartitaIvaFreelance.setAction(Intent.ACTION_GET_CONTENT);

                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imagePartitaIvaFreelance, "Select Picture"), 200);
            }
        });


        buttonRegistrazioneFreelance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controlloInputUtenteRegistrazioneFreelance();
            }

        });

        return view;
    }


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
                editTextPartitaIvaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextPartitaIvaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneNomeFreelance(nomeFreelance) == false) {
                editTextNomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCognomeFreelance(cognomeFreelance) == false) {
                editTextCognomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextCognomeFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneCodiceFiscale(codiceFiscaleFreelance) == false) {
                editTextCodiceFiscaleFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextCodiceFiscaleFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneResidenzaFreelance(residenzaFreelance) == false) {
                editTextResidenzaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextResidenzaFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneNumeroIscrizioneAlbo(numeroIscrizioneAlbo) == false) {
                editTextNumeroIscrizioneAlboFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNumeroIscrizioneAlboFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneTelefonoFreelance(telefonoFreelance) == false) {
                editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextTelefonoFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneEmailFreelance(emailFreelance) == false) {
                editTextEmailFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextEmailFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazionePasswordFreelance(passwordFreelance) == false || passwordFreelance.contains(" ") || !passwordFreelance.equals(confermaPasswordFreelance )) {
                editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(getActivity(), getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else{
                editTextPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPasswordFreelance.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(pathCertificatoPartitaIva == null) {
                textViewLabelCaricamento.setVisibility(View.INVISIBLE);
            }else{
                textViewLabelCaricamento.setVisibility(View.VISIBLE);
            }


            Toast.makeText(getActivity(), getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{

            backgroundTintEditTextFreelanceFragment();
            Toast.makeText(getActivity(), getString(R.string.registrazioneEffettuataRegister), Toast.LENGTH_LONG).show();
        }


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
     * Controllo che il numero di partita Iva rispetti le seguenti caratteristiche:
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
     * Controllo che il nome in input rispetti le seguenti caratteristiche:
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
     * Controllo che il cognome in input rispetti le seguenti caratteristiche:
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
     * Controllo che la residenza dell'azienda sia formattata correttamente
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
     * Controllo che il numero di iscrizione all'albo rispetti le seguenti caratteristiche:
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
     * Controllo che il numero di telefono rispetti le seguenti caratteristiche:
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
     * Controllo che l'email in input rispetti la forma standard delle email.
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
     * Controllo che la password in input rispetti le seguenti caratteristiche:
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    pathCertificatoPartitaIva = selectedImageUri;
                    textViewLabelCaricamento.setVisibility(View.VISIBLE);
                }else{
                    textViewLabelCaricamento.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


}