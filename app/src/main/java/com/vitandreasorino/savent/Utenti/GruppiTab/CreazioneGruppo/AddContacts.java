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

import Model.Pojo.ContactModel;
import Model.DB.Utenti;
import Model.Pojo.Utente;


public class AddContacts extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final String EXTRA_ARRAY_CHECKED_CONTACTS = "checkedContacts";


    RecyclerView recyclerView;
    SearchView searchView;

    ArrayList<ContactModel> arrayList = new ArrayList<ContactModel>();
    ArrayList<Utente> utentiList = new ArrayList<>();
    List<Utente> alreadySelectedUsers;

    ContactAdapter adapter;
    LinearLayout emptyListLayout;
    LinearLayout recycleListLayout;

    //Dichiarazione di text view per la definizione delle due tipologie di layout da vedere
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

        //Impostazione dell'adapter
        alreadySelectedUsers = getIntent().getParcelableArrayListExtra(EXTRA_ARRAY_CHECKED_CONTACTS);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter(this, arrayList);
        recyclerView.setAdapter(adapter);


        //Verifica il permesso
        checkPermission();

    }

    /**
     * Si richiede il permesso di accedere ai contatti presenti nella rubrica quando non è stato richiesto.
     * Se si ottiene il permesso dall'utente si richiama il metodo per accedere ai dati in rubrica, altrimenti non si accede senza permesso.
     */
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
     * Dopo aver ottenuto il permesso dall'utente si accede ai dati della rubrica e, mediante cursore, legge e setta i valori
     * da utilizzare (nome contatto e numero di telefono) all'interno di un arraylist
     */
    private void getContactList() {

        setEmptyLayoutType(EmptyLayoutType.DOWNLOADING);

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Ordina in modo crescente
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        //Inizializza il cursore
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);

        ArrayList<ContactModel> tempArrayList = new ArrayList<ContactModel>();

        //Verifica condizioni
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {
                //Il cursore si muove verso il successivo, e ottiene l'id del contatto, il nome e il numero di telefono
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Uri uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " =?";
                Cursor phoneCursor = getContentResolver().query(uriPhone, null, selection, new String[]{id}, null);

                if (phoneCursor.moveToNext()) {
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    number = PhoneNumberUtils.normalizeNumber(number);

                    ContactModel model = new ContactModel();
                    //Si setta la variabile creata con i dati ottenuti in lettura dalla rubrica
                    model.setName(name);
                    model.setNumber(number);
                    model.setId(id);
                    //Si aggiunge la variabile model che contiene il nuovo contatto creato nell'arraylist
                    tempArrayList.add(model);
                    //Viene chiuso il cursone per la lettura dei numeri
                    phoneCursor.close();
                }
            }
            //Si chiude il cursore per la lettura dell'id del contatto e del nome
            cursor.close();
        }

        //Si verifica se la lista dei contatti è vuota. In questo caso si setta il layout della lista vuota
        if(tempArrayList.size() == 0){
            setEmptyLayoutType(EmptyLayoutType.NORMAL);
            return;
        }
        //Scarica tutti gli utenti presenti nel database di firestore con i relativi numeri di telefono
        Utenti.searchContactsInPhoneBooks(tempArrayList, listOfUsers -> {
            for(Utente u : listOfUsers){
                //Vengono agiunti tutti i contatti con un profilo attivo sul server all'interno dell'adapter della list view.
                int index = tempArrayList.lastIndexOf(new ContactModel(u.getNumeroDiTelefono()));
                if(index >= 0){
                    ContactModel c = tempArrayList.get(index);
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
     * Si verificano le checkbox dell'utente già settate in precedenza, in modo tale da salvarne le modifiche
     **/
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
     * Viene mostrata la list view con contatti o la list view vuota in base al numero di risultati pervenuti dall'elenco e dal server.
     * @param newType : il tipo del nuovo layout
     */
    private void setEmptyLayoutType(EmptyLayoutType newType){
        switch (newType){
            case NORMAL:
                //caso in cui si mostra solo il messaggio della lista vuota
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.VISIBLE);
                emptyListNoPermissionGranted.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                break;
            case DOWNLOADING:
                //caso in cui si stanno per scaricare i dati presenti nella rubrica
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.GONE);
                emptyListNoPermissionGranted.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case CONTACT_ACCESS_NOT_GRANTED:
                //caso in cui l'utente nega il permesso all'accesso dei dati in rubrica
                recycleListLayout.setVisibility(View.GONE);
                emptyListLayout.setVisibility(View.VISIBLE);
                emptyLayoutNoContacts.setVisibility(View.GONE);
                emptyListNoPermissionGranted.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                break;
            case INVISIBLE:
                //caso in cui si abilita la visione del layout con la lista dei contatti
                emptyListLayout.setVisibility(View.GONE);
                recycleListLayout.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Metodo che serve per richiamare il metodo getContactList() che serve per ottenere il permesso di accesso ai dati all'utente.
     * Se l'utente rifiuta di dare il permesso, dovrà essere richiamato nuovamente il metodo per richiederlo.
     * @param requestCode :codice di richiesta passato ActivityCompat.requestPermissions(android.app.Activity, String[], int)
     * @param permissions :le autorizzazioni richieste di tipo String.
     * @param grantResults :i risultati della concessione per le autorizzazioni corrispondenti.
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

    /**
     *Metodo che viene chiamato quando l'utente invia la query.
     * @param query :il testo della query da inviare
     * @return true se la query è stata gestita dal listener, false per consentire alla SearchView di eseguire l'azione predefinita.
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.getFilter().filter(query);

        if(arrayList.size() == 0) setEmptyLayoutType(EmptyLayoutType.NORMAL);
        else setEmptyLayoutType(EmptyLayoutType.INVISIBLE);
        return false;
    }

    /**
     *Metodo che viene chiamato quando il testo della query viene modificato dall'utente.
     * @param newText : stringa contenente il nuovo testo
     * @return false se la ricerca esegue l'azione predefinita, altrimenti true se l'azione è stata gestita dal listener.
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);

        if(arrayList.size() == 0) setEmptyLayoutType(EmptyLayoutType.NORMAL);
        else setEmptyLayoutType(EmptyLayoutType.INVISIBLE);

        return false;
    }

    /**
     * Metodo che riporta nella schermata precedente
     */
    public void onBackButtonPressed(View view) {
        super.onBackPressed();
        finish();
    }

    /**
     * Metodo che serve per aggiungere i contatti all'interno del gruppo che si vuole creare
     * @param view: la nuova vista contenente il gruppo da creare con la lista dei contatti selezionati
     */
    public void onClickAddContacts(View view){
        Intent i = new Intent();
        i.putParcelableArrayListExtra(EXTRA_ARRAY_CHECKED_CONTACTS,getCheckedUsers());
        setResult(RESULT_OK,i);
        finish();
    }

    /**
     *Metodo che serve per trovare tutti gli utenti selezionati nella rubrica
     * @return una lista di oggetti di tipo Utente indicante gli oggetti selezionati dall'utente .
     */
    private ArrayList<Utente> getCheckedUsers(){
        List<ContactModel> checkedContacts = adapter.getCheckedContacts();
        ArrayList<Utente> finalList = new ArrayList<>();

        for(ContactModel c : checkedContacts){
            int index = utentiList.lastIndexOf(new Utente(c.getDocumentId(),c.getNumber()));
            if(index >= 0){
                Utente u = utentiList.get(index);
                finalList.add(u);
            }
        }

        return finalList;
    }

    /**
     * Si definisce una nuova costante di tipo enum per la definizione dei vari stati che possono verificarsi
     * all'interno dell'interfaccia utente di questa activity
     */
    private enum EmptyLayoutType{
        NORMAL,
        CONTACT_ACCESS_NOT_GRANTED,
        DOWNLOADING,
        INVISIBLE;
    }
}