package com.vitandreasorino.savent;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddGroup extends AppCompatActivity {
    FloatingActionButton buttonNewContactGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
    }



    /**
     * metodo che richiama la successiva classe abbinata allo scopo di aggiungere un nuovo membro al gruppo
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

}