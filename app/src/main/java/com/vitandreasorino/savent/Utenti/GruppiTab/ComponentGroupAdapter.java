package com.vitandreasorino.savent.Utenti.GruppiTab;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Model.Pojo.Utente;

/**
 * ComponentGroupAdapter creato per implementare la lista dei componenti del gruppo
 */
public class ComponentGroupAdapter extends BaseAdapter implements Filterable {

    private List<Utente> users = null;
    private List<Utente> filteredData = null;
    private Context context = null;
    private String idAdmin;
    ItemFilter mFilter = new ItemFilter();

    //Costruttore
    public ComponentGroupAdapter(Context context, String idAdmin) {
        this.users = new ArrayList<>();
        this.context=context;
        this.filteredData = new ArrayList<>();
        this.idAdmin = idAdmin;
    }

    //Costruttore
    public ComponentGroupAdapter(Context context) {
        this.users = new ArrayList<>();
        this.context=context;
        this.filteredData = new ArrayList<>();
    }

    public void mergeNewList(List<Utente> newList){
        List<Utente> removedOnes = new ArrayList<>(users);
        List<Utente> addedOnes = new ArrayList<>(newList);

        //Calcolo di quelli rimossi
        removedOnes.removeAll(newList);
        users.removeAll(removedOnes);
        filteredData.removeAll(removedOnes);

        //Calcolo di quelli aggiunti
        addedOnes.removeAll(users);
        users.addAll(addedOnes);
        filteredData.addAll(addedOnes);
    }

    public void addItemsToList(List<Utente> list){
        users.addAll(list);
        filteredData.addAll(list);
    }

    public void addItemToList(Utente user){
        if(users.indexOf(user) < 0){
            users.add(user);
            filteredData.add(user);
        }
    }

    public void removeItemFromList(Utente user){

        if(users.indexOf(user) >= 0) users.remove(user);
        if(filteredData.indexOf(user) >= 0) filteredData.remove(user);

    }

    public List<Utente> getFilteredData(){
        return filteredData;
    }

    public ArrayList<Utente> getNoFilteredData(){
        return new ArrayList<>(users);
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
     * impostiamo il layout per gli elementi dell'elenco utilizzando la classe LayoutInflater e quindi aggiungiamo i dati
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
            v = LayoutInflater.from(context).inflate(R.layout.component_group_list_row, null);
        }

        Utente utente = (Utente) getItem(position);
        ImageView img = v.findViewById(R.id.imageViewComponent);
        TextView txtName = v.findViewById(R.id.textViewNameComponet);
        TextView txtSurname = v.findViewById(R.id.textViewSurnameComponet);
        TextView textAdmin = v.findViewById(R.id.textAdminGroup);

        //imposta il testo per il nome e la cognome componente del gruppo
        txtName.setText(utente.getNome());
        txtSurname.setText(utente.getCognome());

        //imposta l'immagine dei componenti dei gruppi
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

    /**
     * Gestisce le operazioni per il filtraggio della SearchView
     */
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

        /**
         * stampa i risultati del filtro
         * @param constraint
         * @param results
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<Utente>) results.values;

            // aggiorna l'elenco con i dati filtrati
            notifyDataSetChanged();
        }

    }

}
