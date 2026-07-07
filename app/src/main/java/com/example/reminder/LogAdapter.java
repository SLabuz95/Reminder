package com.example.reminder;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

class LogAdapter
        extends RecyclerView.Adapter<LogViewHolder> implements Filterable {

    List<LogData> list
            = Collections.emptyList();
    List<LogData> listFull
            = Collections.emptyList();
    ViewListener listener;
    Context context;

    public LogAdapter(List<LogData> list, ViewListener listener,
                      Context context)
    {
        this.list = list;
        listFull = new ArrayList<>(list);
        this.listener = listener;
        this.context = context;

    }

    @NonNull
    @Override
    public LogViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_list_element, parent, false);
        return new LogViewHolder(v, listener);
    }

    @Override
    public void
    onBindViewHolder(@NonNull final LogViewHolder viewHolder,
                     final int position)
    {
        LogData data = list.get(position);
        viewHolder.contactView.setText(data.contact);
        viewHolder.dateTimeView.setText(
                (data.dateTime.get(Calendar.DAY_OF_MONTH) < 10? "0" + data.dateTime.get(Calendar.DAY_OF_MONTH) : data.dateTime.get(Calendar.DAY_OF_MONTH))+"/"+
                (data.dateTime.get(Calendar.MONTH) + 1 < 10? "0" + (data.dateTime.get(Calendar.MONTH) + 1) : (data.dateTime.get(Calendar.MONTH) + 1))+"/"+
                data.dateTime.get(Calendar.YEAR)+" "+
                (data.dateTime.get(Calendar.HOUR_OF_DAY) < 10? "0" + data.dateTime.get(Calendar.HOUR_OF_DAY) : data.dateTime.get(Calendar.HOUR_OF_DAY))+":"+
                (data.dateTime.get(Calendar.MINUTE) < 10? "0" + data.dateTime.get(Calendar.MINUTE) : data.dateTime.get(Calendar.MINUTE))+":"+
                (data.dateTime.get(Calendar.SECOND) < 10? "0" + data.dateTime.get(Calendar.SECOND) : data.dateTime.get(Calendar.SECOND)));
        viewHolder.partsInfoView.setText(data.partsInfo);
        viewHolder.messageContentView.setText(data.messageContent);
        if(data.expanded == true){
            viewHolder.partsInfoView.setVisibility(VISIBLE);
            viewHolder.messageContentView.setVisibility(VISIBLE);
        }else{
            viewHolder.partsInfoView.setVisibility(GONE);
            viewHolder.messageContentView.setVisibility(GONE);
        }
        if(data.success == true){
            viewHolder.statusIcon.setVisibility(GONE);
        }else{
            viewHolder.statusIcon.setVisibility(VISIBLE);
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
            List<LogData> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(listFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (LogData item : listFull) {
                    if (item.contact.toLowerCase().startsWith(filterPattern) ) {
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
