package com.example.reminder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RemindersListViewHolder
        extends RecyclerView.ViewHolder implements View.OnClickListener{
    //CheckBox checkBox;
    TextView messageView;
    ImageButton deleteButton;
    View view;
    ViewListener listener;

    RemindersListViewHolder(View itemView, ViewListener listener)
    {
        super(itemView);
        this.listener = listener;
        /*checkBox
                = (CheckBox)itemView
                .findViewById(R.id.contact_label);
        checkBox.setOnClickListener(this);*/
        messageView = (TextView)itemView.findViewById(R.id.messageView);
        deleteButton = (ImageButton)itemView.findViewById(R.id.deleteButton);
        messageView.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        view  = itemView;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAdapterPosition());
    }

}
