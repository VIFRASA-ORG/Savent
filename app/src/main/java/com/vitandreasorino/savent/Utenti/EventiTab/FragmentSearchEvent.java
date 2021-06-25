package com.vitandreasorino.savent.Utenti.EventiTab;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    //List of event shown in he ListView.
    List<Evento> listaDiEventi = new ArrayList<>();

    //Used to store the names of creator group in case the event is created by a group
    //The key is the groupId, the value is the name of the group
    Map<String,String> groupsName = new HashMap<>();
    Map<String,Boolean> isAdminMap = new HashMap<>();

    //Default visualization
    SearchEventType pageVisualizationType = SearchEventType.ALL_EVENTS;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_event, container, false);
    }

    /**
     * Constructor used to specify the visualization type.
     *
     * @param mode the visualization type.
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

        eventListView.setEmptyView(view.findViewById(R.id.emptyResults));

        switch (pageVisualizationType){
            case ALL_EVENTS:
                downloadAllEvents();
                break;
            case MY_EVENTS:
                downloadMyEvents();
                break;
        }

        adapter = new EventAdapter(this.getContext(),listaDiEventi);
        eventListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /**
         * Setting the listener for the filterable search
         * and for the click on the item in the list.
         */
        searchView.setOnQueryTextListener(this);
        eventListView.setOnItemClickListener(this);
    }

    /**
     * Enable or disable the progress bar or the emptyTextView.
     *
     * @param isDownloading the flag indicating if the download is still going or not
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
     * Method to download all the event in firebase in case the type of visualization is SearchEventType.ALL_EVENTS
     */
    private void downloadAllEvents(){
        toggleDownloadingElements(true);

        //Download all the event present in the firestore database
        Eventi.getAllEvent(list -> {
            if(list != null){

                for(Evento e : list){
                    //download the image
                    if(e.getIsImageUploaded() == true){
                        Eventi.downloadEventImage(e.getId(),(ClosureBitmap) bitmap -> {
                            e.setImageBitmap(bitmap);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
                listaDiEventi = list;

                //Setting up the custom made adapter for the ListView.
                adapter = new EventAdapter(getContext(),listaDiEventi);
                eventListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                toggleDownloadingElements(false);
            }
        });
    }

    /**
     * Method to download all the event in firebase in case the type of visualization is SearchEventType.MY_EVENTS
     */
    private void downloadMyEvents(){
        toggleDownloadingElements(true);

        //Download all the event present in the firestore database
        Eventi.getAllMyEvents(list -> {
            if(list != null){

                for(Evento e : list){
                    //download the image
                    if(e.getIsImageUploaded() == true){
                        Eventi.downloadEventImage(e.getId(),(ClosureBitmap) bitmap -> {
                            e.setImageBitmap(bitmap);
                            adapter.notifyDataSetChanged();
                        });
                    }

                    //download the group name if the creator of the event is a group
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

                //Setting up the custom made adapter for the ListView.
                adapter = new EventAdapter(getContext(),listaDiEventi,groupsName);
                eventListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                toggleDownloadingElements(false);
            }
        });
    }


    /*

        OVERRIDE OF THE METHOD IN THE AdapterView.OnItemClickListener INTERFACE

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
                //Means that the creator of the event is the logged in user
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
     * Enum used to determine the type of visualization to use for this instance.
     */
    enum SearchEventType{
        ALL_EVENTS,
        MY_EVENTS;
    }
}


/**
 * Adepter specifically created to sets the data in the ListView.
 * Also used to filter the data.
 */
class EventAdapter extends BaseAdapter implements Filterable {

    //Entire list of events present in the ListView.
    private List<Evento> events=null;
    Map<String,String> groupsName = new HashMap<>();

    //List of events shown after a search.
    private List<Evento>filteredData = null;

    private Context context=null;
    ItemFilter mFilter = new ItemFilter();

    /**
     * Constructor
     *
     * @param context the reference context
     * @param events list of event to show in the ListView.
     */
    public EventAdapter(Context context,List<Evento> events,Map<String,String> groupsName)
    {
        this.events=events;
        this.context=context;
        this.filteredData = events;
        this.groupsName = groupsName;
    }

    /**
     * Constructor
     *
     * @param context the reference context
     * @param events list of event to show in the ListView.
     */
    public EventAdapter(Context context,List<Evento> events)
    {
        this.events=events;
        this.context=context;
        this.filteredData = events;
    }

    /**
     * Return the list of the event shown in the ListView after a search.
     * @return
     */
    List<Evento> getFilteredData(){
        return filteredData;
    }


    /*

        OVERRIDE OF SOME METHODS IN THE BaseAdapter ABSTRACT CLASS

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


        //Definition of the visualization logic.
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
            //Showing the label "owner" in case the logged in user is the creator of this event
            if(!AuthHelper.getUserId().equals(e.getIdUtenteCreatore())){
                owner.setVisibility(View.INVISIBLE);
                groupCreator.setVisibility(View.INVISIBLE);
            } else {
                owner.setVisibility(View.VISIBLE);
                groupCreator.setVisibility(View.INVISIBLE);
            }
        }else if(!e.getIdGruppoCreatore().isEmpty()){
            //Hiding the label "owner" and showing the creator name label if the event is
            //being create by a group in which the user is a member.
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

        OVERRIDE OF THE METHOD IN THE Filterable INTERFACE

     */

    @Override
    public Filter getFilter() {
        return mFilter;
    }


    /**
     * Inner class to define a custom filter.
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

                //Logic of confront
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


