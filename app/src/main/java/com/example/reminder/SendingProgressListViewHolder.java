package com.example.reminder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class SendingProgressListViewHolder
        extends RecyclerView.ViewHolder {
    TextView contactView;
    TextView progressBarTextView;
    ProgressBar progressBarView;
    View view;

    SendingProgressListViewHolder(View itemView)
    {
        super(itemView);
        contactView = (TextView)itemView.findViewById(R.id.sending_progress_element_contact);
        progressBarTextView = (TextView)itemView.findViewById(R.id.sending_element_bar_text);
        progressBarView = (ProgressBar)itemView.findViewById(R.id.sending_element_bar);
        view  = itemView;
    }

}
