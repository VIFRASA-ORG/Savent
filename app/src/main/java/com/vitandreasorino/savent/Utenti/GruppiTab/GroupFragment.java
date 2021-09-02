package com.vitandreasorino.savent.Utenti.GruppiTab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureList;
import Model.DAO.Gruppi;
import Model.POJO.Gruppo;

import static android.app.Activity.RESULT_OK;

/**
 * Activity inerente alla lista di tutti i gruppi a cui partecipi o sei admin.
 * Permette la visualizzazione, ricerca e la creazione del gruppo.
 */
public class GroupFragment extends Fragment implements AdapterView.OnItemClickListener {
    List<Gruppo> listaGruppi = new ArrayList<>();
    ListView groupListView;
    SearchView groupSearchView;
    GroupAdapter adapter;
    FloatingActionButton buttonCreateGroup;
    ProgressBar progressBar;
    TextView emptyTextView;

    private SwipeRefreshLayout pullToRefresh;
    private SwipeRefreshLayout emptyResultspullToRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        groupSearchView = view.findViewById(R.id.searchViewGroup);
        groupListView = view.findViewById(R.id.groupListView);
        buttonCreateGroup = view.findViewById(R.id.buttonCreateGroup);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        pullToRefresh = view.findViewById(R.id.pullToRefresh);
        emptyResultspullToRefresh = view.findViewById(R.id.emptyResultsPullToRefresh);
        pullToRefresh.setOnRefreshListener( () -> downloadDataList());
        emptyResultspullToRefresh.setOnRefreshListener( () -> downloadDataList());

        groupListView.setEmptyView(emptyResultspullToRefresh);

        downloadDataList();


        //pulsante che permette di creare un nuovo gruppo
        buttonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent AddGroup = new Intent(getActivity(), com.vitandreasorino.savent.Utenti.GruppiTab.CreazioneGruppo.AddGroup.class);
                startActivity(AddGroup);
            }
        });


         // istanza che permette alla lista dei gruppi di essere cercata con la SearchView
        groupSearchView.setOnQueryTextListener(searchListener);
        groupListView.setOnItemClickListener(this);

        //registrazione del lister per il broadcast
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br, new IntentFilter("UpdateGroup"));
    }

    /**
     * Ricevitore di messaggio broadcast per aggiornare la lista
     * dei gruppi quando viene creato un nuovo gruppo
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isUpdated = intent.getBooleanExtra("Updated", false);
            if(isUpdated){
                downloadDataList();
            }
        }
    };

    /**
     * metodo che permette di rendere visibili la lista se è piena con relativo caricamento della progress bar, altrimenti lasciala invisibile.
     * @param isDownloading
     */
    private void toggleDownloadingElements(boolean isDownloading){
        if(isDownloading){
            AnimationHelper.fadeIn(progressBar,0);
            emptyTextView.setVisibility(View.INVISIBLE);
        }else{
            AnimationHelper.fadeOut(progressBar,0);
            emptyTextView.setVisibility(View.VISIBLE);
        }
    }

    /*Metodo che permette di scaricare e visualizzare i gruppi salvati sul database, con successivo
     * metodo che che controlla se ad ogni gruppo sono associate le rispettive immagini di profilo
     * abbinate
     */
    private void downloadDataList(){
        toggleDownloadingElements(true);

        //lettura di tutti i gruppi dal db
        Gruppi.getAllMyGroups(new ClosureList<Gruppo>() {
            @Override
            public void closure(List<Gruppo> list) {
                if(list != null){
                    for(Gruppo g : list){
                        if(g.getIsImmagineUploaded()){
                            Gruppi.downloadGroupImage(g.getId(), bitmap -> {
                                if(bitmap != null){
                                    g.setImmagineBitmap(bitmap);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                    //salvataggio della rispettiva lista letta dal database
                    listaGruppi = list;

                    //istanzia l'adapter personalizzato
                    adapter = new GroupAdapter(getContext(), listaGruppi);

                    // collegamento dell'adapter alla ListView dei gruppi
                    groupListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    toggleDownloadingElements(false);

                    emptyResultspullToRefresh.setRefreshing(false);
                    pullToRefresh.setRefreshing(false);
                    groupSearchView.setQuery("",true);
                    groupSearchView.clearFocus();
                }
            }
        });
        //istanzia l'adapter personalizzato
        adapter = new GroupAdapter(getContext(), listaGruppi);

        // collegamento dell'adapter alla ListView
        groupListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    /**
     * OVERRIDE DEI METODI NELL'INTERFACCIA SearchView.OnQueryTextListener
     * Forza l'adapter a filtrare l'elemento della ListView in base alla query inserita nella barra di ricerca.
     */
    SearchView.OnQueryTextListener searchListener = new SearchView.OnQueryTextListener() {
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
    };

    /**
     * Metodo che permette di cliccare un singolo gruppo dalla lista dei gruppi.
     * Tale funzione ci permetterà di entrare nel dettaglio del singolo gruppo, anche attraverso la lista filtrata data dalla SearchView
     * che ci permette di visualizzare la lista dei gruppi filtrata
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        groupSearchView.clearFocus();

        Intent i = new Intent(getContext(), GroupDetailActivity.class);
        List<Gruppo> listaGruppiFiltrata = adapter.getFilteredData();

        if(listaGruppiFiltrata == null){
            i.putExtra("IdGrouppoLista", listaGruppi.get(position));
        } else {
            i.putExtra("IdGrouppoLista", listaGruppiFiltrata.get(position));
        }

        startActivityForResult(i, 125);

    }

    /**
     * metodo per aggiornare tutte le info dei gruppi mediante il GroupDetail
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == 125){
                downloadDataList();
            }
        }
    }
}

/**
 * GroupAdapter creato per implementare la listaGruppi
 */
class GroupAdapter extends BaseAdapter implements Filterable {

    private List<Gruppo> groups=null;
    private List<Gruppo>filteredData = null;
    private Context context=null;
    ItemFilter mFilter = new ItemFilter();

    //Costruttore
    public GroupAdapter(Context context,List<Gruppo> groups) {
        this.groups=groups;
        this.context=context;
        this.filteredData = groups;
    }

    public List<Gruppo> getFilteredData(){
        return filteredData;
    }

    /**
     * Il metodo getCount() restituisce il numero totale di elementi da visualizzare in un elenco.
     * Esso conta il valore attraverso il metodo size() dell'elenco di una lista o la lunghezza di un lista.
     * @return
     */
    @Override
    public int getCount() {
        return filteredData.size();
    }


    /**
     * Questo metodo viene utilizzato per ottenere l'elemento di dati associato alla posizione specificata nel set di dati
     * per ottenere i dati corrispondenti della posizione specifica nella raccolta di elementi di dati.
     * Restituisce l'elemento della lista nella posizione specificata.
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        return filteredData.get(position);
    }

    /**
     * Questo metodo, restituisce il corrispondente all'ID dell'elemento di posizione.
     * La funzione restituisce un valore lungo della posizione dell'elemento all'adattatore.
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Questo metodo viene chiamato automaticamente quando la vista delle voci di elenco è pronta per essere visualizzato o sta per essere visualizzato.
     * In questa funzione impostiamo il layout per gli elementi dell'elenco utilizzando la classe LayoutInflater e quindi aggiungiamo i dati
     * alle viste come ImageView , TextView ecc.
     * @param position
     * @param v
     * @param vg
     * @return
     */
    @Override
    public View getView(int position, View v, ViewGroup vg) {
        //espandi il layout per ogni riga della lista
        if (v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.group_list_row, null);
        }

        Gruppo gruppo = (Gruppo) getItem(position);
        TextView txtName = v.findViewById(R.id.textViewNameGroup);
        ImageView img = v.findViewById(R.id.imageViewGroup);
        TextView txtDescription = v.findViewById(R.id.textViewDescription);
        TextView textAdmin = v.findViewById(R.id.textAdmin);

        //imposta il testo per il nome e la descrizione del gruppo
        txtName.setText(gruppo.getNome());
        txtDescription.setText(gruppo.getDescrizione());

        //imposta l'immagine del gruppo se esiste
        if(gruppo.getImmagineBitmap() != null) img.setImageBitmap(gruppo.getImmagineBitmap());
        else img.setImageResource(R.drawable.group_icon);

        //imposta etichetta Admin se si è amministratori del gruppo
        if(gruppo.getIdAmministratore().equals(AuthHelper.getUserId())) textAdmin.setVisibility(View.VISIBLE);
        else textAdmin.setVisibility(View.INVISIBLE);

        //ritorna la vista per la riga corrente
        return v;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    /**
     * Gestisce le operazioni per il filtraggio della SearchView
     */
    private class ItemFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();
            String filterString = constraint.toString().toLowerCase();
            final List<Gruppo> list = groups;
            int count = list.size();
            final ArrayList<Gruppo> nlist = new ArrayList<Gruppo>(count);

            Gruppo filterable;

            //permette di leggere dalla lista dei gruppi solo i nomi per poter ricercarli attraverso la SearchView
            for (int i = 0; i < count; i++) {
                filterable = list.get(i);
                if (filterable.getNome().toLowerCase().contains(filterString)) {
                    nlist.add(filterable);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }


        /**
         * stampa i risultati del filtro
         * @param constraint
         * @param results
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Gruppo>) results.values;

            // aggiorna l'elenco con i dati filtrati
            notifyDataSetChanged();
        }

    }
}
