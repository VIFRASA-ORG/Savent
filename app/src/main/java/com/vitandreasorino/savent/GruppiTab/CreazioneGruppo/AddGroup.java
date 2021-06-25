package com.vitandreasorino.savent.GruppiTab.CreazioneGruppo;

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
import com.vitandreasorino.savent.GruppiTab.ComponentGroupAdapter;
import com.vitandreasorino.savent.GruppiTab.GroupDetailActivity;
import com.vitandreasorino.savent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Helper.ImageHelper;
import Model.Closures.ClosureResult;
import Model.Pojo.ContactModel;
import Model.DB.Gruppi;
import Model.DB.Utenti;
import Model.Pojo.Gruppo;
import Model.Pojo.Utente;

public class AddGroup extends AppCompatActivity {

    FloatingActionButton buttonNewContactGroup;
    ImageView imageNewGroup;
    ListView listView;
    ArrayList<ContactModel> contactsGroupList = new ArrayList<>();
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
     * metodo che richiama la successiva classe abbinata allo scopo di aggiungere nuovi contatti al gruppo
     * cliccando il floating button "aggiungi contatto"
     * @param view
     */
    public void onNewContactClick (View view) {
        Intent addNewContact = new Intent(this, AddContacts.class);
        ArrayList<Utente> l = new ArrayList<>();
        l.addAll(adapter.getNoFilteredData());
        addNewContact.putParcelableArrayListExtra(AddContacts.EXTRA_ARRAY_CHECKED_CONTACTS,l);
        startActivityForResult(addNewContact,ADD_NEW_CONTACT_RESULT);
    }

    /**
     * metodo che riporta nella schermata precedente
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

    public void onClickPhoto(View view){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), PHOTO_LIBRARY_RESULT);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PHOTO_LIBRARY_RESULT) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (resultCode == RESULT_OK) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // Compress it before it is shown into the imageView
                    imageNewGroup.setImageBitmap(ImageHelper.decodeSampledBitmapFromUri(getContentResolver(),selectedImageUri,imageNewGroup));
                    selectedGroupProfileImage = selectedImageUri;
                }
            }
        }else if (requestCode == ADD_NEW_CONTACT_RESULT){
            //Return from the addContact intent
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
     * Used to check if the user is writing some new information that are different from the one on the server
     */
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            checkSaveButtonEnable();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    /**
     * Enable or disable the save button based on the editText status
     */
    private void checkSaveButtonEnable(){
        if(!editTextDescription.getText().toString().isEmpty() && !editTextGroupName.getText().toString().isEmpty() && adapter.getNoFilteredData().size() != 0){
            buttonSaveDataGroup.setEnabled(true);
        }else buttonSaveDataGroup.setEnabled(false);
    }

    /**
     * Triggered when the save button is clicked
     *
     * @param view
     */
    public void onSaveButtonClick(View view){
        toggleDownloadMode(true);

        //Save all the information on the server
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