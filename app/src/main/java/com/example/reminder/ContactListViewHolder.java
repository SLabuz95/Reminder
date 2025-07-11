package com.example.reminder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ContactListViewHolder
        extends RecyclerView.ViewHolder implements View.OnClickListener{
    //CheckBox checkBox;
    TextView textView;
    ViewListener listener;
    View view;

    ContactListViewHolder(View itemView, ViewListener listener)
    {
        super(itemView);
        this.listener = listener;
        /*checkBox
                = (CheckBox)itemView
                .findViewById(R.id.contact_label);
        checkBox.setOnClickListener(this);*/
        textView = (TextView)itemView.findViewById(R.id.reminderContactLabel);
        textView.setOnClickListener(this);
        view  = itemView;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAdapterPosition());
    }

}
