package com.vitandreasorino.savent.Utenti.GruppiTab.CreazioneGruppo;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

import Model.Pojo.ContactModel;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable{

    private List<ContactModel> contactListFiltered;
    private List<ContactModel> contactListAll;
    private Activity activity;
    private List<ContactModel> checkedContactList = new ArrayList<>();


    public ContactAdapter(Activity activity, ArrayList<ContactModel> arrayList) {
        this.contactListFiltered = arrayList;
        this.contactListAll = arrayList;
        this.activity = activity;

        notifyDataSetChanged();
    }

    public List<ContactModel> getCheckedContacts(){
        return checkedContactList;
    }

    /**
     * Metodo che serve per inizializzare i ViewHolder
     * @param parent :il ViewGroup in cui verrà aggiunta la nuova vista dopo essere stata associata a una posizione dell'adattatore.
     * @param viewType :il tipo di vista della nuova vista.
     * @return un nuovo ViewHolder che contiene una vista del tipo di vista specificato.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false);

        return new ViewHolder(view);
    }

    /**
     * Metodo che viene chiamato per ogni ViewHolder per associarlo all'adattatore
     * @param holder :ViewHolder che deve essere aggiornato per rappresentare il contenuto dell'elemento in una determinata posizione nel set di dati.
     * @param position :la posizione dell'elemento all'interno del set di dati dell'adattatore.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ContactModel model = contactListFiltered.get(position);

        holder.tvName.setText(model.getName());
        holder.tvNumber.setText(model.getNumber());
        holder.checkBox.setChecked(model.isChecked());

        if (model.isChecked()) {
            checkedContactList.add(model);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            /**
             * Metodo che serve per verificare se il contatto è stato selezionato. Se è stato selezionato questo viene aggiunto
             * alla lista dei contatti selezionati, altrimenti viene rimosso da questa lista
             * @param buttonView :la vista del pulsante composto il cui stato è cambiato.
             * @param isChecked :il nuovo stato selezionato di buttonView.
             */
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setChecked(isChecked);
                if(isChecked) checkedContactList.add(model);
                else checkedContactList.remove(model);
            }
        });
    }

    /**
     * Metodo che restituisce la dimensione della raccolta che contiene gli elementi che vogliamo visualizzare
     * @return il numero dei contatti filtrati
     */
    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    /**
     * Metodo che restituisce un filtro che può essere utilizzato per vincolare i dati
     * @return un oggetto di tipo Filter
     */
    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {

        /**
         * Metodo che viene richiamato in un thread di lavoro per filtrare i dati in base al vincolo.
         * @param constraint: vincolo che serve per il filtraggio dei dati
         * @return result: il risultato del filtraggio
         */
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<ContactModel> list = contactListAll;
            int count = list.size();
            final ArrayList<ContactModel> nlist = new ArrayList<ContactModel>(count);
            ContactModel filterableContact;

            for (int i = 0; i < count; i++) {
                filterableContact = list.get(i);

                //Logica del confronto
                if (filterableContact.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableContact);
                }

            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        /**
         * Metodo che viene richiamato nel thread dell'interfaccia utente per pubblicare i risultati del filtro nell'interfaccia utente.
         * @param constraint : vincolo utilizzato per il filtraggio dei dati
         * @param filterResults : il risultato del filtraggio
         */
        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            contactListFiltered = (ArrayList<ContactModel>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;
        CheckBox checkBox;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //Definizione del nome, del numero di telefono e della componente checkbox nella vista dell'item view
            tvName = itemView.findViewById(R.id.NameContact);
            tvNumber = itemView.findViewById(R.id.PhoneContact);
            checkBox = itemView.findViewById(R.id.checkboxContacts);

        }

    }

}
