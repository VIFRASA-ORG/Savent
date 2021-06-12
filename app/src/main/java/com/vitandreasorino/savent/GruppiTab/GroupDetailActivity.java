package com.vitandreasorino.savent.GruppiTab;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;


import com.vitandreasorino.savent.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureResult;
import Model.DB.Gruppi;
import Model.DB.Utenti;

import Model.Pojo.Gruppo;
import Model.Pojo.Utente;

public class GroupDetailActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    Gruppo groupModel;

    TextView nameDetailGroup;
    TextView descriptionDetailGroup;
    ImageView imageViewDetailGroup;

    ListView componentListView;
    SearchView searchView;

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

        //Scarica l'immagine del gruppo
        if(groupModel.getIsImmagineUploaded()){
            Gruppi.downloadGroupImage(groupModel.getId(), bitmap -> {
                if(bitmap != null){
                    imageViewDetailGroup.setImageBitmap(bitmap);
                    groupModel.setImmagineBitmap(bitmap);
                }
            });
        }


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

        searchView.setOnQueryTextListener(this);

    }//fine onCreate

    /**
     * Method used to get all the interface reference from the xml file
     */
    private void inflateAll() {
        nameDetailGroup = findViewById(R.id.nameDetailGroup);
        descriptionDetailGroup = findViewById(R.id.descriptionContentTextViewGroup);
        imageViewDetailGroup = findViewById(R.id.imageViewDetailGroup);
        componentListView = findViewById(R.id.componenGrouptListView);
        searchView = findViewById(R.id.searchViewComponentGroup);
    }

    /**
     * Evento tramite Click che permette di tornare indietro
     * @param view
     */
    public void onBackButtonPressed(View view){
        super.onBackPressed();
        finish();
    }

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

    public List<Utente> getFilteredData(){
        return filteredData;
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