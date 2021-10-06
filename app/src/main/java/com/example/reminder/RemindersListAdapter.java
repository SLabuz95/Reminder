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

class RemindersListAdapter
        extends RecyclerView.Adapter<RemindersListViewHolder> /*implements Filterable*/ {

    List<RemindersListData> list
            = Collections.emptyList();
    /*List<RemindersListData> listFull
            = Collections.emptyList();*/
    ViewListener listener;
    Context context;

    public RemindersListAdapter(List<RemindersListData> list, ViewListener listener,
                              Context context)
    {
        this.list = list;
        //listFull = new ArrayList<>(list);
        this.listener = listener;
        this.context = context;

    }

    @NonNull
    @Override
    public RemindersListViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminders_list_element, parent, false);
        return new RemindersListViewHolder(v, listener);
    }

    @Override
    public void
    onBindViewHolder(@NonNull final RemindersListViewHolder viewHolder,
                     final int position)
    {
        viewHolder.textView
                    .setText(list.get(position).contact + "\n" + list.get(position).time);
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
/*
    @Override
    public Filter getFilter() {
        return exampleFilter;
    }
    private Filter exampleFilter = new Filter() {
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
*/
}
