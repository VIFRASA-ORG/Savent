package com.vitandreasorino.savent.GruppiTab.CreazioneGruppo;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vitandreasorino.savent.R;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> implements Filterable {

     List<ContactModel> contactListFiltered;
     List<ContactModel> contactListAll;
     Activity activity;


    public ContactAdapter(Activity activity, ArrayList<ContactModel> arrayList) {
        this.contactListFiltered= arrayList;
        this.contactListAll = arrayList;
        this.activity = activity;

        notifyDataSetChanged();
    }

    @NonNull
    @Override

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {

        ContactModel model = contactListFiltered.get(position);
        //Set name
        holder.tvName.setText(model.getName());
       //set number
        holder.tvNumber.setText(model.getNumber());
    }

    @Override
    public int getItemCount() {
        return contactListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
    Filter filter = new Filter(){
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

                //Logic of confront
                if(filterableContact.getName().toLowerCase().contains(filterString)){
                    nlist.add(filterableContact);
                }

            }
            results.values = nlist;
            results.count = nlist.size();
            Log.i("ciao", nlist.toString());
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            contactListFiltered = (ArrayList<ContactModel>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvNumber;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName= itemView.findViewById(R.id.NameContact);
            tvNumber= itemView.findViewById(R.id.PhoneContact);
        }

    }
}
