package com.example.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

class RemindersListAdapter
        extends RecyclerView.Adapter<RemindersListViewHolder> /*implements Filterable*/ {

    List<RemindersListData> list
            = Collections.emptyList();
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
        RemindersListData data = list.get(position);
        viewHolder.messageView
                    .setText("Wiadomość do: " +
                            ((!data.contact.isEmpty())? (data.contact + " (" + data.customNumber + ")")  :
                            data.customNumber)
                            + "\n"
                            + RemindersListActivity.prepareMessage(list.get(position)));
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

}
