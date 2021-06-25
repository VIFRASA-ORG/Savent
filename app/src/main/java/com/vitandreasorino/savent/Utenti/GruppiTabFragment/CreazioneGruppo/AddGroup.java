package com.vitandreasorino.savent.Utenti.GruppiTabFragment.CreazioneGruppo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;

import Model.ContactModel;

public class AddGroup extends AppCompatActivity {

    FloatingActionButton buttonNewContactGroup;
    ImageView imageNewGroup;
    RecyclerView recyclerView;
    ContactAdapter adapter;
    ArrayList<ContactModel> contactsGroupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        recyclerView = findViewById(R.id.contactGroupList);

    }
    /**
     * metodo che richiama la successiva classe abbinata allo scopo di aggiungere nuovi contatti al gruppo
     * cliccando il floating button "aggiungi contatto"
     * @param view
     */
    public void onNewContactClick (View view) {

        Intent addNewContact = new Intent(this, AddContacts.class);
        startActivity(addNewContact);
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
        startActivityForResult(Intent.createChooser(i, "Select Picture"), 200);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    // Compress it before it is shown into the imageView
                    imageNewGroup.setImageURI(selectedImageUri);
                }
            }
        }
    }
}