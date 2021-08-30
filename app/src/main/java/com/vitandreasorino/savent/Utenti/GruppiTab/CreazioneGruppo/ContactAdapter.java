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

import Model.Pojo.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable{

    private List<Contact> contactListFiltered;
    private List<Contact> contactListAll;
    private Activity activity;
    private List<Contact> checkedContactList = new ArrayList<>();


    public ContactAdapter(Activity activity, ArrayList<Contact> arrayList) {
        this.contactListFiltered = arrayList;
        this.contactListAll = arrayList;
        this.activity = activity;

        notifyDataSetChanged();
    }

    public List<Contact> getCheckedContacts(){
        return checkedContactList;
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Contact model = contactListFiltered.get(position);

        holder.tvName.setText(model.getName());
        holder.tvNumber.setText(model.getNumber());
        holder.checkBox.setChecked(model.isChecked());

        if (model.isChecked()) {
            checkedContactList.add(model);
        }

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.setChecked(isChecked);
                if(isChecked) checkedContactList.add(model);
                else checkedContactList.remove(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<Contact> list = contactListAll;
            int count = list.size();
            final ArrayList<Contact> nlist = new ArrayList<Contact>(count);
            Contact filterableContact;

            for (int i = 0; i < count; i++) {
                filterableContact = list.get(i);

                //Logic of confront
                if (filterableContact.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableContact);
                }

            }
            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            contactListFiltered = (ArrayList<Contact>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber;
        CheckBox checkBox;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.NameContact);
            tvNumber = itemView.findViewById(R.id.PhoneContact);
            checkBox = itemView.findViewById(R.id.checkboxContacts);

        }

    }

}
