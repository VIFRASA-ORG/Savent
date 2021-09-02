package com.vitandreasorino.savent.Utenti.GruppiTab.CreazioneGruppo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.Utenti.GruppiTab.ComponentGroupAdapter;
import com.vitandreasorino.savent.Utenti.GruppiTab.GroupDetailActivity;
import com.vitandreasorino.savent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Helper.ImageHelper;
import Model.Closures.ClosureResult;
import Model.POJO.Contact;
import Model.DAO.Gruppi;
import Model.DAO.Utenti;
import Model.POJO.Gruppo;
import Model.POJO.Utente;

public class AddGroup extends AppCompatActivity {

    FloatingActionButton buttonNewContactGroup;
    ImageView imageNewGroup;
    ListView listView;
    ArrayList<Contact> contactsGroupList = new ArrayList<>();
    LinearLayout emptyListLayout;
    ProgressBar progressBar;
    TextView selectPhotoTextView;

    ComponentGroupAdapter adapter;

    private static final int ADD_NEW_CONTACT_RESULT = 11;
    private static final int PHOTO_LIBRARY_RESULT = 10;


    ImageView buttonSaveDataGroup;
    EditText editTextGroupName;
    EditText editTextDescription;

    Uri selectedGroupProfileImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        listView = findViewById(R.id.contactGroupList);
        emptyListLayout = findViewById(R.id.emptyListLayout);
        listView.setEmptyView(emptyListLayout);
        buttonSaveDataGroup = findViewById(R.id.buttonSaveDataGroup);
        editTextGroupName = findViewById(R.id.editNameGroup);
        editTextDescription = findViewById(R.id.editDescrGroup);
        imageNewGroup = findViewById(R.id.imageNewGroup);
        progressBar = findViewById(R.id.progressBar);
        selectPhotoTextView = findViewById(R.id.selectPhotoTextView);
        buttonNewContactGroup = findViewById(R.id.buttonNewContactGroup);

        editTextGroupName.addTextChangedListener(textWatcher);
        editTextDescription.addTextChangedListener(textWatcher);
        buttonSaveDataGroup.setEnabled(false);

        adapter = new ComponentGroupAdapter(this);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Metodo che richiama la successiva classe abbinata allo scopo di aggiungere nuovi contatti al gruppo
     * cliccando il floating button "aggiungi contatto"
     * @param view: la nuova vista indicante la lista dei contatti da poter aggiungere
     */
    public void onNewContactClick (View view) {
        Intent addNewContact = new Intent(this, AddContacts.class);
        ArrayList<Utente> l = new ArrayList<>();
        l.addAll(adapter.getNoFilteredData());
        addNewContact.putParcelableArrayListExtra(AddContacts.EXTRA_ARRAY_CHECKED_CONTACTS,l);
        startActivityForResult(addNewContact,ADD_NEW_CONTACT_RESULT);
    }

    /**
     * Metodo che riporta nella schermata precedente
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

    /**
     * Metodo per la definizione dell'immagine del profilo dell'utente
     * @param view :la nuova vista indicante il profilo con l'immagine profilo modificata
     */
    public void onClickPhoto(View view){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        //Viene passata la costante per il confronto con il codice requestCode ritornato
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PHOTO_LIBRARY_RESULT);
    }

    /**
     * Metodo che serve per la definizione delle attività rispetto alle scelte relative alla modifica dell'immagine profilo.
     * @param requestCode :codice di richiesta intero originariamente fornito a startActivityForResult(),
     * che consente di identificare da chi proviene questo risultato
     * @param resultCode :codice risultato intero restituito dall'attività figlia tramite il suo setResult().
     * @param data :un intento che può restituire i dati della modifica al chiamante.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_LIBRARY_RESULT) {

            // Confronta il resultCode con la costante SELECT_PICTURE
            if (resultCode == RESULT_OK) {
                //Si ottiene l'url dell'immagine dal database
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    //Si modifica l'anteprima dell'immagine nel layout
                    //Si comprime prima che sia mostrata nel componente imageView
                    imageNewGroup.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(),selectedImageUri,imageNewGroup));
                    selectedGroupProfileImage = selectedImageUri;
                }
            }
        }else if (requestCode == ADD_NEW_CONTACT_RESULT){
            //Si restituisce il risultato nell'intent dell'aggiunta del contatto
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

                adapter.notifyDataSetChanged();
            }
        }

        checkSaveButtonEnable();
    }

    /**
     * Si verifica se l'utente sta scrivendo alcune nuove informazioni differenti da quelle presenti sul server
     */
    TextWatcher textWatcher = new TextWatcher() {

        /**
         * Metodo che viene chiamato per avvisarti che, all'interno di s, i countcaratteri che iniziano a start stanno per
         * essere sostituiti da un nuovo testo con lunghezza after.
         * @param s
         * @param start
         * @param count
         * @param after
         */
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        /**
         * Metodo che viene chiamato per avvisarti che, all'interno di s, i countcaratteri che iniziano a start hanno appena sostituito
         * il vecchio testo che aveva lunghezza before. È un errore tentare di apportare modifiche ad s da questo callback.
         * @param s
         * @param start
         * @param before
         * @param count
         */
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkSaveButtonEnable();
        }

        /**
         * Metodo che viene chiamato per informarti che, da qualche parte all'interno di s, il testo è stato modificato.
         * @param s
         */
        @Override
        public void afterTextChanged(Editable s) { }
    };

    /**
     * Si attiva o disattiva il bottone del Salva Modifiche in base allo stato dell'edit text
     */
    private void checkSaveButtonEnable(){
        if(!editTextDescription.getText().toString().isEmpty() && !editTextGroupName.getText().toString().isEmpty() && adapter.getNoFilteredData().size() != 0){
            buttonSaveDataGroup.setEnabled(true);
        }else buttonSaveDataGroup.setEnabled(false);
    }

    /**
     * Si attiva il salvataggio quando l'utente clicca sul relativo button
     * @param view :la nuova vista con i nuovi dati aggiornati.
     */
    public void onSaveButtonClick(View view){
        toggleDownloadMode(true);

        //Si salvano tutte le informazioni presenti sul server
        Gruppo newGroup = new Gruppo();
        newGroup.setNome(editTextGroupName.getText().toString());
        newGroup.setDescrizione(editTextDescription.getText().toString());
        newGroup.setIdAmministratore(AuthHelper.getUserId());
        newGroup.setImmagine(selectedGroupProfileImage);

        List<String> idComponenti = new ArrayList<>();
        for(Utente u : adapter.getNoFilteredData()){
            idComponenti.add(u.getId());
        }
        idComponenti.add(AuthHelper.getUserId());

        newGroup.setIdComponenti(idComponenti);

        Gruppi.addNewGroup(newGroup, groupId -> {
            if(groupId != null){
                Gruppi.getGroup(groupId, gruppo -> {
                    if(gruppo != null){
                        toggleDownloadMode(false);
                        Toast.makeText(this,R.string.groupCreated,Toast.LENGTH_LONG).show();
                        Intent i = new Intent(this, GroupDetailActivity.class);
                        i.putExtra("IdGrouppoLista",gruppo);
                        i.putExtra("Creato", true);
                        startActivity(i);
                        finish();
                    }else{
                        Toast.makeText(this,R.string.groupCreatedWithSomeDownloadError,Toast.LENGTH_LONG).show();
                        toggleDownloadMode(false);
                        super.onBackPressed();
                        finish();
                    }
                });
            }else{
                Toast.makeText(this,R.string.groupCreationError,Toast.LENGTH_LONG).show();
                toggleDownloadMode(false);
            }
        });
    }

    /**
     * Si disabilitano le componenti del layout dell'aggiunta del gruppo mentre vengono apportate le modifiche sulle edit text
     * o che li abilita dopo che si finisce di modificare le edit text del layout
     * @param isEnabled : valore booleano equivalente a true che indica le componenti abilitate
     */
    private void toggleDownloadMode(boolean isEnabled){
        if(isEnabled){
            buttonSaveDataGroup.setEnabled(false);
            buttonNewContactGroup.setEnabled(false);
            editTextDescription.setEnabled(false);
            editTextGroupName.setEnabled(false);
            selectPhotoTextView.setEnabled(false);

            AnimationHelper.fadeIn(progressBar,500);
        }else{
            buttonSaveDataGroup.setEnabled(true);
            buttonNewContactGroup.setEnabled(true);
            editTextDescription.setEnabled(true);
            editTextGroupName.setEnabled(true);
            selectPhotoTextView.setEnabled(true);

            AnimationHelper.fadeOut(progressBar,500);
        }
    }
}