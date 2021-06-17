package com.vitandreasorino.savent.GruppiTab.CreazioneGruppo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;

import Model.ContactModel;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener {

    RecyclerView recyclerView;
    SearchView searchView;
    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    ContactAdapter adapter;
    LinearLayout emptyListLayout;
    LinearLayout recycleListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        recyclerView = findViewById(R.id.ListContacts);
        searchView = findViewById(R.id.searchContact);
        emptyListLayout =findViewById(R.id.emptyListLayout);
        recycleListLayout =findViewById(R.id.recycleListLayout);

        searchView.setOnQueryTextListener(this);

        //Verifica il permesso
        checkPermission();

    }

    private void checkPermission() {
        //Verifica le condizioni
        if (ContextCompat.checkSelfPermission(AddContacts.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Quando il permesso non è richiesto, richiedilo

            Toast.makeText(AddContacts.this, "Permission denied!", Toast.LENGTH_SHORT).show();
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
                    model.setId(id);
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

        toggleEmptyLayout(arrayList.size() == 0);
    }

    /**
     * metodo che serve per decidere quale tipologia di lista vedere: nel caso in cui la rubrica
     * abbia almeno un contatto si rende visibile la lista piena, altrimenti si visionerà la lista vuota
     * contenente un messaggio che notifica l'assenza di contatti in rubrica
     * @param isEmpty
     */
    private void toggleEmptyLayout(boolean isEmpty){
        if(isEmpty){
            recycleListLayout.setVisibility(View.GONE);
            emptyListLayout.setVisibility(View.VISIBLE);
        }else{
            recycleListLayout.setVisibility(View.VISIBLE);
            emptyListLayout.setVisibility(View.GONE);
        }
    }

    /**
     * metodo che serve per richiamare il metodo getContactList() che serve per ottenere il permesso di accesso ai dati all'utente.
     * Se l'utente rifiuta di dare il permesso, dovrà essere richiamato nuovamente il metodo per richiederlo
     *
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
            //quando viene rifiutato il permesso si lancia un messaggio di avviso
            Toast.makeText(AddContacts.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            //poi si rende la lista vuota visibile, poichè non è stato concesso il permesso
            toggleEmptyLayout(true);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);
        toggleEmptyLayout(adapter.contactListFiltered.size()==0);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        toggleEmptyLayout(adapter.contactListFiltered.size()==0);
        return false;
    }

    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }
    public void onClickAddContacts(View view){
        Intent createGroup = new Intent(this, AddGroup.class);
        startActivity(createGroup);

    }
}