package com.vitandreasorino.savent.EventiTab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vitandreasorino.savent.MapActivity;
import com.vitandreasorino.savent.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewEvent extends AppCompatActivity {

    Date dataInserita;
    private Double valoreLatitudine = null;
    private Double valoreLongitudine = null;
    private TextView textViewLatitudineScritta;
    private TextView textViewLongitudineScritta;

    private TextView textViewLatitudine;
    private TextView textViewLongitudine;

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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

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
                    }
                },hour, minute, android.text.format.DateFormat.is24HourFormat(context));
                timePickerDialog.show();
            }
        });

    }


    public void onMap(View view) {


        Intent mapActivity = new Intent(this, MapActivity.class);
        startActivityForResult(mapActivity, 203);
    }


    /*
    Metodo utilizzato per il ritorno della longitudine e latitudine dall'activity "MapActivity.java"
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
           valoreLatitudine == null || valoreLongitudine == null){

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

            }

            Toast.makeText(this, getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        }else{

            backgroundTintEditTextCreateEvent();
            statusProgress.getProgress();
            // valoreLatitudine;
            // valoreLongitudine;
            Toast.makeText(this, getString(R.string.eventoCreato), Toast.LENGTH_LONG).show();

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

        int contatoreCaratteri = 0;

        if(controlloNomeEvento == null)  {
            return false;
        }

        Pattern p = Pattern.compile("^[a-zA-Z ]*$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(controlloNomeEvento);
        boolean matchTrovato = m.matches();

        for (int i=0; i<controlloNomeEvento.length(); i++) {

           if(controlloNomeEvento.charAt(i) != ' ' ) {
               contatoreCaratteri++;
           }

        }

        return matchTrovato && contatoreCaratteri > 0;
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