package com.vitandreasorino.savent.RegistrazioniEnte;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vitandreasorino.savent.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.Closures.ClosureList;
import Model.DB.Gruppi;
import Model.Pojo.Gruppo;


public class CompanyFragment extends Fragment {

    Uri pathCertificatoPartitaIvaDitta;
    Uri pathVisuraCameraleDitta;

    private static final int RESULT_OK = -1;
    private Button buttonCertificatoPartitaIvaDitta, buttonVisuraCameraleDitta, buttonRegistrazioneDitta;

    EditText editTextPartitaIvaDitta,  editTextNomeDitta,editTextDomicilioFiscaleDitta, editTextSedeDitta, editTextTelefonoDitta,
            editTextEmailDitta, editTextPasswordDitta, editTextConfermaPasswordDitta;


    TextView textViewLabelCaricamentoCertificatoDitta, textViewLabelCaricamentoVisuraDitta;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_company, container, false);

        buttonCertificatoPartitaIvaDitta = (Button) view.findViewById(R.id.buttonCertificatoPartitaIvaDitta);
        buttonVisuraCameraleDitta  = (Button) view.findViewById(R.id.buttonVisuraCameraleDitta);
        buttonRegistrazioneDitta = (Button) view.findViewById(R.id.buttonRegistrazioneDitta);

        editTextPartitaIvaDitta = (EditText) view.findViewById(R.id.editTextPartitaIvaDitta);
        editTextNomeDitta = (EditText) view.findViewById(R.id.editTextNomeDitta);
        editTextDomicilioFiscaleDitta = (EditText) view.findViewById(R.id.editTextDomicilioFiscaleDitta);
        editTextSedeDitta = (EditText) view.findViewById(R.id.editTextSedeDitta);
        editTextTelefonoDitta = (EditText) view.findViewById(R.id.editTextTelefonoDitta);
        editTextEmailDitta = (EditText) view.findViewById(R.id.editTextEmailDitta);
        editTextPasswordDitta = (EditText) view.findViewById(R.id.editTextPasswordDitta);
        editTextConfermaPasswordDitta = (EditText) view.findViewById(R.id.editTextConfermaPasswordDitta);


        textViewLabelCaricamentoCertificatoDitta = (TextView) view.findViewById(R.id.textViewLabelCaricamentoCertificatoDitta);
        textViewLabelCaricamentoVisuraDitta = (TextView) view.findViewById(R.id.textViewLabelCaricamentoVisuraDitta);

        buttonCertificatoPartitaIvaDitta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imagePartitaIvaDitta = new Intent();
                imagePartitaIvaDitta.setType("image/*");
                imagePartitaIvaDitta.setAction(Intent.ACTION_GET_CONTENT);

                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imagePartitaIvaDitta, "Select Picture"), 201);
            }
        });

        buttonVisuraCameraleDitta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageVisuraCameraleDitta = new Intent();
                imageVisuraCameraleDitta.setType("image/*");
                imageVisuraCameraleDitta.setAction(Intent.ACTION_GET_CONTENT);

                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imageVisuraCameraleDitta, "Select Picture"), 202);
            }
        });

        buttonRegistrazioneDitta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlloInputUtenteRegistrazioneDitta();
            }
        });


        return view;
    }


    private void controlloInputUtenteRegistrazioneDitta() {

        String numeroPartitaIvaDitta, nomeDitta,domicilioFiscaleDitta, sedeDitta, numeroTelefonoDitta,
               emailDitta, passwordDitta, confermaPasswordDitta;

        numeroPartitaIvaDitta = editTextPartitaIvaDitta.getText().toString();
        nomeDitta = editTextNomeDitta.getText().toString();
        domicilioFiscaleDitta = editTextDomicilioFiscaleDitta.getText().toString();
        sedeDitta = editTextSedeDitta.getText().toString();
        numeroTelefonoDitta = editTextTelefonoDitta.getText().toString();
        emailDitta = editTextEmailDitta.getText().toString();
        passwordDitta = editTextPasswordDitta.getText().toString();
        confermaPasswordDitta = editTextConfermaPasswordDitta.getText().toString();

        if(validazionePartitaIvaDitta(numeroPartitaIvaDitta) == false || validazioneNomeDitta(nomeDitta) == false ||
           validazioneDomicilioFiscale(domicilioFiscaleDitta) == false ||
           validazioneSedeDitta(sedeDitta) == false || validazioneTelefonoDitta(numeroTelefonoDitta) == false ||
           validazioneEmailDitta(emailDitta) == false || validazionePasswordDitta(passwordDitta) == false ||
           !passwordDitta.equals(confermaPasswordDitta) || passwordDitta.contains(" ") ||
           pathCertificatoPartitaIvaDitta == null || pathVisuraCameraleDitta == null){

            if(validazionePartitaIvaDitta(numeroPartitaIvaDitta) == false) {
                editTextPartitaIvaDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextPartitaIvaDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneNomeDitta(nomeDitta) == false) {
                editTextNomeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNomeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneDomicilioFiscale(domicilioFiscaleDitta) == false) {
                editTextDomicilioFiscaleDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextDomicilioFiscaleDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneSedeDitta(sedeDitta) == false) {
                editTextSedeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextSedeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneTelefonoDitta(numeroTelefonoDitta) == false) {
                editTextTelefonoDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextTelefonoDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneEmailDitta(emailDitta) == false) {
                editTextEmailDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextEmailDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(pathCertificatoPartitaIvaDitta == null){
                textViewLabelCaricamentoCertificatoDitta.setVisibility(View.INVISIBLE);
            }else{
                textViewLabelCaricamentoCertificatoDitta.setVisibility(View.VISIBLE);
            }

            if(validazionePasswordDitta(passwordDitta) == false || passwordDitta.contains(" ") || !passwordDitta.equals(confermaPasswordDitta)) {
                editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(getActivity(), getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else{
                editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }


            if(pathVisuraCameraleDitta == null ){
                textViewLabelCaricamentoVisuraDitta.setVisibility(View.INVISIBLE);
            }else{
                textViewLabelCaricamentoVisuraDitta.setVisibility(View.VISIBLE);
            }

            Toast.makeText(getActivity(), getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{

            backgroundTintEditTextDittaFragment();
            Toast.makeText(getActivity(), getString(R.string.registrazioneEffettuataRegister), Toast.LENGTH_LONG).show();

        }


    }


    /**
     * Settaggio dell'underline a tutte le editText nel colore grigio.
     */
    public void backgroundTintEditTextDittaFragment() {

        editTextPartitaIvaDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNomeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextDomicilioFiscaleDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextSedeDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextTelefonoDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEmailDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));




        if(pathCertificatoPartitaIvaDitta != null) {
            textViewLabelCaricamentoCertificatoDitta.setVisibility(View.VISIBLE);
        }
        if(pathVisuraCameraleDitta != null) {
            textViewLabelCaricamentoVisuraDitta.setVisibility(View.VISIBLE);
        }


    }


    /**
     * Controllo che il numero di partita Iva rispetti le seguenti caratteristiche:
     * lunghezza di 11, consentiti solo caratteri numerici.
     * @param numeroPartitaIva stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, alt
     * rimenti false
     */
    public boolean validazionePartitaIvaDitta(String numeroPartitaIva) {

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
     * @param controlloNomeDitta stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNomeDitta(String controlloNomeDitta) {

        if(controlloNomeDitta == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-z]{3,15}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloNomeDitta);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Controllo che il domiclio fiscale dell'azienda sia formattato correttamente
     * Va inserito prima il numero civico e poi successivamente la via 
     * @param controlloDomiclioFiscale stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneDomicilioFiscale(String controlloDomiclioFiscale) {

        if(controlloDomiclioFiscale == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^\\d{1}[a-z-A-Z\\ \\d#-.\\/'()]+$");
        Matcher m = p.matcher(controlloDomiclioFiscale);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }



    /**
     * Controllo che la sede dell'azienda sia formattata correttamente
     * Va inserito prima il numero civico e poi successivamente la via
     * @param controlloSedeDitta stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneSedeDitta(String controlloSedeDitta) {

        if(controlloSedeDitta == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^\\d{1}[a-z-A-Z\\ \\d#-.\\/'()]+$");
        Matcher m = p.matcher(controlloSedeDitta);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    /**
     * Controllo che il numero di telefono rispetti le seguenti caratteristiche:
     * lunghezza compresa tra 9 e 11, consentiti solo caratteri numerici
     * @param controlloTelefonoDitta stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneTelefonoDitta(String controlloTelefonoDitta) {

        if(controlloTelefonoDitta == null) {
            return false;
        }

        Pattern p = Pattern.compile("^[0-9]{9,11}$");
        Matcher m = p.matcher(controlloTelefonoDitta);
        boolean matchTrovato = m.matches();

        return matchTrovato ;
    }


    /**
     * Controllo che l'email in input rispetti la forma standard delle email.
     * @param controlloEmailDitta stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneEmailDitta(String controlloEmailDitta) {

        if(controlloEmailDitta == null) {
            return false;
        }

        Pattern p = Pattern.compile(".+@.+\\.[a-z]+", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloEmailDitta);
        boolean matchTrovato = m.matches();

        String  espressioneAggiuntiva ="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pAggiuntiva = Pattern.compile(espressioneAggiuntiva, Pattern.CASE_INSENSITIVE);
        Matcher mAggiuntiva = pAggiuntiva.matcher(controlloEmailDitta);
        boolean matchTrovatoAggiuntivo = mAggiuntiva.matches();

        return matchTrovato && matchTrovatoAggiuntivo;
    }


    /**
     * Controllo che la password in input rispetti le seguenti caratteristiche:
     * contenta un carattere maiuscolo, contenga un carattere minuscolo e contenga
     * un carattere numerico e essa deve essere di lunghezza compresa tra 8 e 20 caratteri.
     * @param controlloPasswordDitta stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazionePasswordDitta(String controlloPasswordDitta) {

        if(controlloPasswordDitta == null)  {
            return false;
        }

        Pattern p = Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20})");
        Matcher m = p.matcher(controlloPasswordDitta);
        boolean matchTrovato = m.matches();

        return matchTrovato;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 201) {
                // Get the url of the image from data
                Uri selectedImageCertificatoUri = data.getData();
                if (null != selectedImageCertificatoUri) {
                    // update the preview image in the layout
                    pathCertificatoPartitaIvaDitta = selectedImageCertificatoUri;
                    textViewLabelCaricamentoCertificatoDitta.setVisibility(View.VISIBLE);
                }else{
                    textViewLabelCaricamentoCertificatoDitta.setVisibility(View.INVISIBLE);
                }
            }

            if (requestCode == 202) {
                // Get the url of the image from data
                Uri selectedImageVisuraUri = data.getData();
                if (null != selectedImageVisuraUri) {
                    // update the preview image in the layout
                    pathVisuraCameraleDitta = selectedImageVisuraUri;
                    textViewLabelCaricamentoVisuraDitta.setVisibility(View.VISIBLE);
                }else{
                    textViewLabelCaricamentoVisuraDitta.setVisibility(View.INVISIBLE);
                }
            }
        }
    }


}