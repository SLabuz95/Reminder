package com.example.reminder;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
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

class SendingProgressListAdapter
        extends RecyclerView.Adapter<SendingProgressListViewHolder>{

    List<SendingProgressListData> list
            = Collections.emptyList();
    Context context;

    public SendingProgressListAdapter(List<SendingProgressListData> list,Context context)
    {
        this.list = list;
        this.context = context;

    }

    @NonNull
    @Override
    public SendingProgressListViewHolder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sending_progress_list_element, parent, false);
        return new SendingProgressListViewHolder(v);
    }

    @Override
    public void
    onBindViewHolder(@NonNull final SendingProgressListViewHolder viewHolder,
                     final int position)
    {
        SendingProgressListData data = list.get(position);
        viewHolder.contactView.setText(data.contact);
        if(data.failed == true){
            viewHolder.progressBarTextView.setText("Błąd");
            viewHolder.progressBarView.setBackgroundColor(Color.RED);
        }else {
            if (data.messageParts == null) {
                viewHolder.progressBarTextView.setText("Przygotowywanie");
            } else {
                viewHolder.progressBarTextView.setText(data.status + "/" + data.messageParts.size());
                viewHolder.progressBarView.setMax(data.messageParts.size());
                viewHolder.progressBarView.setProgress(data.status);
            }
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
}
