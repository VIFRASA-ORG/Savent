package com.vitandreasorino.savent.Utenti.EventiTab;

import android.app.Activity;
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

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Helper.AnimationHelper;
import Helper.AuthHelper;
import Model.Closures.ClosureBitmap;
import Model.DB.Eventi;
import Model.DB.Gruppi;
import Model.Pojo.Evento;

public class FragmentSearchEvent extends Fragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {


    private static final int EDIT_EVENT_ACTIVITY_RESULT = 1;


    ListView eventListView;
    SearchView searchView;
    EventAdapter adapter;

    ProgressBar progressBar;
    TextView emptyTextView;

    private SwipeRefreshLayout pullToRefresh;
    private SwipeRefreshLayout emptyViewPullToRefresh;

    //Elenco degli eventi mostrato in ListView.
    List<Evento> listaDiEventi = new ArrayList<>();

    //Utilizzato per memorizzare i nomi del gruppo di creatori nel caso in cui l'evento venga creato da un gruppo
    //La chiave è il groupId, il valore è il nome del gruppo
    Map<String,String> groupsName = new HashMap<>();
    Map<String,Boolean> isAdminMap = new HashMap<>();

    //Visualizzazione predefinita
    SearchEventType pageVisualizationType = SearchEventType.ALL_EVENTS;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_event, container, false);
    }

    /**
     * Costruttore per specificare il tipo di visualizzazione
     *
     * @param mode il tipo di visualizzazione.
     */
    public FragmentSearchEvent(SearchEventType mode) {
        pageVisualizationType = mode;
    }

    public FragmentSearchEvent( ) {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventListView = view.findViewById(R.id.eventListView);
        searchView = view.findViewById(R.id.searchViewEvent);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        emptyViewPullToRefresh = view.findViewById(R.id.emptyResultsPullToRefresh);
        pullToRefresh = view.findViewById(R.id.pullToRefresh);

        emptyViewPullToRefresh.setOnRefreshListener( () -> downloadAll());
        pullToRefresh.setOnRefreshListener( () -> downloadAll());

        eventListView.setEmptyView(emptyViewPullToRefresh);

        downloadAll();

        adapter = new EventAdapter(this.getContext(),listaDiEventi);
        eventListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /**
         * Impostazione del listener per la ricerca filtrabile
         * e per il click sulla voce della lista.
         */
        searchView.setOnQueryTextListener(this);
        eventListView.setOnItemClickListener(this);

        //registrazione del lister per il broadcast
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br, new IntentFilter("UpdateEvent"));
    }

    private void downloadAll(){
        searchView.setQuery("",true);
        searchView.clearFocus();
        switch (pageVisualizationType){
            case ALL_EVENTS:
                downloadAllEvents();
                break;
            case MY_EVENTS:
                downloadMyEvents();
                break;
        }
        pullToRefresh.setRefreshing(false);
        emptyViewPullToRefresh.setRefreshing(false);
    }

    /**
     * Ricevitore di messaggio broadcast per aggiornare la lista
     * degli eventi quando ne viene creato uno nuovo.
     */
    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        Boolean isUpdated = intent.getBooleanExtra("Updated", false);
        if(isUpdated){
            switch (pageVisualizationType){
                case ALL_EVENTS:
                    downloadAllEvents();
                    break;
                case MY_EVENTS:
                    downloadMyEvents();
                    break;
            }
        }
        }
    };

    /**
     * Abilita o disabilita la barra di avanzamento o il emptyTextView.
     *
     * @param isDownloading il flag che indica se il download è ancora in corso o meno
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

    /**
     * Metodo per scaricare tutti gli eventi in firebase nel caso in cui il tipo di visualizzazione sia SearchEventType.ALL_EVENTS
     */
    private void downloadAllEvents(){
        toggleDownloadingElements(true);

        //Scarica tutti gli eventi presenti nel database firestore
        Eventi.getAllEvent(list -> {
            if(list != null){

                for(Evento e : list){
                    //Scarica l'immagine
                    if(e.getIsImageUploaded() == true){
                        Eventi.downloadEventImage(e.getId(),(ClosureBitmap) bitmap -> {
                            e.setImageBitmap(bitmap);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
                listaDiEventi = list;

                //Configurazione dell'adattatore personalizzato per ListView.
                adapter = new EventAdapter(getContext(),listaDiEventi);
                eventListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                toggleDownloadingElements(false);
            }
        });
    }

    /**
     * Metodo per scaricare tutti gli eventi in firebase nel caso in cui il tipo di visualizzazione sia SearchEventType.MY_EVENTS
     */
    private void downloadMyEvents(){
        toggleDownloadingElements(true);

        //Scarica tutti gli eventi presenti nel database firestore
        Eventi.getAllMyEvents(list -> {
            if(list != null){

                for(Evento e : list){
                    //Scarica l'immagine
                    if(e.getIsImageUploaded() == true){
                        Eventi.downloadEventImage(e.getId(),(ClosureBitmap) bitmap -> {
                            e.setImageBitmap(bitmap);
                            adapter.notifyDataSetChanged();
                        });
                    }

                    //scarica il nome del gruppo se il creatore dell'evento è un gruppo
                    if(!e.getIdGruppoCreatore().isEmpty()){
                        Gruppi.getGroupName(e.getIdGruppoCreatore(), pair -> {
                            if(pair != null){
                                groupsName.put(e.getIdGruppoCreatore(),pair.first);
                                isAdminMap.put(e.getIdGruppoCreatore(),pair.second);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }
                }

                listaDiEventi = list;

                //Configurazione dell'adattatore personalizzato per ListView.
                adapter = new EventAdapter(getContext(),listaDiEventi,groupsName);
                eventListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                toggleDownloadingElements(false);
            }
        });
    }


    /*
        OVERRIDE DEL METODO NELL'INTERFACCIA AdapterView.OnItemClickListener
     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if(pageVisualizationType == SearchEventType.ALL_EVENTS){
            Intent i = new Intent(getContext(), EventDetailActivity.class);
            List<Evento> eventi = adapter.getFilteredData();
            i.putExtra("eventObj",(eventi == null ) ? listaDiEventi.get(position) : eventi.get(position));
            startActivity(i);
        }else if(pageVisualizationType == SearchEventType.MY_EVENTS){
            List<Evento> eventi = adapter.getFilteredData();
            Evento selectedEvent = (eventi == null ) ? listaDiEventi.get(position) : eventi.get(position);

            if(!selectedEvent.getIdGruppoCreatore().isEmpty()){
                if(isAdminMap.containsKey(selectedEvent.getIdGruppoCreatore())){
                    Boolean isAdmin = isAdminMap.get(selectedEvent.getIdGruppoCreatore());

                    if(isAdmin){
                        Intent i = new Intent(getContext(), EditEvent.class);
                        i.putExtra("eventObj",selectedEvent);
                        startActivityForResult(i,EDIT_EVENT_ACTIVITY_RESULT);
                    }else{
                        Intent i = new Intent(getContext(), EventDetailActivity.class);
                        i.putExtra("eventObj",selectedEvent);
                        startActivity(i);
                    }
                }
            }else{
                //Significa che l'autore dell'evento è l'utente che ha effettuato l'accesso
                Intent i = new Intent(getContext(), EditEvent.class);
                i.putExtra("eventObj",selectedEvent);
                startActivityForResult(i,EDIT_EVENT_ACTIVITY_RESULT);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == EDIT_EVENT_ACTIVITY_RESULT){
            System.out.println(resultCode);
            if(resultCode == Activity.RESULT_OK){
                switch (pageVisualizationType){
                    case ALL_EVENTS:
                        downloadAllEvents();
                        break;
                    case MY_EVENTS:

                        downloadMyEvents();
                        break;
                }
            }
        }

    }

    /*
        OVERRIDE DEI METODI NELL'INTERFACCIA SearchView.OnQueryTextListener
        forzare l'adattatore a filtrare l'elemento ListView in base alla query nella barra di ricerca.
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
     * Enum utilizzato per determinare il tipo di visualizzazione da utilizzare per questa istanza.
     */
    enum SearchEventType{
        ALL_EVENTS,
        MY_EVENTS;
    }
}


/**
 * Adapter creato appositamente per impostare i dati in ListView.
 * Utilizzato anche per filtrare i dati.
 */
class EventAdapter extends BaseAdapter implements Filterable {

    //Intero elenco di eventi presenti in ListView.
    private List<Evento> events=null;
    Map<String,String> groupsName = new HashMap<>();

    //Elenco degli eventi visualizzati dopo una ricerca.
    private List<Evento>filteredData = null;

    private Context context=null;
    ItemFilter mFilter = new ItemFilter();

    /**
     * Costruttore
     *
     * @param context il contest di riferimento
     * @param events lista degli eventi da mostrare nella listView
     */
    public EventAdapter(Context context,List<Evento> events,Map<String,String> groupsName)
    {
        this.events=events;
        this.context=context;
        this.filteredData = events;
        this.groupsName = groupsName;
    }

    /**
     * Costruttore
     *
     * @param context il contesto di riferimento
     * @param events lista degli eventi da mostrare nella listView
     */
    public EventAdapter(Context context,List<Evento> events)
    {
        this.events=events;
        this.context=context;
        this.filteredData = events;
    }

    /**
     * Ritorna la lista degli eventi da mostrare nella listView dopo una ricerca
     * @return
     */
    List<Evento> getFilteredData(){
        return filteredData;
    }


    /*
        OVERRIDE DI ALCUNI METODI NELLA CLASSE ASTRATTA BaseAdapter
     */

    @Override
    public int getCount()
    {
        return filteredData.size();
    }

    @Override
    public Object getItem(int position)
    {
        return filteredData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View v, ViewGroup vg)
    {
        if (v==null) v= LayoutInflater.from(context).inflate(R.layout.event_list_row, null);


        //Definizione della logica di visualizzazione.
        Evento e=(Evento) getItem(position);
        TextView titolo = v.findViewById(R.id.textViewTitolo);
        TextView desc = v.findViewById(R.id.textViewDescrizione);
        ImageView img = v.findViewById(R.id.imageViewProfile);
        TextView date = v.findViewById(R.id.textViewDateTime);
        TextView owner = v.findViewById(R.id.textViewOwner);
        TextView groupCreator = v.findViewById(R.id.textViewGroupCreator);

        titolo.setText(e.getNome());
        desc.setText(e.getDescrizione());
        date.setText(e.getNeutralData());
        if(e.getImageBitmap()!= null) img.setImageBitmap(e.getImageBitmap());
        else img.setImageResource(R.drawable.event_placeholder_icon);

        if(!e.getIdUtenteCreatore().isEmpty()){

            //Mostra l'etichetta "proprietario" nel caso in cui l'utente che ha effettuato l'accesso sia l'autore di questo evento
            if(!AuthHelper.getUserId().equals(e.getIdUtenteCreatore())){
                owner.setVisibility(View.INVISIBLE);
                groupCreator.setVisibility(View.INVISIBLE);
            } else {
                owner.setVisibility(View.VISIBLE);
                groupCreator.setVisibility(View.INVISIBLE);
            }
        }else if(!e.getIdGruppoCreatore().isEmpty()){

            //Nasconde l'etichetta "proprietario" e mostrare l'etichetta del nome del creatore se l'evento è
            //stato creato da un gruppo di cui l'utente è membro.
            if(groupsName.containsKey(e.getIdGruppoCreatore())){
                owner.setVisibility(View.INVISIBLE);
                groupCreator.setVisibility(View.VISIBLE);
                groupCreator.setText(groupsName.get(e.getIdGruppoCreatore()));
            }else{
                owner.setVisibility(View.INVISIBLE);
                groupCreator.setVisibility(View.INVISIBLE);
            }
        }

        return v;
    }


    /*
        OVERRIDE DEL METODO NELL'INTERFACCIA Filterable
     */

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    /**
     * Classe interna per definire un filtro personalizzato.
     */
    private class ItemFilter extends Filter{

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<Evento> list = events;
            int count = list.size();
            final ArrayList<Evento> nlist = new ArrayList<Evento>(count);
            Evento filterableEvent;

            for (int i = 0; i < count; i++) {
                filterableEvent = list.get(i);

                //Logica di confronto
                if(filterableEvent.getNome().toLowerCase().contains(filterString) || filterableEvent.getDescrizione().contains(filterString)){
                    nlist.add(filterableEvent);
                }

            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Evento>) results.values;
            notifyDataSetChanged();
        }
    }
}


