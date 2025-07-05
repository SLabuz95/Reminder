package com.example.reminder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ReminderTemplateViewHolder
        extends RecyclerView.ViewHolder implements View.OnClickListener{
    TextView reminderTextView;
    ImageButton editButton;
    ImageButton deleteButton;
    ViewListener listener;
    View view;

    ReminderTemplateViewHolder(View itemView, ViewListener listener)
    {
        super(itemView);
        this.listener = listener;
        reminderTextView
                = (TextView)itemView
                .findViewById(R.id.reminderContact);
        reminderTextView.setOnClickListener(this);
        editButton = (ImageButton)itemView.findViewById(R.id.editButton);
        editButton.setOnClickListener(this);
        deleteButton = (ImageButton)itemView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(this);
        view  = itemView;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAdapterPosition());
    }

}