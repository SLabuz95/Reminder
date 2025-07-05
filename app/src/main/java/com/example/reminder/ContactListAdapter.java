package com.example.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ContactListAdapter
        extends RecyclerView.Adapter<ContactListViewHolder> implements Filterable {

    List<ContactListData> list
            = Collections.emptyList();
    List<ContactListData> listFull
            = Collections.emptyList();
    ViewListener listener;
    Context context;

    public ContactListAdapter(List<ContactListData> list, ViewListener listener,
                                        Context context)
    {
        this.list = list;
        listFull = new ArrayList<>(list);
        this.listener = listener;
        this.context = context;

    }

    @NonNull
    @Override
    public ContactListViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_element, parent, false);
        return new ContactListViewHolder(v, listener);
    }

    @Override
    public void
    onBindViewHolder(@NonNull final ContactListViewHolder viewHolder,
                     final int position)
    {
        if(list.get(position).contactName.isEmpty()){
            viewHolder.textView
                .setText(list.get(position).phoneNumber);
        }else {
            viewHolder.textView
                    .setText(list.get(position).contactName + "\n" + list.get(position).phoneNumber);
        }

    }

    @Override
    public int getItemCount()
    {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(
            RecyclerView recyclerView)
    {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private final Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<ContactListData> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (ContactListData item : listFull) {
                    if (item.contactName.toLowerCase().startsWith(filterPattern) || item.phoneNumber.toLowerCase().startsWith(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
