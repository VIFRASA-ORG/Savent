package com.vitandreasorino.savent.Utenti.GruppiTab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.Utenti.GruppiTab.CreazioneGruppo.AddContacts;
import com.vitandreasorino.savent.R;
import com.vitandreasorino.savent.Utenti.Notification.NotificationActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureResult;
import Model.DB.Gruppi;
import Model.DB.Utenti;

import Model.Pojo.Gruppo;
import Model.Pojo.Utente;

public class GroupDetailActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnFocusChangeListener{

    private static final int ADD_CONTACTS_RESULT = 11;

    private boolean isModified = false;
    private boolean isCreated = false;

    Gruppo groupModel;
    Utente admin;

    ImageView imageViewDetailGroup;
    TextView editProfilePhotoGroup;
    Uri newSelectedImage;
    EditText nameDetailGroup;
    EditText descriptionDetailGroup;
    Button leaveGroup;
    Button deleteGroup;
    View viewEditGroupPhoto;
    ListView componentListView;
    SearchView searchView;
    ImageView buttonSaveDataGroup;
    ProgressBar progressBar;
    ComponentGroupAdapter adapter;
    ProgressBar progressBarPage;
    TextView emptyTextView;
    FloatingActionButton buttonAddUserToGroup;
    TextView textAddUserToGroup;

    ArrayList<Utente> groupComponentsOriginal = new ArrayList<>();
    ArrayList<Utente> groupComponentsUpdated = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        //Deserializing the object from the intent
        groupModel = (Gruppo) getIntent().getSerializableExtra("IdGrouppoLista");
        isCreated = getIntent().getBooleanExtra("Creato", false);

        //Inflate all the component
        inflateAll();

        boolean fromNotification = getIntent().getBooleanExtra(NotificationActivity.FROM_NOTIFICATION_INTENT,false);
        if(fromNotification){
            String groupId = getIntent().getStringExtra("groupId");

            Gruppi.getGroup(groupId, group -> {
                groupModel = group;
                continueDownload();
            });
        }else{
            //Deserializing the object from the intent
            groupModel = (Gruppo) getIntent().getSerializableExtra("IdGrouppoLista");
            continueDownload();
        }


        /**
         * Metodo attivabile tramite una pressione prolungata sul singolo componente del gruppo.
         */
        componentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // ricerca dell'utente che si vuole eliminare con relativo id e id del gruppo di appartenenza
                Utente utente = (adapter.getFilteredData().size() == 0) ? adapter.getNoFilteredData().get(position): adapter.getFilteredData().get(position);
                String idUser = utente.getId();
                String idGroup = groupModel.getId();

                // se si è admin di un gruppo allora abilità il dialog che richiede se vuoi eliminare o no l'utente selezionato dal gruppo
                if(AuthHelper.getUserId().equals(groupModel.getIdAmministratore())){

                    AlertDialog.Builder alertRemoveUser = new  AlertDialog.Builder(GroupDetailActivity.this);
                    alertRemoveUser.setTitle(R.string.removeUserFromTheGroup);
                    alertRemoveUser.setMessage(R.string.confirmRemoveUserFromTheGroup);
                    alertRemoveUser.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //se l'utente selezionato che si vuole eliminare corrisponde all'admin allora avvisa che non può auto-eliminarsi
                            if(idUser.equals(groupModel.getIdAmministratore())){
                                Toast.makeText(GroupDetailActivity.this, R.string.msgYouAreAdmin, Toast.LENGTH_LONG).show();

                            } else {

                                //altrimenti rimuovi l'utente selezionato
                                Gruppi.removeUserFromGroup(idUser, idGroup, closureBool -> {
                                    if(closureBool){
                                        Toast.makeText(GroupDetailActivity.this, R.string.confirmDelete, Toast.LENGTH_LONG).show();
                                        adapter.removeItemFromList(utente);
                                        adapter.notifyDataSetChanged();
                                        //aggiorna quanto cancelli un componente dal gruppo
                                        updateIntent();
                                    }
                                });
                            }

                        }//fine onClick
                    });

                    //nel acaso di risposta negativa
                    alertRemoveUser.setNegativeButton(R.string.confirmNegative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(GroupDetailActivity.this, R.string.noConfirmDelete, Toast.LENGTH_SHORT).show();
                        }
                    });
                    alertRemoveUser.create().show();

                }
                return false;
            }
        });


        /**
         * metodo che permette di selezionare la nuova immagine
         */
        editProfilePhotoGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageProfile = new Intent();
                imageProfile.setType("image/*");
                imageProfile.setAction(Intent.ACTION_GET_CONTENT);
                // pass the constant to compare it
                // with the returned requestCode
                startActivityForResult(Intent.createChooser(imageProfile, "Select Picture"), 200);
            }
        });

        /**
         * pulsante SALVA che permette di salvare gli eventuali dati modificati
         */
        buttonSaveDataGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveDataButtonClick(v);
            }
        });

        //settaggio delle informazioni
        nameDetailGroup.addTextChangedListener(textWatcher);
        descriptionDetailGroup.addTextChangedListener(textWatcher);
        nameDetailGroup.setOnFocusChangeListener(this);
        descriptionDetailGroup.setOnFocusChangeListener(this);
        searchView.setOnQueryTextListener(this);

        checkSaveButtonActivation();

    }//fine onCreate

    private void continueDownload(){
        //Inserisci il nome e descrizione del gruppo all'interno della vista
        nameDetailGroup.setText(groupModel.getNome());
        descriptionDetailGroup.setText(groupModel.getDescrizione());

        //Scarica l'immagine del gruppo dal db
        if(groupModel.getIsImmagineUploaded()){
            Gruppi.downloadGroupImage(groupModel.getId(), bitmap -> {

                if(bitmap != null){
                    imageViewDetailGroup.setImageBitmap(bitmap);
                    groupModel.setImmagineBitmap(bitmap);
                }
            });
        }
        functionsBasedOnRole();
        downloadDataList();
    }

    /**
     * Metodo che permette di attivare e disattivare le funzioni in base al ruolo di appartenenza
     */
    private void functionsBasedOnRole() {

        if(groupModel.getIdAmministratore().equals(AuthHelper.getUserId())){
            deleteGroup.setEnabled(true);
            deleteGroup.setVisibility(View.VISIBLE);
            textAddUserToGroup.setEnabled(true);
            textAddUserToGroup.setVisibility(View.VISIBLE);
            buttonAddUserToGroup.setEnabled(true);
            buttonAddUserToGroup.setVisibility(View.VISIBLE);
        } else {

            leaveGroup.setEnabled(true);
            leaveGroup.setVisibility(View.VISIBLE);
            nameDetailGroup.setEnabled(false);
            descriptionDetailGroup.setEnabled(false);
            editProfilePhotoGroup.setEnabled(false);
            editProfilePhotoGroup.setVisibility(View.INVISIBLE);
            viewEditGroupPhoto.setVisibility(View.INVISIBLE);
            buttonSaveDataGroup.setEnabled(false);
            buttonSaveDataGroup.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * metodo che indica che tale modifica è andata a buon fine
     */
    private void updateIntent() {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
    }

    /**
     * metodo che permette di visualizzare la lista seè piena o di non visualizzarla se è vuota
     * @param isDownloading
     */
    private void toggleDownloadingElements(boolean isDownloading){
        if(isDownloading){
            emptyTextView.setVisibility(View.GONE);
            AnimationHelper.fadeIn(progressBarPage,0);
        }else{
            AnimationHelper.fadeOut(progressBarPage,0);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * metodo che carica le info da db è nel momento di caricamento visualizza la progressbar
     */
    private void downloadDataList() {
        toggleDownloadingElements(true);

        //istanzia l'adapter personalizzato
        adapter = new ComponentGroupAdapter(this, groupModel.getIdAmministratore());
        componentListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //Scarica lista componenti con rispettiva immagine del profilo associata
        if(!groupModel.getIdComponenti().isEmpty()){

            for(String id : groupModel.getIdComponenti()){

                Utenti.getUser(id, utente -> {
                    if(utente != null){
                        if(utente.getId().equals(AuthHelper.getUserId())) admin = utente;
                        adapter.addItemToList(utente);
                        groupComponentsOriginal.add(utente);
                        groupComponentsUpdated.add(utente);
                        adapter.notifyDataSetChanged();

                        if(utente.getIsProfileImageUploaded()){
                            Utenti.downloadUserImage(utente.getId(), (ClosureResult<File>) file -> {
                                utente.setProfileImageUri(Uri.fromFile(file));
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                });
            }
        }
        toggleDownloadingElements(false);
    }


    /**
     * Evento chiamato quando viene premuto il pulsante per salvare le informazioni sul server
     * @param view
     */
    private void onSaveDataButtonClick(View view){

        clearAllFocusAndColor();

        //Disabilitazione di tutti i componenti e visualizzazione della Progress Bar
        disableAllComponent();
        buttonSaveDataGroup.setEnabled(false);

        if(checkAllNewValues()){
            updateFieldToServer();
        }else{
            //aggiornare le nuove informazioni sul server
            enableAllComponent();
            buttonSaveDataGroup.setEnabled(true);
        }
    }

    /**
     * Eseguire la query per aggiornare le nuove informazioni sul server.
     */
    private void updateFieldToServer(){
        //C'è sicuramente qualcosa di diverso da caricare sul server
        List<Object> listOfUpdates = new ArrayList<>();

        // carica nome
        if(!nameDetailGroup.getText().toString().equals(groupModel.getNome())){
            listOfUpdates.add(Gruppi.NOME_FIELD);
            listOfUpdates.add(nameDetailGroup.getText().toString());
        }

        // carica descrizione
        if(!descriptionDetailGroup.getText().toString().equals(groupModel.getDescrizione())){
            listOfUpdates.add(Gruppi.DESCRIZIONE_FIELD);
            listOfUpdates.add(descriptionDetailGroup.getText().toString());
        }

        //cariamento componenti
        ArrayList<Utente> copy = new ArrayList<>(groupComponentsOriginal);
        copy.retainAll(groupComponentsUpdated);

        if(groupComponentsOriginal.size() != groupComponentsUpdated.size() || copy.size() != groupComponentsOriginal.size() ){
            //modifiche
            ArrayList<String> componentsId = new ArrayList<>();
            for(Utente u : groupComponentsUpdated) componentsId.add(u.getId());

            listOfUpdates.add(Gruppi.COMPONENTI_FIELD);
            listOfUpdates.add(componentsId);
        }

        if(listOfUpdates.size() == 0) {
            //Controlla se è necessario caricare l'immagine
            if(newSelectedImage != null){
                Gruppi.uploadGroupImage(newSelectedImage,groupModel.getId(),closureBool -> {
                    if(closureBool){
                        enableAllComponent();
                        buttonSaveDataGroup.setEnabled(false);
                        newSelectedImage = null;
                        isModified = true;
                        //toast relativo alla sola immagine del grouppo
                        Toast.makeText(this,R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                    }else{
                        enableAllComponent();
                        Toast.makeText(this,R.string.errorUpload,Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                //Questo codice non dovrebbe mai essere eseguito perché il pulsante dovrebbe essere disabilitato in caso di assenza di nuovi dati.
                enableAllComponent();
                buttonSaveDataGroup.setEnabled(false);
                Toast.makeText(this,R.string.noUpdates,Toast.LENGTH_SHORT).show();
            }

            return;
        };

        //Inserisci i primi due come primi parametri
        String firstField = (String) listOfUpdates.get(0);
        Object firstValue = listOfUpdates.get(1);
        if(listOfUpdates.size() > 2){
            listOfUpdates.remove(0);
            listOfUpdates.remove(0);
        }

        /**
         * metodo per aggiornare i campi del gruppo
         */
        Gruppi.updateFields(groupModel.getId(), closureBool -> {
            if(closureBool){
                //controlla se dobbiamo caricare anche l'immagine
                if(newSelectedImage != null){
                    Gruppi.uploadGroupImage(newSelectedImage,groupModel.getId(),closureBool1 -> {
                        if(closureBool1){
                            enableAllComponent();
                            buttonSaveDataGroup.setEnabled(false);
                            newSelectedImage = null;
                            isModified = true;
                            //tost quando si carica l'immagine
                            Toast.makeText(this,R.string.informationUploaded,Toast.LENGTH_SHORT).show();
                        }else{
                            enableAllComponent();
                            Toast.makeText(this,R.string.errorUpload,Toast.LENGTH_SHORT).show();
                        }
                    });
                }else{
                    enableAllComponent();
                    buttonSaveDataGroup.setEnabled(false);
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
     * Aggiorna il modello con le nuove informazioni dopo l'aggiornamento del server.
     */
    private void updateModel() {
        groupModel.setNome(nameDetailGroup.getText().toString());
        groupModel.setDescrizione(descriptionDetailGroup.getText().toString());
    }

    private void enableAllComponent() {
        nameDetailGroup.setEnabled(true);
        descriptionDetailGroup.setEnabled(true);
        editProfilePhotoGroup.setEnabled(true);

        AnimationHelper.fadeOut(progressBar,1000);
    }

    /**
     * Check all the new information entered in the nome, cognome  fields.
     * @return true if all the new values are of the correct pattern, false otherwise.
     */
    private boolean checkAllNewValues(){
        boolean flag = true;

        String nome,descrizione;
        nome = nameDetailGroup.getText().toString();
        descrizione = descriptionDetailGroup.getText().toString();

        if(nome.isEmpty() || descrizione.isEmpty()){

            if(nome.isEmpty()) {
                nameDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else {
                nameDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

            if(descrizione.isEmpty()) {
                descriptionDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF0000")));
                flag = false;
            }else {
                descriptionDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
            }

        }

        return flag;
    }


    private void disableAllComponent(){
        nameDetailGroup.setEnabled(false);
        descriptionDetailGroup.setEnabled(false);
        editProfilePhotoGroup.setEnabled(false);

        AnimationHelper.fadeIn(progressBar,1000);
    }

    /**
     * Remove the focus from all the components and reset the background color.
     */
    private void clearAllFocusAndColor(){

        nameDetailGroup.clearFocus();
        descriptionDetailGroup.clearFocus();
        nameDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        descriptionDetailGroup.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));

    }

    /**
     * Method used to get all the interface reference from the xml file
     */
    private void inflateAll() {
        nameDetailGroup = findViewById(R.id.nameDetailGroup);
        editProfilePhotoGroup = findViewById(R.id.textEditGroupPhoto);
        descriptionDetailGroup = findViewById(R.id.descriptionContentTextViewGroup);
        imageViewDetailGroup = findViewById(R.id.imageViewDetailGroup);
        componentListView = findViewById(R.id.componenGrouptListView);
        searchView = findViewById(R.id.searchViewComponentGroup);
        buttonSaveDataGroup = findViewById(R.id.buttonSaveDataGroup);
        progressBar = findViewById(R.id.progressBarGroup);
        leaveGroup = findViewById(R.id.leavegroupButton);
        deleteGroup = findViewById(R.id.deleteGroupButton);
        viewEditGroupPhoto = findViewById(R.id.viewEditGroupPhoto);
        buttonAddUserToGroup = findViewById(R.id.buttonAddUserToGroup);
        textAddUserToGroup = findViewById(R.id.textAddUserToGroup);

        progressBarPage = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
        componentListView.setEmptyView(findViewById(R.id.emptyResults));
    }

    @Override
    public void onBackPressed() {
        if(isModified) setResult(RESULT_OK);

        if(isCreated){
            Intent i = new Intent("UpdateGroup");
            i.putExtra("Updated", true);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }

        super.onBackPressed();
        finish();
    }

    /**
     * Evento tramite Click che permette di tornare indietro con eventauale aggiornamento
     * @param view
     */
    public void onBackButtonPressed(View view){
        onBackPressed();
    }

    /**
     * tasto che permette all'admin di cancellare il gruppo da lui creato
     * @param view
     */
    public void onDeleteGroup(View view) {
        String myId = AuthHelper.getUserId();
        String idGroup = groupModel.getId();
        AlertDialog.Builder alertDelete = new  AlertDialog.Builder(GroupDetailActivity.this);
        alertDelete.setTitle(R.string.titleDeleteGroup);
        alertDelete.setMessage(R.string.msgDeleteGroup);

        /**
         * Se si è admin del gruppo allora posso visualizzare il dialog per poter eliminare tale gruppo
         */
        if(AuthHelper.getUserId().equals(groupModel.getIdAmministratore())){
            alertDelete.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Elimina il tuo gruppo
                    Gruppi.deleteGroup(groupModel.getId(), closureBool -> {
                        if(closureBool){
                            Toast.makeText(GroupDetailActivity.this, R.string.deleteSuccess, Toast.LENGTH_SHORT).show();

                            Intent i = new Intent();
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    });

                  String.valueOf(groupModel.getIdComponenti().size());

                }
            });

            //Nel caso di risposta negativa nel dialog, stampa solo
            alertDelete.setNegativeButton(R.string.confirmNegative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //tost conferma gruppo non eliminato!
                    Toast.makeText(GroupDetailActivity.this, R.string.deleteInsuccess, Toast.LENGTH_SHORT).show();
                }
            });
            alertDelete.create().show();

        }

    }

    /**
     * Buttone che permette all'utente di abbandonare il gruppo dove è iscritto
     * @param view
     */
    public void onLeaveGroup(View view) {
        String myId = AuthHelper.getUserId();
        String idGroup = groupModel.getId();
        AlertDialog.Builder alertLeave = new  AlertDialog.Builder(GroupDetailActivity.this);
        alertLeave.setTitle(R.string.titleLeaveGroup);
        alertLeave.setMessage(R.string.msgLeaveGroup);

        /**
         * Se non si è admin del gruppo allora posso visualizzare il dialog per poter abbandonare tale gruppo
         */
        if(!AuthHelper.getUserId().equals(groupModel.getIdAmministratore())){

            //Nel caso di risposta positiva nel dialog
            alertLeave.setPositiveButton(R.string.confirmPositive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Rimuoviti dal gruppo
                    Gruppi.removeUserFromGroup(AuthHelper.getUserId(), groupModel.getId(), closureBool -> {
                        if(closureBool){

                            //tost che conferma abbandono avvenuto con successo!
                            Toast.makeText(GroupDetailActivity.this, R.string.leaveSuccess, Toast.LENGTH_LONG).show();

                            Intent i = new Intent();
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    });

                }
            });

            //Nel caso di risposta negativa nel dialog, stampa solo
            alertLeave.setNegativeButton(R.string.confirmNegative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //tost conferma utente non eliminato!
                    Toast.makeText(GroupDetailActivity.this, R.string.leaveInSuccess, Toast.LENGTH_SHORT).show();
                }
            });
            alertLeave.create().show();

        }
    }//fine onLeave

    /*

        OVERRIDE OF THE METHODs IN THE SearchView.OnQueryTextListener INTERFACE
        force the adapter to filter the ListView item based on the query in the Search bar.

     */

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }
    /**
     * Enable or disable the button to save the changes only if there are some changes in the account info.
     */
    private void checkSaveButtonActivation(){
        if(groupModel == null) return;

        ArrayList<Utente> copy = new ArrayList<>(groupComponentsOriginal);
        copy.retainAll(groupComponentsUpdated);

        if(!nameDetailGroup.getText().toString().equals(groupModel.getNome()) || !descriptionDetailGroup.getText().toString().equals(groupModel.getDescrizione()) ||
            newSelectedImage != null || groupComponentsOriginal.size() != groupComponentsUpdated.size() || copy.size() != groupComponentsOriginal.size() ){
            buttonSaveDataGroup.setEnabled(true);
        } else {
            buttonSaveDataGroup.setEnabled(false);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (resultCode == RESULT_OK) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageViewDetailGroup.setImageURI(selectedImageUri);
                    newSelectedImage = selectedImageUri;
                    checkSaveButtonActivation();
                }
            }
        }else if(requestCode == ADD_CONTACTS_RESULT){
            if(resultCode == RESULT_OK){
                List<Utente> selectedUsers = data.getParcelableArrayListExtra(AddContacts.EXTRA_ARRAY_CHECKED_CONTACTS);

                for(Utente u: selectedUsers){
                    if(u.getIsProfileImageUploaded()){
                        Utenti.downloadUserImage(u.getId(), new ClosureResult<File>() {
                            @Override
                            public void closure(File result) {
                                u.setProfileImageUri(Uri.fromFile(result));
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                if(adapter.getNoFilteredData().size() >= 1) adapter.mergeNewList(selectedUsers);
                else adapter.addItemsToList(selectedUsers);

                adapter.addItemToList(admin);
                groupComponentsUpdated = adapter.getNoFilteredData();

                adapter.notifyDataSetChanged();
                checkSaveButtonActivation();
            }
        }
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            v.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#AAAAAA")));
        }
    }


    public void buttonAddUserToGroup(View view) {

        //Toast.makeText(GroupDetailActivity.this, "Creare evento", Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, AddContacts.class);
        i.putParcelableArrayListExtra(AddContacts.EXTRA_ARRAY_CHECKED_CONTACTS,groupComponentsUpdated);
        startActivityForResult(i,ADD_CONTACTS_RESULT);
    }

}//fine classe GroupDetailActivity