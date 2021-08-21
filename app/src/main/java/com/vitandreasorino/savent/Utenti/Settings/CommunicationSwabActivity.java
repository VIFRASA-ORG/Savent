package com.vitandreasorino.savent.Utenti.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.Calendar;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Helper.SQLiteHelper;
import Model.Closures.ClosureBoolean;
import Model.DB.CodiciComunicazioneTampone;
import Model.DB.CodiciIdentificativi;
import Model.DB.Enti;
import Model.DB.Utenti;
import Model.Pojo.CodiceComunicazioneTampone;
import Model.Pojo.CodiceIdentificativo;

public class CommunicationSwabActivity extends AppCompatActivity {

    public static final String STATUS_SANITARIO_FIELD = "statusSanitario";
    Button buttonCommunicationSwab;
    Button buttonRealCommunicationSwab;
    LinearLayout boxDettaglioTampone;
    TextView textViewDettaglioTampone;
    TextView textViewEsitoFinaleTampone;
    TextView textViewDataTampone;
    TextView textViewEnteRilascio;
    EditText editTextCodeCommunicationSwab;
    ProgressBar progressBar;
    CodiceComunicazioneTampone tampone = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_swab);
        buttonCommunicationSwab = (Button) findViewById(R.id.buttonCommunicationSwab);
        buttonRealCommunicationSwab = (Button) findViewById(R.id.buttonRealCommunicationSwab);
        boxDettaglioTampone = (LinearLayout) findViewById(R.id.boxDettaglioTampone);
        textViewDettaglioTampone = (TextView) findViewById(R.id.textViewDettaglioTampone);
        textViewEsitoFinaleTampone = (TextView) findViewById(R.id.textViewEsitoFinaleTampone);
        textViewDataTampone = (TextView) findViewById(R.id.textViewDataTampone);
        textViewEnteRilascio = (TextView) findViewById(R.id.textViewEnteRilascio);
        editTextCodeCommunicationSwab = (EditText) findViewById(R.id.editTextCodeCommunicationSwab);
        progressBar = (ProgressBar) findViewById(R.id.progressBarCommSwab);

        editTextCodeCommunicationSwab.addTextChangedListener(textWatcher);

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

    private void checkSaveButtonActivation() {
        if(editTextCodeCommunicationSwab.getText().toString().contains(" ") || editTextCodeCommunicationSwab.getText().length() == 0 ) {
            buttonCommunicationSwab.setEnabled(false);
            buttonRealCommunicationSwab.setEnabled(false);
        }else{
            buttonCommunicationSwab.setEnabled(true);
            buttonRealCommunicationSwab.setEnabled(false);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * Metodo chiamato quando clicchi la freccetta per tornare indietro
     * alla schermata precedente
     * @param view
     */
    public void onBackButtonPressed(View view) {

        onBackPressed();
    }


    /**
     * Metodo per la visualizzazione dell'esito del tampone
     * @param view
     */
    public void onClickViewSwab(View view) {

        // salviamo il codice inserito nell'editText in una stringa che andremo a confrontare con i codici presenti
        // nel database centrale.
        String codiceEditText;
        codiceEditText = editTextCodeCommunicationSwab.getText().toString();

        // Controllo se il codice inserito nell'editText è presente su Firestore
        CodiciComunicazioneTampone.getCode(codiceEditText, codice -> {
            tampone = codice;
            if(codice != null) {

                // Controllo che il codice inerente al tampone non sia già stato utilizzato
                if(codice.getUsato() == false) {

                    // Controllo l'ente che ha assegnato quel codice all'utente
                    Enti.getEnte(codice.getIdEnteCreatore(), ente -> {
                        if(ente != null) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(codice.getDataCreazione());
                            textViewDataTampone.setText("" + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + "  " +
                                    calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE));
                            textViewEsitoFinaleTampone.setVisibility(View.VISIBLE);
                            textViewDataTampone.setVisibility(View.VISIBLE);
                            textViewEnteRilascio.setVisibility(View.VISIBLE);
                            buttonRealCommunicationSwab.setEnabled(true);

                            // Controllo dell'esito del tampone, in caso di positività si setta "Positive", altrimenti "Negative"
                            if(codice.getEsitoTampone()) {
                                // Settare la positività
                                textViewEsitoFinaleTampone.setText(getString(R.string.statusPositivo));
                            }else{
                                // Settare la negatività
                                textViewEsitoFinaleTampone.setText(getString(R.string.statusNegativo));
                            }

                            // Settaggio dell'ente che ha rilasciato il codice, per controllare se è stato rilasciato
                            // da una ditta o da un libero professionista
                            if(ente.getNomeDitta() != null) {
                                textViewEnteRilascio.setText(ente.getNomeDitta());
                            }else{
                                textViewEnteRilascio.setText(ente.getNomeLP() + " " + ente.getCognomeLP());
                            }
                        }
                    });

                    // in caso il codice del tampone sia stato già utilizzato si settano tutti i campi non visibili
                }else{
                    textViewEsitoFinaleTampone.setVisibility(View.INVISIBLE);
                    textViewDataTampone.setVisibility(View.INVISIBLE);
                    textViewEnteRilascio.setVisibility(View.INVISIBLE);
                    Toast.makeText(CommunicationSwabActivity.this, getString(R.string.codiceUtilizzato), Toast.LENGTH_LONG).show();
                    buttonRealCommunicationSwab.setEnabled(false);
                }
                // se il codice inserito non coincide con nessuno dei codici presenti su Firestore, si settano tutti i  campi
                // non visibili e si mostra a schermo un messaggio di errore
            }else{
                textViewEsitoFinaleTampone.setVisibility(View.INVISIBLE);
                textViewDataTampone.setVisibility(View.INVISIBLE);
                textViewEnteRilascio.setVisibility(View.INVISIBLE);
                Toast.makeText(CommunicationSwabActivity.this, getString(R.string.validitaCodice), Toast.LENGTH_LONG).show();
                buttonRealCommunicationSwab.setEnabled(false);
            }
        });

    }


    /**
     * Metodo per la comunicazione dell'esito del tampone al sistema
     * @param view
     */
    public void onClickCommunicationSwab(View view) {

        String codiceEditText;
        codiceEditText = editTextCodeCommunicationSwab.getText().toString();

        // Alert che comparirà a schermo una volta cliccato il pulsante "Comunica esito"
        AlertDialog.Builder alert = new  AlertDialog.Builder(CommunicationSwabActivity.this);
        alert.setTitle(getString(R.string.comunicaCodiceTitolo));
        alert.setMessage(getString(R.string.comunicazioneEsitoDescrizione));

        // Settaggio operazioni da effettuare nel caso l'utente scelga "Si\Yes"
        alert.setPositiveButton(getString(R.string.siAlert), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                if(tampone != null) {

                    // Alert che comparirà a schermo una volta cliccato il pulsante "Yes"
                    AlertDialog.Builder alertConfirm = new  AlertDialog.Builder(CommunicationSwabActivity.this);
                    alertConfirm.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Si puliscono tutti i campi delle TextView e dell'editText
                            editTextCodeCommunicationSwab.setText("");
                            textViewEsitoFinaleTampone.setText("");
                            textViewDataTampone.setText("");
                            textViewEnteRilascio.setText("");
                        }
                    });

                    // Controlla che l'esito del tampone sia true, e quindi che l'utente risulti positivo
                    if(tampone.getEsitoTampone()) {
                        SQLiteHelper helper = new SQLiteHelper(getApplicationContext());

                        // Assegnazione di tutti i codici presenti sul database locale (SQLite) dell'utente loggato
                        // inseriti in un arrayList di tipo CodiceIdentificativo.
                        ArrayList<CodiceIdentificativo> listaCodici = helper.letturaMieiCodici();

                        // si disabilitano tutti i pulsanti e l'editText con relativa progressBar
                        changeComponentStatus(false);
                        AnimationHelper.fadeIn(progressBar, 1000);

                        // Inserimento dei codici positivi presenti nell'arrayList tramite una transizione per inserirli su Firestore
                        CodiciIdentificativi.communicatePositiveCodeTransaction(codiceEditText, listaCodici, isSuccess -> {
                            if(isSuccess) {
                                alertConfirm.setTitle(getString(R.string.positivita));
                                alertConfirm.setMessage(getString(R.string.comunicazioneAvvenuta));
                                alertConfirm.show();

                                // si attivano tutti i pulsanti e l'editText con relativa progressBar
                                changeComponentStatus(true);
                                AnimationHelper.fadeOut(progressBar, 1000);
                                updateStatus();
                            }else{
                                alertConfirm.setTitle(getString(R.string.errore));
                                alertConfirm.setMessage(getString(R.string.comunicazioneFallita));

                                // si attivano tutti i pulsanti e l'editText con relativa progressBar
                                changeComponentStatus(true);
                                AnimationHelper.fadeOut(progressBar, 1000);
                                alertConfirm.show();
                            }
                        });
                        // Aggiornamento dello stato sanitario nella HomeActivity (logo,textView)
                    }else{

                        // si disabilitano tutti i pulsanti e l'editText con relativa progressBar
                        changeComponentStatus(false);
                        AnimationHelper.fadeIn(progressBar, 1000);
                        CodiciIdentificativi.communicateNegativeCodeTransaction(codiceEditText, isSuccess -> {
                            if(isSuccess) {
                                alertConfirm.setTitle(getString(R.string.negativita));
                                alertConfirm.setMessage(getString(R.string.comunicazioneAvvenuta));
                                alertConfirm.show();

                                // si attivano tutti i pulsanti e l'editText con relativa progressBar
                                changeComponentStatus(true);
                                AnimationHelper.fadeOut(progressBar, 1000);
                                updateStatus();
                            }else{
                                alertConfirm.setTitle(getString(R.string.errore));
                                alertConfirm.setMessage(getString(R.string.comunicazioneFallita));
                                alertConfirm.show();

                                // si attivano tutti i pulsanti e l'editText con relativa progressBar
                                changeComponentStatus(true);
                                AnimationHelper.fadeOut(progressBar, 1000);
                            }
                        });
                        // Aggiornamento dello stato sanitario nella HomeActivity (logo,textView)

                    }
                }
            }
        });

        // Settaggio operazioni da effettuare nel caso l'utente scelga "No"
        alert.setNegativeButton(getString(R.string.noAlert), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();


    }


    /**
     * Metodo per l'aggiornamento dello stato sanitario dell'utente loggato
     * @param status variabile intera che indica la la percentuale di positività
     */
    private void aggiornaStatoSanitario(int status) {
        Utenti.updateFields(AuthHelper.getUserId(), new ClosureBoolean() {
            @Override
            public void closure(boolean isSuccess) {

            }
        }, STATUS_SANITARIO_FIELD,status);
    }


    /**
     * Update status sanitario
     */
    private void updateStatus() {
        Intent i = new Intent("updateStatusHealth");
        i.putExtra("updatedStatusHealth", true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(i);
    }

    /**
     * Metodo per attivare\disattivare i componenti del layout
     * @param value valore booleano da passare in caso true si attivano, altrimenti si disattivano
     */
    private void changeComponentStatus(boolean value) {
        buttonRealCommunicationSwab.setEnabled(value);
        buttonCommunicationSwab.setEnabled(value);
        editTextCodeCommunicationSwab.setEnabled(value);
    }



}


