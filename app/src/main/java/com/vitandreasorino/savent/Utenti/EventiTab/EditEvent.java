package com.vitandreasorino.savent.Utenti.EventiTab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.vitandreasorino.savent.Utenti.EventiTab.CreazioneEvento.MapActivity;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Helper.ImageHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureResult;
import Model.DAO.Eventi;
import Model.DAO.Gruppi;
import Model.DAO.Utenti;
import Model.POJO.Evento;
import Model.POJO.Gruppo;

public class EditEvent extends AppCompatActivity implements View.OnFocusChangeListener {

    String creatoreId;


    private boolean isModified = false;

    ImageView saveSettings;


    ProgressBar progressBarEvent;

    AutoCompleteTextView autoCompleteEdit;
    Uri immagineSelezionataEdit;
    ImageView imageViewEditEvent;

    String idAccountCreatoreEdit;
    ArrayList<String> arrayIdEdit = new ArrayList<>();
    ArrayList<String> arrayNomeEdit = new ArrayList<>();

    Evento eventModel;
    Gruppo groupModel;

    private Calendar oldDate;
    private Calendar newDate;

    private TextView displayDateEdit;
    private DatePickerDialog.OnDateSetListener dateSetListenerEdit;

    private static final String TAG = "EditEvent";
    private TextView displayTimeEdit;
    Context contextEdit = this;

    private SeekBar seekBarStatusProgressEditEvent;
    private TextView textViewEditContatoreStatus;

    private EditText editTextEditNomeEvento;
    private EditText editTextEditDescrizione;
    private EditText editTextEditNuumeroMassimoPartecipanti;

    private TextView textViewEditLatitudine;
    private TextView textViewEditLongitudine;
    private Double valoreLatitudineEdit = null;
    private Double valoreLongitudineEdit = null;


    private void setAdapterEdit(int position) {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.options_item, arrayNomeEdit);
        try {
            autoCompleteEdit.setText(arrayAdapter.getItem(position).toString(), false);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        autoCompleteEdit.setAdapter(arrayAdapter);
        autoCompleteEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String item = arrayIdEdit.get(position);
                idAccountCreatoreEdit = item;
                checkSaveButtonActivation();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);



        saveSettings = (ImageView) findViewById(R.id.saveSettings);

        progressBarEvent = findViewById(R.id.progressBarEvent);

        imageViewEditEvent = (ImageView) findViewById(R.id.imageViewEditEvent);

        displayDateEdit = (TextView) findViewById(R.id.textViewEditSelectDataEvent);
        displayTimeEdit = (TextView) findViewById(R.id.textViewEditSelectTimeEvent);

        textViewEditContatoreStatus = (TextView) findViewById(R.id.textViewEditContatoreStatus);
        seekBarStatusProgressEditEvent = (SeekBar) findViewById(R.id.seekBarStatusProgressEditEvent);

        editTextEditNomeEvento = (EditText) findViewById(R.id.editTextEditNomeEvento);
        editTextEditDescrizione = (EditText) findViewById(R.id.editTextEditDescrizione);
        editTextEditNuumeroMassimoPartecipanti = (EditText) findViewById(R.id.editTextEditNuumeroMassimoPartecipanti);

        textViewEditLatitudine = (TextView) findViewById(R.id.textViewEditLatitudine);
        textViewEditLongitudine = (TextView) findViewById(R.id.textViewEditLongitudine);


        autoCompleteEdit = (AutoCompleteTextView) findViewById(R.id.autoCompleteEditEvent);
        arrayNomeEdit.clear();
        arrayIdEdit.clear();

        saveSettings.setEnabled(false);

        /*
        inserisco il nome e cognome utente all'interno del popup
         */
        String idUtenteLoggato = AuthHelper.getUserId();
        Utenti.getNameSurnameOfUser(idUtenteLoggato, closureResult -> {

            if (closureResult != null) {
                arrayIdEdit.add(AuthHelper.getUserId());
                arrayNomeEdit.add("<" + closureResult + "> " + getString(R.string.accountCreatore));
                if(eventModel.getIdUtenteCreatore().equals(idUtenteLoggato)){
                    idAccountCreatoreEdit = eventModel.getIdUtenteCreatore();
                    setAdapterEdit(0);
                    checkSaveButtonActivation();
                }


                /*
                inserisco il nome dei gruppi per il quale l'utente loggato è amministratore
                 */
                Gruppi.getAdministrationGroups(closureList -> {
                    if (closureList != null) {
                        int i = 1;
                        int groupPosition = 0;
                        for (Gruppo g : closureList) {
                            arrayIdEdit.add(g.getId());
                            arrayNomeEdit.add(g.getNome());
                            if(eventModel.getIdGruppoCreatore().equals(g.getId())) {
                                groupPosition = i;
                                idAccountCreatoreEdit = g.getId();
                            }
                            i++;
                        }
                        setAdapterEdit(groupPosition);
                        checkSaveButtonActivation();
                    }
                });
            }
        });


        //Deserializing the object from the intent
        eventModel = (Evento) getIntent().getSerializableExtra("eventObj");

        oldDate = Calendar.getInstance();
        oldDate.setTime( eventModel.getDataOra());

        newDate = Calendar.getInstance();
        newDate.setTime( eventModel.getDataOra());

        //Adding the listener for updates
        Eventi.addDocumentListener(eventModel.getId(),this,closureResult -> {
            if(closureResult != null){
                eventModel = closureResult;
                oldDate = Calendar.getInstance();
                oldDate.setTime( eventModel.getDataOra());
                refreshData();
            }
        });






        saveSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveDataButtonClick(v);
            }
        });

        /*
        Evento per settare il progress della seekBar in tempo reale
        */
        seekBarStatusProgressEditEvent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int i = 0;

            /*
            Metodo che permette di cambiare colore a seconda del progresso selezionato dall'utente alla seekBar
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                i = progress;
                if (i <= 33) {
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                } else if (i > 33 && i <= 66) {
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                } else {
                    seekBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                }

                checkSaveButtonActivation();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            /*
            Metodo che permette di indicare la percentuale inserita dall'utente nella seekBar
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewEditContatoreStatus.setText("" + i + "%");

            }
        });


                /*
        Metodo per l'evento del click della textView, aprirà un DatePickerDialog tramite il quale si selezionerà
        la data dell'evento mostrandola e settandola nella textView
        */
        displayDateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);


                int year = newDate.get(Calendar.YEAR);
                int month = newDate.get(Calendar.MONTH);
                int day = newDate.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog dialog = new DatePickerDialog(EditEvent.this,
                        dateSetListenerEdit, year, month, day);


                dialog.getDatePicker().setMinDate(cal.getTime().getTime());

                dialog.show();
            }
        });


        dateSetListenerEdit = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d(TAG, "onDateSet: dd/mm/yyyy: " + dayOfMonth + "/" + month + "/" + year);

                String date = dayOfMonth + "/" + (month + 1) + "/" + year;

                newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                newDate.set(Calendar.MONTH, month);
                newDate.set(Calendar.YEAR, year);

                displayDateEdit.setText(date);

                checkSaveButtonActivation();
            }
        };


        /*
        Metodo per l'evento del click della textView, aprirà un timePickerDialog tramite il quale si selezionerà
        l'orario dell'evento mostrandolo e settandolo nella textView
         */
        displayTimeEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(contextEdit, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        displayTimeEdit.setText(hourOfDay + ":" + minute);
                        newDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        newDate.set(Calendar.MINUTE, minute);
                        checkSaveButtonActivation();
                    }
                }, newDate.get(Calendar.HOUR_OF_DAY), newDate.get(Calendar.MINUTE), android.text.format.DateFormat.is24HourFormat(contextEdit));
                timePickerDialog.show();
            }
        });


        editTextEditNomeEvento.addTextChangedListener(textWatcherEdit);
        editTextEditDescrizione.addTextChangedListener(textWatcherEdit);
        editTextEditNuumeroMassimoPartecipanti.addTextChangedListener(textWatcherEdit);
        textViewEditLatitudine.addTextChangedListener(textWatcherEdit);
        textViewEditLongitudine.addTextChangedListener(textWatcherEdit);

        editTextEditNomeEvento.setOnFocusChangeListener(this);
        editTextEditDescrizione.setOnFocusChangeListener(this);
        editTextEditNuumeroMassimoPartecipanti.setOnFocusChangeListener(this);
        textViewEditLatitudine.setOnFocusChangeListener(this);
        textViewEditLongitudine.setOnFocusChangeListener(this);


    }
    

    private void refreshData(){

        if(eventModel.getIsImageUploaded() == true) {

            //We have to download the image all over again because the intent allow you to pass just file under the size of 1mb
            Eventi.downloadEventImage(eventModel.getId(), (ClosureBitmap) result -> {
                eventModel.setImageBitmap(result);
                imageViewEditEvent.setImageBitmap(result);
            });

            //Download the url
            Eventi.downloadEventImageUri(eventModel.getId(), new ClosureResult<Uri>() {
                @Override
                public void closure(Uri result) {
                    if(result != null){
                        eventModel.setImageUri(result);
                    }
                }
            });
        }

        //Insert all model information in the view
        editTextEditNomeEvento.setText(eventModel.getNome());
        editTextEditDescrizione.setText(eventModel.getDescrizione());

        //Computing the available places and the number of participant in the queue
        editTextEditNuumeroMassimoPartecipanti.setText("" + eventModel.getNumeroMassimoPartecipanti());

        seekBarStatusProgressEditEvent.setProgress(eventModel.getSogliaAccettazioneStatus());
        textViewEditContatoreStatus.setText("" + eventModel.getSogliaAccettazioneStatus() + "%");


        String[] parts = eventModel.getNeutralData().split(" ");

       displayDateEdit.setText(parts[0]);
       displayTimeEdit.setText(parts[1]);

       textViewEditLatitudine.setText("" + eventModel.getLatitudine());
       textViewEditLongitudine.setText("" + eventModel.getLongitudine());

       if(!eventModel.getIdUtenteCreatore().isEmpty()) {
           creatoreId = eventModel.getIdUtenteCreatore();
       }

       if(!eventModel.getIdGruppoCreatore().isEmpty()) {
           creatoreId = eventModel.getIdUtenteCreatore();
       }

    }




    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }

    /**
     * Controllo che l'utente abbia scritto dati diversi da quelli sul Db.
     */
    TextWatcher textWatcherEdit = new TextWatcher() {
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
        if(eventModel == null) return;


        if(!editTextEditNomeEvento.getText().toString().equals(eventModel.getNome()) || !editTextEditDescrizione.getText().toString().equals(eventModel.getDescrizione())
           || !editTextEditNuumeroMassimoPartecipanti.getText().toString().equals("" + eventModel.getNumeroMassimoPartecipanti()) || immagineSelezionataEdit != null
           || seekBarStatusProgressEditEvent.getProgress() != eventModel.getSogliaAccettazioneStatus() ||
           !textViewEditLatitudine.getText().toString().equals("" + eventModel.getLatitudine()) ||
           !textViewEditLongitudine.getText().toString().equals("" + eventModel.getLongitudine()) ||
           oldDate.compareTo(newDate) != 0 || (!eventModel.getIdUtenteCreatore().equals(idAccountCreatoreEdit) && !eventModel.getIdGruppoCreatore().equals(idAccountCreatoreEdit)))
        {
            saveSettings.setEnabled(true);


        }else{
            saveSettings.setEnabled(false);
        }
    }

    /**
     * Remove the focus from all the components and reset the background color.
     */
    private void clearAllFocusAndColor(){
        editTextEditNomeEvento.clearFocus();
        editTextEditDescrizione.clearFocus();
        editTextEditNuumeroMassimoPartecipanti.clearFocus();
        textViewEditLatitudine.clearFocus();
        textViewEditLongitudine.clearFocus();
        editTextEditNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEditDescrizione.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEditNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewEditLatitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewEditLongitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
    }

    private void disableAllComponent(){
        editTextEditNomeEvento.setEnabled(false);
        editTextEditDescrizione.setEnabled(false);
        editTextEditNuumeroMassimoPartecipanti.setEnabled(false);

        AnimationHelper.fadeIn(progressBarEvent,1000);
    }

    private void enableAllComponent(){
        editTextEditNomeEvento.setEnabled(true);
        editTextEditDescrizione.setEnabled(true);
        editTextEditNuumeroMassimoPartecipanti.setEnabled(true);

        AnimationHelper.fadeOut(progressBarEvent,1000);
    }



    /**
     * Event called when is pressed the button to save the information to the server
     * @param view
     */
    private void onSaveDataButtonClick(View view){

        clearAllFocusAndColor();

        //Disabling all the component and showing the progress bar
        disableAllComponent();
        saveSettings.setEnabled(false);

        if(checkAllNewValues()){

            updateFieldToServer();

        }else{
            enableAllComponent();
            saveSettings.setEnabled(true);
        }
    }

    /**
     * Execute the query to update the new information to the server.
     */
    private void updateFieldToServer(){
        //There's for sure something different to upload to the server
        List<Object> listOfUpdates = new ArrayList<>();


        //nome
        if(!editTextEditNomeEvento.getText().toString().equals(eventModel.getNome())){
            listOfUpdates.add(Eventi.NOME_FIELD);
            listOfUpdates.add(editTextEditNomeEvento.getText().toString());
        }

        //descrizione
        if(!editTextEditDescrizione.getText().toString().equals(eventModel.getDescrizione())){
            listOfUpdates.add(Eventi.DESCRIZIONE_FIELD);
            listOfUpdates.add(editTextEditDescrizione.getText().toString());
        }

        //descrizione
        if(Integer.parseInt(editTextEditNuumeroMassimoPartecipanti.getText().toString()) != eventModel.getNumeroMassimoPartecipanti()){
            listOfUpdates.add(Eventi.MAX_PARTECIPANTI_FIELD);
            listOfUpdates.add(Integer.parseInt(editTextEditNuumeroMassimoPartecipanti.getText().toString()));
        }

        //soglia status
        if(seekBarStatusProgressEditEvent.getProgress() != eventModel.getSogliaAccettazioneStatus()){
            listOfUpdates.add(Eventi.STATUS_SOGLIA_FIELD);
            listOfUpdates.add(seekBarStatusProgressEditEvent.getProgress());
        }

        //latitudine
        if(!textViewEditLatitudine.getText().toString().equals("" + eventModel.getLatitudine())){
            listOfUpdates.add(Eventi.LATITUDINE_FIELD);
            listOfUpdates.add(Double.parseDouble(textViewEditLatitudine.getText().toString()));
        }

        //longitudine
        if(!textViewEditLongitudine.getText().toString().equals("" + eventModel.getLongitudine())){
            listOfUpdates.add(Eventi.LONGITUDINE_FIELD);
            listOfUpdates.add(Double.parseDouble(textViewEditLongitudine.getText().toString()));
        }

        if(newDate.compareTo(oldDate) != 0) {
            listOfUpdates.add(Eventi.DATA_ORA_FIELD);
            listOfUpdates.add(newDate.getTime());
        }

        if(!eventModel.getIdUtenteCreatore().equals(idAccountCreatoreEdit) && !eventModel.getIdGruppoCreatore().equals(idAccountCreatoreEdit)) {

            if(idAccountCreatoreEdit.equals(AuthHelper.getUserId())) {
                listOfUpdates.add(Eventi.UTENTE_CREATORE_FIELD);
                listOfUpdates.add(idAccountCreatoreEdit);
                listOfUpdates.add(Eventi.GRUPPO_CREATORE_FIELD);
                listOfUpdates.add("");
            }else{
                listOfUpdates.add(Eventi.GRUPPO_CREATORE_FIELD);
                listOfUpdates.add(idAccountCreatoreEdit);
                listOfUpdates.add(Eventi.UTENTE_CREATORE_FIELD);
                listOfUpdates.add("");
            }
        }


        //This means that no other information is different rather than the image.
        if(listOfUpdates.size() == 0) {
            //Check if we need to upload the image
            if(immagineSelezionataEdit != null){
                Eventi.uploadEventImage(immagineSelezionataEdit,eventModel.getId(), closureBool -> {
                    if(closureBool){
                        enableAllComponent();
                        saveSettings.setEnabled(false);
                        immagineSelezionataEdit = null;
                        isModified = true;
                        Toast.makeText(this,R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                    }else{
                        enableAllComponent();
                        Toast.makeText(this,R.string.errorUpload,Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //This code should never be executed because the button should be disabled in case of no new data.
                enableAllComponent();
                saveSettings.setEnabled(false);
                Toast.makeText(this,R.string.noUpdates,Toast.LENGTH_SHORT).show();
            }
            return;
        };

        //put the first two as first parameters
        String firstField = (String) listOfUpdates.get(0);;
        Object firstValue = listOfUpdates.get(1);;
        if(listOfUpdates.size() > 2){
            listOfUpdates.remove(0);
            listOfUpdates.remove(0);
        }

        Eventi.updateFields(eventModel.getId(), closureBool -> {
            if(closureBool){
                //check if we have to upload also the image
                if(immagineSelezionataEdit != null){
                    Eventi.uploadEventImage(immagineSelezionataEdit,eventModel.getId(),closureBool1 -> {
                        if(closureBool1){
                            enableAllComponent();
                            saveSettings.setEnabled(false);
                            immagineSelezionataEdit = null;
                            isModified = true;
                            Toast.makeText(this,R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                        }else{
                            enableAllComponent();
                            Toast.makeText(this,R.string.errorUpload,Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    enableAllComponent();
                    saveSettings.setEnabled(false);
                    updateModel();
                    isModified = true;
                    Toast.makeText(this,R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                }
            }else{
                enableAllComponent();
                Toast.makeText(this,R.string.errorUpload,Toast.LENGTH_SHORT).show();
            }
        }, firstField,firstValue,listOfUpdates.toArray());
    }

    /**
     * Update the model with the new information after the server update.
     */
    private void updateModel() {

        eventModel.setNome(editTextEditNomeEvento.getText().toString());
        eventModel.setDescrizione(editTextEditDescrizione.getText().toString());
        eventModel.setNumeroMassimoPartecipanti(Integer.parseInt(editTextEditNuumeroMassimoPartecipanti.getText().toString()));
        eventModel.setSogliaAccettazioneStatus(seekBarStatusProgressEditEvent.getProgress());
        eventModel.setLatitudine(Double.parseDouble(textViewEditLatitudine.getText().toString()));
        eventModel.setLongitudine(Double.parseDouble(textViewEditLongitudine.getText().toString()));
        eventModel.setDataOra(newDate.getTime());

        oldDate = Calendar.getInstance();
        oldDate.setTime(eventModel.getDataOra());


        if (idAccountCreatoreEdit.equals(AuthHelper.getUserId())) {
            eventModel.setUtenteCreatore(idAccountCreatoreEdit);
            eventModel.setGruppoCreatore("");
        } else {
            eventModel.setGruppoCreatore(idAccountCreatoreEdit);
            eventModel.setUtenteCreatore("");
        }
    }



    public void onClickPhotoEditEvent(View view) {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 211);
    }

    public void onMap(View view) {

        Intent mapActivity = new Intent(this, MapActivity.class);
        mapActivity.putExtra("isNotNull", (valoreLatitudineEdit != null && valoreLongitudineEdit != null));
        mapActivity.putExtra("latitudine", valoreLatitudineEdit);
        mapActivity.putExtra("longitudine",valoreLongitudineEdit);
        startActivityForResult(mapActivity, 220);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 220) {


            if(resultCode == RESULT_OK) {
                boolean isNull = data.getBooleanExtra("isNull", true);

                if(isNull == true) {
                    textViewEditLatitudine.setVisibility(View.INVISIBLE);
                    textViewEditLongitudine.setVisibility(View.INVISIBLE);
                }else{
                    valoreLatitudineEdit = data.getDoubleExtra("latitudine",0);
                    valoreLongitudineEdit = data.getDoubleExtra("longitudine",0);
                    textViewEditLatitudine.setVisibility(View.VISIBLE);
                    textViewEditLongitudine.setVisibility(View.VISIBLE);
                    textViewEditLatitudine.setText("" + valoreLatitudineEdit);
                    textViewEditLongitudine.setText("" + valoreLongitudineEdit);

                }
            }
        }


        if (resultCode == RESULT_OK) {
            // compare the resultCode with the
            // SELECT_PICTURE constant

            if (requestCode == 211) {

                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // Compress it before it is shown into the imageView
                    imageViewEditEvent.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(), selectedImageUri, imageViewEditEvent));
                    immagineSelezionataEdit = selectedImageUri;
                    checkSaveButtonActivation();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        if(isModified) setResult(RESULT_OK);
        super.onBackPressed();
        finish();
    }

    public void onBackButtonPressed(View view) {
        onBackPressed();
    }

    /*
    Metodo Utilizzato per effettuare i controlli in input da parte dell'utente nella creazione Evento
    */
    private boolean checkAllNewValues() {
        boolean flag = true;
        String nomeEventoEdit, descrizioneEventoEdit, numeroPartecipantiEventoEdit, selezioneDataEdit, selezionaOraEdit;

        nomeEventoEdit = editTextEditNomeEvento.getText().toString();
        descrizioneEventoEdit = editTextEditDescrizione.getText().toString();
        numeroPartecipantiEventoEdit = editTextEditNuumeroMassimoPartecipanti.getText().toString();
        selezioneDataEdit = displayDateEdit.getText().toString();
        selezionaOraEdit = displayTimeEdit.getText().toString();


        if (validazioneNomeEventoEdit(nomeEventoEdit) == false || nomeEventoEdit.isEmpty() ||
                validazioneDescrizioneEventoEdit(descrizioneEventoEdit) == false || descrizioneEventoEdit.isEmpty() ||
                numeroPartecipantiEventoEdit.isEmpty() || validazioneNumeroPartecipantiEventoEdit(numeroPartecipantiEventoEdit) == false ||
                selezioneDataEdit.equals("Select date") || selezioneDataEdit.equals("Seleziona data") ||
                selezionaOraEdit.equals("Select time") || selezionaOraEdit.equals("Seleziona ora") ) {

            if (validazioneNomeEventoEdit(nomeEventoEdit) == false || nomeEventoEdit.isEmpty()) {
                editTextEditNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            } else {
                editTextEditNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if (validazioneDescrizioneEventoEdit(descrizioneEventoEdit) == false || descrizioneEventoEdit.isEmpty()) {
                editTextEditDescrizione.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            } else {
                editTextEditDescrizione.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if (numeroPartecipantiEventoEdit.isEmpty() || validazioneNumeroPartecipantiEventoEdit(numeroPartecipantiEventoEdit) == false) {
                editTextEditNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            } else {
                editTextEditNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if (selezioneDataEdit.equals("Select date") || selezioneDataEdit.equals("Seleziona data") ||
                    selezionaOraEdit.equals("Select time") || selezionaOraEdit.equals("Seleziona ora")) {
                flag = false;
                Toast.makeText(this, getString(R.string.messaggioDataOraEvento), Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, getString(R.string.campiErratiRegister), Toast.LENGTH_LONG).show();

        } else {
            backgroundTintEditTextCreateEventEdit();
        }

        return flag;
    }


    private void backgroundTintEditTextCreateEventEdit() {
        editTextEditNomeEvento.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEditDescrizione.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        editTextEditNuumeroMassimoPartecipanti.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewEditLatitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        textViewEditLongitudine.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));

    }


    /**
     * Controllo che il nome in input rispetti le seguenti caratteristiche:
     * controlla che contenga la stringa solo caratteri letterali, e inoltre che non sia
     * vuota o che contenga solo spazi.
     * @param controlloNomeEvento stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneNomeEventoEdit(String controlloNomeEvento) {

        return !controlloNomeEvento.isEmpty();
    }


    /**
     * Controlla che la descrizione non contenga solo caratteri di spaziatura
     * @param controlloDescrizioneEvento stringa da controllare
     * @return ritorna true se la stringa è formattata correttamente, altrimenti false
     */
    public boolean validazioneDescrizioneEventoEdit(String controlloDescrizioneEvento) {

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
    public boolean validazioneNumeroPartecipantiEventoEdit(String controlloNumeroPartecipantiEvento) {

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


    /*
    Metodo per l'eliminazione di un evento che si è creato o tramite il proprio account o tramite il gruppo.
     */
    public void onDeleteEvent(View view) {

        String myId = AuthHelper.getUserId();
        String idEvent = eventModel.getId();
        AlertDialog.Builder alertDelete = new  AlertDialog.Builder(EditEvent.this);
        alertDelete.setTitle(R.string.titleDeleteEvent);
        alertDelete.setMessage(R.string.msgDeleteEvent);

        /* Se il campo idUtenteCreatore non è vuoto, vuol dire che il creatore dell'evento è un utente. */
        if(!eventModel.getIdUtenteCreatore().isEmpty()) {

            /* Controlla che l'id dell'utente loggato sia uguale all'id dell'utente creatore */
            if(AuthHelper.getUserId().equals(eventModel.getIdUtenteCreatore())) {

                // Nel caso di risposta positiva nel dialog
                alertDelete.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Elimina il tuo evento
                        Eventi.deleteEvent(eventModel.getId(), closureBool -> {
                            if(closureBool){
                                Toast.makeText(EditEvent.this, R.string.deleteSuccessEvent, Toast.LENGTH_SHORT).show();

                                Intent i = new Intent();
                                setResult(RESULT_OK, i);
                                finish();
                            }
                        });


                    }
                });


                // Nel caso di risposta negativa nel dialog, stampa solo
                alertDelete.setNegativeButton(R.string.confirmNegative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //tost conferma evento non eliminato!
                        Toast.makeText(EditEvent.this, R.string.deleteInsuccess, Toast.LENGTH_SHORT).show();
                    }
                });
                alertDelete.create().show();

            }
        }


        /* Se il campo idUGruppoCreatore non è vuoto, vuol dire che il creatore dell'evento è un gruppo. */
        if(!eventModel.getIdGruppoCreatore().isEmpty()) {

            /* Prendo con il metodo tutti i gruppi nei quali l'utente loggato è amministratore */
            Gruppi.getAdministrationGroups(closureList -> {

                if (closureList != null) {
                    /* Creo un iterator per poter iterare la lista di tutti i gruppi in modo tale da poterli
                    confrontare successivamente */
                    Iterator<Gruppo> it = closureList.iterator();
                    while(it.hasNext()) {
                        Gruppo oggettoGruppo = it.next();
                        // Controlla che l'id del gruppo corrisponda a quello impostato nella creazione dell'evento
                        if(oggettoGruppo.getId().equals(eventModel.getIdGruppoCreatore())) {

                            // Nel caso di risposta positiva nel dialog
                            alertDelete.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Elimina il tuo evento
                                    Eventi.deleteEvent(eventModel.getId(), closureBool -> {
                                        if(closureBool){
                                            Toast.makeText(EditEvent.this, R.string.deleteSuccessEvent, Toast.LENGTH_SHORT).show();

                                            Intent i = new Intent();
                                            setResult(RESULT_OK, i);
                                            finish();
                                        }
                                    });


                                }
                            });


                            // Nel caso di risposta negativa nel dialog, stampa solo
                            alertDelete.setNegativeButton(R.string.confirmNegative, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //tost conferma evento non eliminato!
                                    Toast.makeText(EditEvent.this, R.string.deleteInsuccess, Toast.LENGTH_SHORT).show();
                                }
                            });
                            alertDelete.create().show();

                        }

                    }

                }

            });

        }


    }






}