package com.vitandreasorino.savent.Utenti.EventiTab;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
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
import com.vitandreasorino.savent.R;
import java.util.*;
import Helper.AnimationHelper;
import Model.Closures.ClosureBitmap;
import Model.Closures.ClosureList;
import Model.DB.Eventi;
import Model.DB.Partecipazioni;
import Model.Pojo.Evento;

/**
 * Gestisce la lista degli eventi dove si partecipa.
 * Permette la visualizzazione e ricerca di tali eventi dove si è deciso di partecipare.
 */
public class MyEventPartecipations extends AppCompatActivity implements AdapterView.OnItemClickListener{

    List<Evento> listaEventi = new ArrayList<Evento>();
    ListView eventListView;
    SearchView eventSearchView;
    EventAdapterPartecipations adapter;
    ProgressBar progressBarEvent;
    TextView emptyTextViewEvents;

    private SwipeRefreshLayout pullToRefresh;
    private SwipeRefreshLayout emptyViewPullToRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_partecipations);

        eventListView = findViewById(R.id.myPartecipationEventsListView);
        eventSearchView = findViewById(R.id.searchViewMyPartecipationEvents);
        progressBarEvent = findViewById(R.id.progressBarEvent);
        emptyTextViewEvents = findViewById(R.id.emptyTextViewEvents);

        emptyViewPullToRefresh = findViewById(R.id.emptyResultsPullToRefresh);
        pullToRefresh = findViewById(R.id.pullToRefresh);
        eventListView.setEmptyView(emptyViewPullToRefresh);

        //istanzia l'adapter personalizzato
        adapter = new EventAdapterPartecipations(this, listaEventi);

        emptyViewPullToRefresh.setOnRefreshListener( () -> downloadDataList());
        pullToRefresh.setOnRefreshListener( () -> downloadDataList());

        // collegamento dell'adapter alla ListView
        eventListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        downloadDataList();

        // istanza che permette alla lista dei gruppi di essere cercata con la SearchView
        eventSearchView.setOnQueryTextListener(searchListener);
        eventListView.setOnItemClickListener(this);

        //registrazione del lister per il broadcast
        LocalBroadcastManager.getInstance(this).registerReceiver(updatedListPartecipationsBoradcast, new IntentFilter("UpdateListPartecipations"));
    }

    /**
     * Ricevitore di messaggio broadcast per aggiornare la lista
     * degli eventi quando ne viene creato uno nuovo.
     */
    private BroadcastReceiver updatedListPartecipationsBoradcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean isUpdatedList = intent.getBooleanExtra("UpdatedListPartecipations", false);
            if(isUpdatedList){
                downloadDataList();
            }
        }
    };


    /**
     * metodo che entra in azione durante il caricamento degli elementi della lista
     * @param isDownloading
     */
    private void toggleDownloadingElements(boolean isDownloading){
        if(isDownloading){
            AnimationHelper.fadeIn(progressBarEvent,0);
            emptyTextViewEvents.setVisibility(View.INVISIBLE);
        }else{
            AnimationHelper.fadeOut(progressBarEvent,0);
            emptyTextViewEvents.setVisibility(View.VISIBLE);
        }
    }

    /*Metodo che permette di scaricare e visualizzare gli eventi dove si è partecipanti o in coda salvati sul database, con successivo
     * metodo che che controlla se ad ogni evento sono associate le rispettive immagini del profilo abbinate
     */
    private void downloadDataList() {
        toggleDownloadingElements(true);
        Log.i("prova", "1");
        Eventi.getMyParticipationEvents(new ClosureList<Evento>() {
            @Override
            public void closure(List<Evento> list) {
                if(list != null) {
                    for(Evento e : list) {
                        if(e.getIsImageUploaded()){
                            Eventi.downloadEventImage(e.getId(), new ClosureBitmap() {
                                @Override
                                public void closure(Bitmap bitmap) {
                                    if(bitmap != null) {
                                        e.setImageBitmap(bitmap);
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }//fine for
                    /* salvataggio della rispettiva lista letta dal database*/
                    listaEventi = list;

                    //istanzia l'adapter personalizzato
                    adapter.setList(listaEventi);

                    // collegamento dell'adapter alla ListView
                    eventListView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();

                    toggleDownloadingElements(false);
                    pullToRefresh.setRefreshing(false);
                    emptyViewPullToRefresh.setRefreshing(false);
                }
            }
        });



    }

    /**
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
     * Metodo che permette di cliccare sul singolo evento dalla lista delle partecipazioni.
     * Tale metodo ci permetterà di entrare nel dettaglio del singolo evento dove si partecipa,
     * anche quando è filtrata dalla SearchView.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        eventSearchView.clearFocus();

        Intent i = new Intent(getApplicationContext(), EventDetailActivity.class);
        List<Evento> listaEventiFiltrata = adapter.getFilteredData();

        if(listaEventiFiltrata == null){
            i.putExtra("eventObj", listaEventi.get(position));
        } else {
            i.putExtra("eventObj", listaEventiFiltrata.get(position));
        }
        startActivity(i);
    }

    /**
     * pulsante "back" che permette di tornare all'activity precedente.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onBackButtonPressed(View view) {
        onBackPressed();
    }

}// fine classe


/**
 * EventAdapterPartecipations creato per implementare la lista degli eventi
 */
class EventAdapterPartecipations extends BaseAdapter implements Filterable {

    private List<Evento> events = null;
    private List<Evento>filteredData = null;
    private Context context = null;
    ItemFilter mFilter = new ItemFilter();

    //Costruttore
    public EventAdapterPartecipations(Context context,List<Evento> events) {
        this.events=events;
        this.context=context;
        this.filteredData = events;
    }

    public void setList(List<Evento> events){
        this.events = events;
        this.filteredData = events;
        notifyDataSetChanged();
    }

    public List<Evento> getFilteredData(){
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
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View v, ViewGroup parent) {

        //espandi il layout per ogni riga della lista
        if (v == null) v = LayoutInflater.from(context).inflate(R.layout.events_partecipations_list_row, null);

        Evento evento = (Evento) getItem(position);
        ImageView img = v.findViewById(R.id.imageViewProfilePartecipazione);
        TextView txtName = v.findViewById(R.id.textViewNomePartecipazione);
        TextView txtDescription = v.findViewById(R.id.textViewDescrizionePartecipazione);
        TextView date = v.findViewById(R.id.textViewDatePartecipazione);
        TextView txtPartecipante = v.findViewById(R.id.textViewPartecipante);
        TextView txtInCoda = v.findViewById(R.id.textViewInCoda);

        //imposta il testo per il nome e la descrizione dell'evento
        txtName.setText(evento.getNome());
        txtDescription.setText(evento.getDescrizione());
        date.setText(evento.getNeutralData());

        //imposta l'immagine dell'evento se esiste
        if(evento.getImageBitmap() != null) img.setImageBitmap(evento.getImageBitmap());
        else img.setImageResource(R.drawable.event_placeholder_icon);

        participationManager(evento,txtPartecipante, txtInCoda);

        return v;
    }

    /**
     * Metodo che ti permette di stampare l'etichetta a seconda se sei in coda o sei già un partecipante dell'evento
     * @param evento
     * @param txtPartecipante
     * @param txtInCoda
     */
    private void participationManager(Evento evento, TextView txtPartecipante, TextView txtInCoda){

        //prova a scaricare l'istanza di partecipazione a questo evento
        Partecipazioni.getMyPartecipationAtEvent(evento.getId(), partecipazione -> {

            if(partecipazione != null){
                if(partecipazione.getAccettazione()){ //se l'utente ha già aderito all'evento
                    if(partecipazione.getListaAttesa()){ //e se risulta nella lista di attesa
                        //stampa sei In Coda
                        txtPartecipante.setVisibility(View.INVISIBLE);
                        txtInCoda.setVisibility(View.VISIBLE);
                    }else{
                        //stampa sei partecipante
                        txtInCoda.setVisibility(View.INVISIBLE);
                        txtPartecipante.setVisibility(View.VISIBLE);
                    }
                }else{
                    if(partecipazione.getListaAttesa()){ //se risulta nella lista di attesa
                        //stampa sei In Coda
                        txtPartecipante.setVisibility(View.INVISIBLE);
                        txtInCoda.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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
            final List<Evento> list = events;
            int count = list.size();
            final ArrayList<Evento> nlist = new ArrayList<Evento>(count);

            Evento filterable;

            //permette di leggere dalla lista degli eventi solo i nomi per poter ricercarli attraverso la SearchView
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
            filteredData = (ArrayList<Evento>) results.values;

            // aggiorna l'elenco con i dati filtrati
            notifyDataSetChanged();
        }

    }

}
