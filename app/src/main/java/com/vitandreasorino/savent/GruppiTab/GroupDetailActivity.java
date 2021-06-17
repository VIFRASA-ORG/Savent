package com.vitandreasorino.savent.GruppiTab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;


import com.vitandreasorino.savent.LoginActivity;
import com.vitandreasorino.savent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureBoolean;
import Model.Closures.ClosureResult;
import Model.DB.GenericUser;
import Model.DB.Gruppi;
import Model.DB.Utenti;

import Model.Pojo.Gruppo;
import Model.Pojo.Utente;

public class GroupDetailActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, View.OnFocusChangeListener{

    Gruppo groupModel;

    ImageView imageViewDetailGroup;
    TextView editProfilePhotoGroup;
    Uri newSelectedImage;

    EditText nameDetailGroup;
    EditText descriptionDetailGroup;
    Button leaveGroup;
    View viewEditGroupPhoto;

    ListView componentListView;
    SearchView searchView;

    ImageView buttonSaveDataGroup;

    ProgressBar progressBar;

    ComponentGroupAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        //Deserializing the object from the intent
        groupModel = (Gruppo) getIntent().getSerializableExtra("IdGrouppoLista");

        //Inflate all the component
        inflateAll();

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


        componentListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // ricerca dell'id dell'utente che si vuole eliminare
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


        //Funzionalità in base al ruolo: se sono l'admin del gruppo posso modificare, altrimenti posso solo abbandonare
        if(groupModel.getIdAmministratore().equals(AuthHelper.getUserId())){

            leaveGroup.setEnabled(false);
            leaveGroup.setVisibility(View.INVISIBLE);

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
         * bottone che permette di salvare gli eventuali dati modificati
         */
        buttonSaveDataGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveDataButtonClick(v);
            }
        });


        //istanzia l'adapter personalizzato
        adapter = new ComponentGroupAdapter(this, groupModel.getIdAmministratore());
        componentListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        //Scarica lista componenti con rispettiva immagine del profilo associata
        if(!groupModel.getIdComponenti().isEmpty()){

            for(String id : groupModel.getIdComponenti()){

                Utenti.getUser(id, closureResult -> {
                    if(closureResult != null){
                        adapter.addItemToList(closureResult);
                        adapter.notifyDataSetChanged();

                        if(closureResult.getIsProfileImageUploaded()){
                            Utenti.downloadUserImage(closureResult.getId(), (ClosureResult<File>) file -> {
                                closureResult.setProfileImageUri(Uri.fromFile(file));
                                adapter.notifyDataSetChanged();
                            });
                        }
                    }
                });
            }
        }

        nameDetailGroup.addTextChangedListener(textWatcher);
        descriptionDetailGroup.addTextChangedListener(textWatcher);
        nameDetailGroup.setOnFocusChangeListener(this);
        descriptionDetailGroup.setOnFocusChangeListener(this);

        searchView.setOnQueryTextListener(this);

        checkSaveButtonActivation();



    }//fine onCreate


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

        if(listOfUpdates.size() == 0) {
            //Controlla se è necessario caricare l'immagine
            if(newSelectedImage != null){
                Gruppi.uploadGroupImage(newSelectedImage,groupModel.getId(),closureBool -> {
                    if(closureBool){
                        enableAllComponent();
                        buttonSaveDataGroup.setEnabled(false);
                        newSelectedImage = null;
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
     * Check all the new information entered in the nome, cognome and phoneNumer fields.
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
        viewEditGroupPhoto = findViewById(R.id.viewEditGroupPhoto);
    }

    /**
     * Evento tramite Click che permette di tornare indietro
     * @param view
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();

        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();

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

        if(!nameDetailGroup.getText().toString().equals(groupModel.getNome()) || !descriptionDetailGroup.getText().toString().equals(groupModel.getDescrizione()) ||
            newSelectedImage != null){
            buttonSaveDataGroup.setEnabled(true);
        } else {
            buttonSaveDataGroup.setEnabled(false);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == 200) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageViewDetailGroup.setImageURI(selectedImageUri);
                    newSelectedImage = selectedImageUri;
                    checkSaveButtonActivation();
                }
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

        Toast.makeText(GroupDetailActivity.this, "Creare evento", Toast.LENGTH_LONG).show();


    }
}//fine classe GroupDetailActivity



class ComponentGroupAdapter extends BaseAdapter implements Filterable {

    private List<Utente> users = null;
    private List<Utente> filteredData = null;
    private Context context = null;
    private String idAdmin;
    ItemFilter mFilter = new ItemFilter();

    //Costruttori
    public ComponentGroupAdapter(Context context, String idAdmin) {
        this.users = new ArrayList<>();
        this.context=context;
        this.filteredData = new ArrayList<>();
        this.idAdmin = idAdmin;
    }

    public void addItemToList(Utente user){
        users.add(user);
        filteredData.add(user);
    }

    public void removeItemFromList(Utente user){

        if(users.indexOf(user) >= 0) users.remove(user);
        if(filteredData.indexOf(user) >= 0) filteredData.remove(user);

    }

    public List<Utente> getFilteredData(){
        return filteredData;
    }

    public List<Utente> getNoFilteredData(){
        return users;
    }

    @Override
    public int getCount() {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup vg) {

        //espandi il layout per ogni riga della lista
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.component_group_list_row, null);
        }

        Utente utente = (Utente) getItem(position);
        ImageView img = v.findViewById(R.id.imageViewComponent);
        TextView txtName = v.findViewById(R.id.textViewNameComponet);
        TextView txtSurname = v.findViewById(R.id.textViewSurnameComponet);
        TextView textAdmin = v.findViewById(R.id.textAdminGroup);

        //imposta il testo per il nome e la descrizione del gruppo
        txtName.setText(utente.getNome());
        txtSurname.setText(utente.getCognome());
        if(utente.getProfileImageBitmap() != null) img.setImageBitmap(utente.getProfileImageBitmap());
        else if(utente.getProfileImageUri() != null) img.setImageURI(utente.getProfileImageUri());
        else img.setImageResource(R.drawable.profile_icon);

        //imposta etichetta "Amm.re" nel caso un componente del gruppo è il creatore di tale del gruppo
        if(utente.getId().equals(idAdmin)){
            textAdmin.setVisibility(View.VISIBLE);
        } else {
            textAdmin.setVisibility(View.INVISIBLE);
        }

        //ritorna la vista per la riga corrente
        return v;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    private class ItemFilter extends Filter{

        /**
         * Metodo utilizzato per eseguire l'operazione di filtraggio.
         * @param constraint
         * @return
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            String filterString = constraint.toString().toLowerCase();
            final List<Utente> list = users;
            int count = list.size();
            final ArrayList<Utente> nlist = new ArrayList<Utente>(count);

            Utente filterable;

            for (int i = 0; i < count; i++) {
                filterable = list.get(i);
                //permette di leggere dalla lista nomi e cognome per poter ricercarli attraverso la SearchView
                if (filterable.getNome().toLowerCase().contains(filterString) || filterable.getCognome().toLowerCase().contains(filterString)) {
                    nlist.add(filterable);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Utente>) results.values;

            // aggiorna l'elenco con i dati filtrati
            notifyDataSetChanged();
        }

    }

}//fine classe2