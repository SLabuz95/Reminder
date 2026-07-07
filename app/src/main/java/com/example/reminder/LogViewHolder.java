package com.example.reminder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class LogViewHolder
        extends RecyclerView.ViewHolder implements View.OnClickListener{
    TextView contactView;
    TextView dateTimeView;
    TextView partsInfoView;
    TextView messageContentView;
    ImageView statusIcon;
    View view;
    ViewListener listener;

    LogViewHolder(View itemView, ViewListener listener)
    {
        super(itemView);
        this.listener = listener;
        contactView = (TextView)itemView.findViewById(R.id.callerView);
        dateTimeView = (TextView)itemView.findViewById(R.id.dateTimeView);
        partsInfoView = (TextView)itemView.findViewById(R.id.partsInfoView);
        messageContentView = (TextView)itemView.findViewById(R.id.messageContentView);
        statusIcon = (ImageView)itemView.findViewById(R.id.failIcon);
        contactView.setOnClickListener(this);
        partsInfoView.setOnClickListener(this);
        messageContentView.setOnClickListener(this);
        statusIcon.setOnClickListener(this);
        view  = itemView;
    }

    @Override
    public void onClick(View view) {
        listener.onClick(view, getAdapterPosition());
    }

}
