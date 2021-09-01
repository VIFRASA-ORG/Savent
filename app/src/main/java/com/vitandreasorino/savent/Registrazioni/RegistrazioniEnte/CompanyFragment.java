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


public class CompanyFragment extends Fragment implements View.OnFocusChangeListener {

    Uri pathCertificatoPartitaIvaDitta;
    Uri pathVisuraCameraleDitta;

    private static final int RESULT_OK = -1;
    private Button buttonCertificatoPartitaIvaDitta, buttonVisuraCameraleDitta, buttonRegistrazioneDitta;

    EditText editTextPartitaIvaDitta,  editTextNomeDitta,editTextDomicilioFiscaleDitta, editTextSedeDitta, editTextTelefonoDitta,
            editTextEmailDitta, editTextPasswordDitta, editTextConfermaPasswordDitta;


    TextView textViewLabelCaricamentoCertificatoDitta, textViewLabelCaricamentoVisuraDitta;
    ProgressBar progressBar ;


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

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAllFocusChanged();
    }

    private void setAllFocusChanged(){
        editTextPartitaIvaDitta.setOnFocusChangeListener(this);
        editTextNomeDitta.setOnFocusChangeListener(this);
        editTextDomicilioFiscaleDitta.setOnFocusChangeListener(this);
        editTextSedeDitta.setOnFocusChangeListener(this);
        editTextTelefonoDitta.setOnFocusChangeListener(this);
        editTextEmailDitta.setOnFocusChangeListener(this);
        editTextPasswordDitta.setOnFocusChangeListener(this);
        editTextConfermaPasswordDitta.setOnFocusChangeListener(this);
    }

    private void removeAllFocus(){
        editTextPartitaIvaDitta.clearFocus();
        editTextNomeDitta.clearFocus();
        editTextDomicilioFiscaleDitta.clearFocus();
        editTextSedeDitta.clearFocus();
        editTextTelefonoDitta.clearFocus();
        editTextEmailDitta.clearFocus();
        editTextPasswordDitta.clearFocus();
        editTextConfermaPasswordDitta.clearFocus();
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
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

            if(validazionePasswordDitta(passwordDitta) == false || passwordDitta.contains(" ") ) {
                editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(getActivity(), getString(R.string.passwordErrataRegister), Toast.LENGTH_LONG).show();
            }else if(!passwordDitta.equals(confermaPasswordDitta)){
                editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                Toast.makeText(getActivity(), getString(R.string.passwordsNotMatching), Toast.LENGTH_LONG).show();
                return;
            }else{
                editTextPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
                editTextConfermaPasswordDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(pathCertificatoPartitaIvaDitta == null){
                textViewLabelCaricamentoCertificatoDitta.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(),R.string.errorPIVA,Toast.LENGTH_LONG).show();
                return;
            }else{
                textViewLabelCaricamentoCertificatoDitta.setVisibility(View.VISIBLE);
            }

            if(pathVisuraCameraleDitta == null ){
                textViewLabelCaricamentoVisuraDitta.setVisibility(View.INVISIBLE);
                Toast.makeText(getActivity(),R.string.errorVCamerale,Toast.LENGTH_LONG).show();
                return;
            }else{
                textViewLabelCaricamentoVisuraDitta.setVisibility(View.VISIBLE);
            }
        }else{

            disableAllComponents();
            backgroundTintEditTextDittaFragment();

            Ente e = new Ente();
            e.setPartitaIva(numeroPartitaIvaDitta);
            e.setNomeDitta(nomeDitta);
            e.setDomicilioFiscaleDitta(domicilioFiscaleDitta);
            e.setVisuraCamerale(pathVisuraCameraleDitta);
            e.setCertificatoPIVA(pathCertificatoPartitaIvaDitta);
            e.setSedeDitta(sedeDitta);

            //Check if the phone number is already taken
            GenericUser.isPhoneNumberAlreadyTaken(numeroTelefonoDitta, closureBool -> {
                if(!closureBool){
                    e.setNumeroTelefono(numeroTelefonoDitta);
                    computeRegistrationToServer(e,emailDitta,passwordDitta);
                }else{
                    editTextTelefonoDitta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                    Toast.makeText(getActivity(),R.string.phoneNumberAlreadyTaken,Toast.LENGTH_LONG).show();
                    enableAllComponents();
                    return;
                }
            });
        }
    }

    private void computeRegistrationToServer(Ente e, String email, String password){
        Enti.createNewEnteDitta(e,email,password,closureBool -> {
            if(closureBool){
                /*  If the Ente creation is successful, execute the logout from the account because
                 *   it has to be enabled from the service provider after having verified all the information.
                 * */

                //LogOut
                AuthHelper.logOut(getContext());
                Toast.makeText(getActivity(),R.string.enteCreated,Toast.LENGTH_LONG).show();

                //Go back to the first page
                Intent firstPage = new Intent(getActivity(), LogSingInActivity.class);
                firstPage.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);    //Removing from the stack all the previous Activity.
                startActivity(firstPage);
                getActivity().finish();
            }else{
                Toast.makeText(getActivity(), getString(R.string.registrazioneErrore), Toast.LENGTH_LONG).show();
                AuthHelper.logOut(getContext());
                enableAllComponents();
            }
        });
    }

    private void disableAllComponents(){

        editTextPartitaIvaDitta.setEnabled(false);
        editTextNomeDitta.setEnabled(false);
        editTextDomicilioFiscaleDitta.setEnabled(false);
        editTextSedeDitta.setEnabled(false);
        editTextTelefonoDitta.setEnabled(false);
        editTextEmailDitta.setEnabled(false);
        editTextPasswordDitta.setEnabled(false);
        editTextConfermaPasswordDitta.setEnabled(false);

        buttonCertificatoPartitaIvaDitta.setEnabled(false);
        buttonVisuraCameraleDitta.setEnabled(false);
        buttonRegistrazioneDitta.setEnabled(false);

        AnimationHelper.fadeIn(progressBar,1000);
    }

    private void enableAllComponents(){

        editTextPartitaIvaDitta.setEnabled(true);
        editTextNomeDitta.setEnabled(true);
        editTextDomicilioFiscaleDitta.setEnabled(true);
        editTextSedeDitta.setEnabled(true);
        editTextTelefonoDitta.setEnabled(true);
        editTextEmailDitta.setEnabled(true);
        editTextPasswordDitta.setEnabled(true);
        editTextConfermaPasswordDitta.setEnabled(true);

        buttonCertificatoPartitaIvaDitta.setEnabled(true);
        buttonVisuraCameraleDitta.setEnabled(true);
        buttonRegistrazioneDitta.setEnabled(true);

        AnimationHelper.fadeOut(progressBar,1000);
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