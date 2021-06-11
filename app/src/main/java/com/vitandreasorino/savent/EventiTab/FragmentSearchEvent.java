package com.vitandreasorino.savent.EventiTab;

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
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Helper.AuthHelper;
import Model.DB.Eventi;
import Model.Pojo.Evento;

public class FragmentSearchEvent extends Fragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

    ListView eventListView;
    SearchView searchView;
    EventAdapter adapter;

    //List of event shown in he ListView.
    List<Evento> listaDiEventi = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        eventListView = view.findViewById(R.id.eventListView);
        searchView = view.findViewById(R.id.searchViewEvent);

        //Download all the event present in the firestore database
        Eventi.getAllEvent(list -> {
            if(list != null){

                for(Evento e : list){
                    //download the image
                    if(e.getIsImageUploaded() == true){
                        Eventi.downloadEventImage(e.getId(), bitmap -> {
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
            }
        });

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


    /*

        OVERRIDE OF THE METHOD IN THE AdapterView.OnItemClickListener INTERFACE

     */

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent i = new Intent(getContext(), EventDetailActivity.class);
        List<Evento> eventi = adapter.getFilteredData();
        i.putExtra("eventObj",(eventi == null ) ? listaDiEventi.get(position) : eventi.get(position));
        startActivity(i);
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
}


/**
 * Adepter specifically created to sets the data in the ListView.
 * Also used to filter the data.
 */
class EventAdapter extends BaseAdapter implements Filterable {

    //Entire list of events present in the ListView.
    private List<Evento> events=null;

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

        titolo.setText(e.getNome());
        desc.setText(e.getDescrizione());
        date.setText(e.getNeutralData());
        if(e.getImageBitmap()!= null) img.setImageBitmap(e.getImageBitmap());
        else img.setImageResource(R.drawable.event_placeholder_icon);
        if(!AuthHelper.getUserId().equals(e.getIdUtenteCreatore())) owner.setText("");
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


