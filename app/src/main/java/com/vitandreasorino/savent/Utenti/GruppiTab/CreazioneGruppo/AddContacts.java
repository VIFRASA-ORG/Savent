package com.vitandreasorino.savent.Utenti.GruppiTab.CreazioneGruppo;

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
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Model.POJO.Contact;
import Model.DAO.Utenti;
import Model.POJO.Utente;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String EXTRA_ARRAY_CHECKED_CONTACTS = "checkedContacts";


    RecyclerView recyclerView;
    SearchView searchView;

    ArrayList<Contact> arrayList = new ArrayList<Contact>();
    ArrayList<Utente> utentiList = new ArrayList<>();
    List<Utente> alreadySelectedUsers;

    ContactAdapter adapter;
    LinearLayout emptyListLayout;
    LinearLayout recycleListLayout;

    //Empty layout textView inside
    TextView emptyLayoutNoContacts;
    TextView emptyListNoPermissionGranted;
    ProgressBar progressBar;

    FloatingActionButton buttonSaveContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        recyclerView = findViewById(R.id.ListContacts);
        searchView = findViewById(R.id.searchContact);
        emptyListLayout =findViewById(R.id.emptyListLayout);
        recycleListLayout =findViewById(R.id.recycleListLayout);

        emptyLayoutNoContacts = findViewById(R.id.noContacts);
        emptyListNoPermissionGranted = findViewById(R.id.noPermission);
        progressBar = findViewById(R.id.progressBar);

        buttonSaveContacts = findViewById(R.id.buttonSaveContacts);

        searchView.setOnQueryTextListener(this);

        setEmptyLayoutType(EmptyLayoutType.NORMAL);

        //Setting the adapter
        alreadySelectedUsers = getIntent().getParcelableArrayListExtra(EXTRA_ARRAY_CHECKED_CONTACTS);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);


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

        setEmptyLayoutType(EmptyLayoutType.DOWNLOADING);

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Ordina in modo crescente
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        //Inizializza il cursore
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        ArrayList<Contact> tempArrayList = new ArrayList<Contact>();

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
                    number = PhoneNumberUtils.normalizeNumber(number);

                    Contact model = new Contact();
                    model.setName(name);
                    model.setNumber(number);
                    model.setId(id);
                    //Add model in array list
                    tempArrayList.add(model);
                    //Close phone cursor
                    phoneCursor.close();
                }
            }
            //Chiudi il cursore
            cursor.close();
        }

        //Checking if the contact list is empty
        if(tempArrayList.size() == 0){
            setEmptyLayoutType(EmptyLayoutType.NORMAL);
            return;
        }

        //Download all the users from firebase with those phone numbers
        Utenti.searchContactsInPhoneBooks(tempArrayList, listOfUsers -> {
            for(Utente u : listOfUsers){
                //Adding all contact with an active account on the servers to the listView adapter.
                int index = tempArrayList.lastIndexOf(new Contact(u.getNumeroDiTelefono()));
                if(index >= 0){
                    Contact c = tempArrayList.get(index);
                    c.setDocumentId(u.getId());
                    arrayList.add(c);
                    utentiList.add(u);
                }
            }

            if(arrayList.size() == 0) setEmptyLayoutType(EmptyLayoutType.NORMAL);
            else setEmptyLayoutType(EmptyLayoutType.INVISIBLE);

            checkAlreadySelected();
            adapter.notifyDataSetChanged();
        });

    }

    /**
     * Method used to check the checkbox of the user already checked before.
     */
    private void checkAlreadySelected(){
        if(alreadySelectedUsers == null) return;
        if(alreadySelectedUsers.size() == 0) return;

        for(Utente selectedUser : alreadySelectedUsers){
            int index = utentiList.lastIndexOf(selectedUser);
            if(index >= 0){
                arrayList.get(index).setChecked(true);
            }
        }
    }

    /**
     * Showing the list view or the emptyView based the number of result from the cantact list and from the server.
     *
     * @param newType
     */
    private void setEmptyLayoutType(EmptyLayoutType newType){
        switch (newType){
            case NORMAL:
                //This is tha case in which show only the message of empty list
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.VISIBLE);
                emptyListNoPermissionGranted.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.GONE);
                emptyListNoPermissionGranted.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case CONTACT_ACCESS_NOT_GRANTED:
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.GONE);
                emptyListNoPermissionGranted.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            case INVISIBLE:
                emptyListLayout.setVisibility(View.GONE);
                recycleListLayout.setVisibility(View.VISIBLE);
                break;
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
            setEmptyLayoutType(EmptyLayoutType.CONTACT_ACCESS_NOT_GRANTED);
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);

        if(arrayList.size() == 0) setEmptyLayoutType(EmptyLayoutType.NORMAL);
        else setEmptyLayoutType(EmptyLayoutType.INVISIBLE);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

        if(arrayList.size() == 0) setEmptyLayoutType(EmptyLayoutType.NORMAL);
        else setEmptyLayoutType(EmptyLayoutType.INVISIBLE);

        return false;
    }

    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }

    public void onClickAddContacts(View view){
        //adapter.
        Intent i = new Intent();
        i.putParcelableArrayListExtra(EXTRA_ARRAY_CHECKED_CONTACTS,getCheckedUsers());
        setResult(RESULT_OK,i);
        finish();
    }

    /**
     * Find all the users selected.
     *
     * @return a list of Utente object indicating the selected objects.
     */
    private ArrayList<Utente> getCheckedUsers(){
        List<Contact> checkedContacts = adapter.getCheckedContacts();
        ArrayList<Utente> finalList = new ArrayList<>();

        for(Contact c : checkedContacts){
            int index = utentiList.lastIndexOf(new Utente(c.getDocumentId(),c.getNumber()));
            if(index >= 0){
                Utente u = utentiList.get(index);
                finalList.add(u);
            }
        }

        return finalList;
    }


    /**
     * Various state of the user interface for this Activity
     */
    private enum EmptyLayoutType{
        NORMAL,
        CONTACT_ACCESS_NOT_GRANTED,
        DOWNLOADING,
        INVISIBLE;
    }
}