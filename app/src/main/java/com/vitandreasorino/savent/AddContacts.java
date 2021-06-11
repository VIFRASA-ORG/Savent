package com.vitandreasorino.savent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    SearchView searchView;
    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        recyclerView = findViewById(R.id.ListContacts);
        searchView = findViewById(R.id.searchContact);

        searchView.setOnQueryTextListener(this);

        //Verifica il permesso
        checkPermission();

    }

    private void checkPermission() {
        //Verifica le condizioni
        if (ContextCompat.checkSelfPermission(AddContacts.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Quando il permesso non è richiesto, richiedilo
            ActivityCompat.requestPermissions(AddContacts.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        } else {
            //una volta ottenuto il permesso, accedi ai dati in rubrica richiamando il metodo
            getContactList();
        }
    }

    /**
     * metodo che, dopo aver ottenuto il permesso dall'utente accede ai dati della rubrica e, mediante cursore, legge e setta i valori
     * da utilizzare (nome contatto e numero di telefono) all'interno di un arraylist
     */
    private void getContactList() {

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Ordina in modo crescente
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        //Inizializza il cursore
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        //Verifica condizioni
        if (cursor.getCount() > 0) {
            //quando il conteggio è maggiore di 0 usa il while loop
            while (cursor.moveToNext()) {
                //Il cursore si muove verso il successivo, e ottiene l'id del contatto e il nome
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));

                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";

                Cursor phoneCursor = getContentResolver().query(uriPhone, null, selection, new String[]{id}, null);


                if (phoneCursor.moveToNext()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                    ContactModel model = new ContactModel();
                    model.setName(name);
                    model.setNumber(number);
                    //Add model in array list
                    arrayList.add(model);
                    //Close phone cursor
                    phoneCursor.close();
                }
            }
            //Chiudi il cursore
            cursor.close();
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * metodo che serve per richiamare il metodo getContactList() che serve per ottenere il permesso di accesso ai dati all'utente.
     * Se l'utente rifiuta di dare il permesso, dovrà essere richiamato nuovamente il metodo per richiederlo
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Dopo aver ottenuto il permesso accedi ai dati
            getContactList();
        } else {
            //quando viene rifiutato il permesso viene richiesto, e si lancia un messaggio di avviso
            Toast.makeText(AddContacts.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            //richiama il metodo
            checkPermission();
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }
}

