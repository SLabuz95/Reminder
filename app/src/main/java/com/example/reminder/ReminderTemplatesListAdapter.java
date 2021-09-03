package com.example.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

class ReminderTemplatesListAdapter
        extends RecyclerView.Adapter<ReminderTemplateViewHolder> {

    List<ReminderTemplatesListData> list
            = Collections.emptyList();
    ViewListener listener;
    Context context;

    public ReminderTemplatesListAdapter(List<ReminderTemplatesListData> list, ViewListener listener,
                                Context context)
    {
        this.list = list;
        this.listener = listener;
        this.context = context;

    }

    @Override
    public ReminderTemplateViewHolder
    onCreateViewHolder(ViewGroup parent,
                       int viewType)
    {

        Context context
                = parent.getContext();
        LayoutInflater inflater
                = LayoutInflater.from(context);

        // Inflate the layout

        View photoView
                = inflater
                .inflate(R.layout.reminder_template_list_element,
                        parent, false);

        ReminderTemplateViewHolder viewHolder
                = new ReminderTemplateViewHolder(photoView, listener);
        return viewHolder;
    }

    @Override
    public void
    onBindViewHolder(final ReminderTemplateViewHolder viewHolder,
                     final int position)
    {
        final int index = viewHolder.getAdapterPosition();
        viewHolder.reminderTextView
                .setText(list.get(position).reminderText);
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listener.onClick(view, index);
            }
        });
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
