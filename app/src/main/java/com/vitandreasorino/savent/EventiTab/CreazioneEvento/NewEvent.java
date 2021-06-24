package com.vitandreasorino.savent.EventiTab.CreazioneEvento;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vitandreasorino.savent.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AuthHelper;
import Helper.ImageHelper;
import Model.DB.Eventi;
import Model.DB.Gruppi;
import Model.DB.Utenti;
import Model.Pojo.Evento;
import Model.Pojo.Gruppo;

public class NewEvent extends AppCompatActivity {

    AutoCompleteTextView autoComplete;
    Uri immagineSelezionata;
    ImageView imageViewEvent;

    String idAccountCreatore;
    ArrayList<String> arrayId = new ArrayList<>();
    ArrayList<String> arrayNome = new ArrayList<>();

    int anno; int mese; int giorno;
    int ora; int minuti;

    Date dataInserita;
    private TextView textViewLatitudineScritta;
    private TextView textViewLongitudineScritta;

    private TextView textViewLatitudine;
    private TextView textViewLongitudine;
    private Double valoreLatitudine = null;
    private Double valoreLongitudine = null;

    private EditText editTextNomeEvento;
    private EditText editTextTextMultiLine;
    private EditText editTextNuumeroMassimoPartecipanti;

    private SeekBar statusProgress;
    private TextView textViewContatoreStatus;

    private static final String TAG = "NewEvent";
    private TextView displayDate;
    private DatePickerDialog.OnDateSetListener dateSetListener;

    private TextView displayTime;
    Context context = this;


    private void setAdapter(){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.options_item, arrayNome);
        try{
            autoComplete.setText(arrayAdapter.getItem(0).toString(), false);
        }catch(IndexOutOfBoundsException e){
            e.printStackTrace();
        }
        autoComplete.setAdapter(arrayAdapter);
        autoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = arrayId.get(position);
                idAccountCreatore = item;

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);


        autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        arrayNome.clear();
        arrayId.clear();

        /*
        inserisco il nome e cognome utente all'interno del popup
         */
        String idUtenteLoggato = AuthHelper.getUserId();
        Utenti.getNameSurnameOfUser(idUtenteLoggato, closureResult -> {

            if(closureResult != null) {
                arrayId.add(AuthHelper.getUserId());
                arrayNome.add("<" + closureResult + "> " + getString(R.string.accountCreatore));
                idAccountCreatore = AuthHelper.getUserId();
                setAdapter();


                /*
                inserisco il nome dei gruppi per il quale l'utente loggato è amministratore
                 */
                Gruppi.getAdministrationGroups(closureList -> {
                    if(closureList != null) {
                        for(Gruppo g : closureList) {
                            arrayId.add(g.getId());
                            arrayNome.add(g.getNome());
                            setAdapter();
                        }
                    }
                });
            }
        });


        setAdapter();

        imageViewEvent = (ImageView) findViewById(R.id.imageViewEvent);


        textViewLatitudine = (TextView) findViewById(R.id.textViewLatitudine);
        textViewLongitudine = (TextView) findViewById(R.id.textViewLongitudine);

        textViewLatitudineScritta = (TextView) findViewById(R.id.textViewLatitudine);
        textViewLongitudine = (TextView) findViewById(R.id.textViewLongitudine);

        editTextNomeEvento = (EditText) findViewById(R.id.editTextNomeEvento);
        editTextTextMultiLine = (EditText) findViewById(R.id.editTextTextMultiLine);
        editTextNuumeroMassimoPartecipanti = (EditText) findViewById(R.id.editTextNuumeroMassimoPartecipanti);


        textViewContatoreStatus = (TextView) findViewById(R.id.textViewContatoreStatus);
        statusProgress = (SeekBar) findViewById(R.id.seekBarStatusProgress);

        displayDate = (TextView) findViewById(R.id.textViewSelectDataEvent);
        displayTime = (TextView) findViewById(R.id.textViewSelectTimeEvent);

        Calendar calendario = Calendar.getInstance();
        int hour = calendario.get(Calendar.HOUR_OF_DAY);
        int minute = calendario.get(Calendar.MINUTE);

        /*
        Evento per settare il progress della seekBar in tempo reale
         */
        statusProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int i=0;

            /*
            Metodo che permette di cambiare colore a seconda del progresso selezionato dall'utente alla seekBar
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                i=progress;
                if(i<=33) {
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                }else if(i>33 && i<= 66) {
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                }else{
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /*
            Metodo che permette di indicare la percentuale inserita dall'utente nella seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewContatoreStatus.setText(""+ i +"%");

            }
        });

        /*
        Metodo per l'evento del click della textView, aprirà un DatePickerDialog tramite il quale si selezionerà
        la data dell'evento mostrandola e settandola nella textView
        */
        displayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);

                Calendar cal1 = (Calendar) cal.clone();

                if(dataInserita != null) {
                    cal.setTimeInMillis(dataInserita.getTime());
                }


                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(NewEvent.this,
                        dateSetListener, year, month, day);


                dialog.getDatePicker().setMinDate(cal1.getTime().getTime());

                dialog.show();
            }
        });


        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d(TAG, "onDateSet: dd/mm/yyyy: " + dayOfMonth + "/" + month + "/" + year);

                String date = dayOfMonth + "/" + (month+1) + "/" + year;

                Calendar calendario = Calendar.getInstance();
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.YEAR, year);

                anno = year;
                mese = month;
                giorno = dayOfMonth;

                dataInserita = calendario.getTime();


                displayDate.setText(date);

            }
        };


        /*
        Metodo per l'evento del click della textView, aprirà un timePickerDialog tramite il quale si selezionerà
        l'orario dell'evento mostrandolo e settandolo nella textView
         */
        displayTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        displayTime.setText(hourOfDay + ":" + minute);
                        ora = hourOfDay;
                        minuti = minute;
                    }
                },hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });

    }


    public void onMap(View view) {

        Intent mapActivity = new Intent(this, MapActivity.class);
        mapActivity.putExtra("isNotNull", (valoreLatitudine != null && valoreLongitudine != null));
        mapActivity.putExtra("latitudine", valoreLatitudine);
        mapActivity.putExtra("longitudine",valoreLongitudine);
        startActivityForResult(mapActivity, 203);

    }


    public void onClickPhotoEvent(View view){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 210);
    }


    /*
    Metodo utilizzato per il ritorno della longitudine e latitudine dall'activity "MapActivity.java"
    e per il settaggio di un immagine all'interno della textView
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 203) {


            if(resultCode == RESULT_OK) {
                boolean isNull = data.getBooleanExtra("isNull", true);

                if(isNull == true) {
                    textViewLatitudine.setVisibility(View.INVISIBLE);
                    textViewLongitudine.setVisibility(View.INVISIBLE);
                }else{
                    valoreLatitudine = data.getDoubleExtra("latitudine",0);
                    valoreLongitudine = data.getDoubleExtra("longitudine",0);
                    textViewLatitudine.setVisibility(View.VISIBLE);
                    textViewLongitudine.setVisibility(View.VISIBLE);
                    textViewLatitudine.setText("" + valoreLatitudine);
                    textViewLongitudine.setText("" + valoreLongitudine);

                }
            }
        }


        if (resultCode == RESULT_OK) {
            // compare the resultCode with the
            // SELECT_PICTURE constant

            if (requestCode == 210) {

                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // Compress it before it is shown into the imageView
                    imageViewEvent.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(),selectedImageUri,imageViewEvent));
                    immagineSelezionata = selectedImageUri;
                }
            }
        }
    }

    /*
     Cliccando il bottone ritorna alla schermata precedente chiudendo definitivamente l'activity attuale
     */
    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }


    /*
     Metodo Utilizzato per creare l'evento da parte dell'utente
     */
    public void onCreateEvent(View view) {
        controlloInputUtenteCreazioneEvento();
    }


    /*
     Metodo Utilizzato per effettuare i controlli in input da parte dell'utente nella creazione Evento
     */
    private void controlloInputUtenteCreazioneEvento() {

        String nomeEvento, descrizioneEvento, numeroPartecipantiEvento, selezioneData, selezionaOra;

        nomeEvento = editTextNomeEvento.getText().toString();
        descrizioneEvento = editTextTextMultiLine.getText().toString();
        numeroPartecipantiEvento = editTextNuumeroMassimoPartecipanti.getText().toString();
        selezioneData = displayDate.getText().toString();
        selezionaOra = displayTime.getText().toString();


        if(validazioneNomeEvento(nomeEvento) == false || nomeEvento.isEmpty() ||
           validazioneDescrizioneEvento(descrizioneEvento) == false || descrizioneEvento.isEmpty() ||
           numeroPartecipantiEvento.isEmpty() || validazioneNumeroPartecipantiEvento(numeroPartecipantiEvento) == false ||
           valoreLatitudine == null || valoreLongitudine == null || selezioneData.equals("Select date")|| selezioneData.equals("Seleziona data") ||
                selezionaOra.equals("Select time")|| selezionaOra.equals("Seleziona ora")){

            if(validazioneNomeEvento(nomeEvento) == false || nomeEvento.isEmpty()) {
                editTextNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(validazioneDescrizioneEvento(descrizioneEvento) == false || descrizioneEvento.isEmpty()) {
                editTextTextMultiLine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextTextMultiLine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(numeroPartecipantiEvento.isEmpty() || validazioneNumeroPartecipantiEvento(numeroPartecipantiEvento) == false) {
                editTextNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
            }else {
                editTextNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(valoreLatitudine == null || valoreLongitudine == null) {
                Toast.makeText(this, getString(R.string.luogoEvento), Toast.LENGTH_SHORT).show();
            }

            if(selezioneData.equals("Select date")|| selezioneData.equals("Seleziona data") ||
               selezionaOra.equals("Select time")|| selezionaOra.equals("Seleziona ora")) {

                Toast.makeText(this, getString(R.string.messaggioDataOraEvento), Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{

            backgroundTintEditTextCreateEvent();
            Evento e = new Evento();
            e.setImageUri(immagineSelezionata);

            for(int i=0; i<arrayId.size(); i++) {

                if(idAccountCreatore.equals(arrayId.get(i))) {
                    if(i==0) {
                        e.setUtenteCreatore(idAccountCreatore);
                    }else{
                        e.setGruppoCreatore(idAccountCreatore);
                    }
                }
            }

            e.setNome(nomeEvento);
            e.setDescrizione(descrizioneEvento);
            e.setNumeroPartecipanti(0);
            e.setNumeroMassimoPartecipanti(Integer.parseInt(numeroPartecipantiEvento));
            e.setSogliaAccettazioneStatus(statusProgress.getProgress());

            Calendar c = Calendar.getInstance();
            c.set(anno,mese,giorno, ora, minuti);
            e.setDataOra(c.getTime());


            e.setLatitudine(valoreLatitudine);
            e.setLongitudine(valoreLongitudine);

            Eventi.addNewEvent(e, closureResult -> {

                if(closureResult != null){

                    if(immagineSelezionata != null) {
                        Eventi.uploadEventImage(immagineSelezionata, closureResult, closureBool-> {
                            if(closureBool) {
                                Toast.makeText(this, getString(R.string.eventoCreato), Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        Toast.makeText(this, getString(R.string.eventoCreato), Toast.LENGTH_LONG).show();
                    }

                }
            });

        }

    }

    private void backgroundTintEditTextCreateEvent() {
        editTextNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextTextMultiLine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewLatitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewLongitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));

    }


    /**
     * Controllo che il nome in input rispetti le seguenti caratteristiche:
     * controlla che contenga la stringa solo caratteri letterali, e inoltre che non sia
     * vuota o che contenga solo spazi.
     * @param controlloNomeEvento stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNomeEvento(String controlloNomeEvento) {

        return !controlloNomeEvento.isEmpty();
    }


    /**
     * Controlla che la descrizione non contenga solo caratteri di spaziatura
     * @param controlloDescrizioneEvento stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneDescrizioneEvento(String controlloDescrizioneEvento) {

        int contatoreCaratteri = 0;

        if(controlloDescrizioneEvento == null)  {
            return false;
        }


        for (int i=0; i<controlloDescrizioneEvento.length(); i++) {

            if(controlloDescrizioneEvento.charAt(i) != ' ' ) {
                contatoreCaratteri++;
            }

        }

        return contatoreCaratteri > 0;
    }


    /**
     * Parse della stringa in un intero, e successivo controllo del valore che sia compreso tra 2 e 300
     * @param controlloNumeroPartecipantiEvento stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNumeroPartecipantiEvento(String controlloNumeroPartecipantiEvento) {

        if(controlloNumeroPartecipantiEvento == null)  {
            return false;
        }

        int numeroPartecipanti = Integer.parseInt(controlloNumeroPartecipantiEvento);

        if(numeroPartecipanti < 1) {
            return false;
        }else{
            return true;
        }

    }


}