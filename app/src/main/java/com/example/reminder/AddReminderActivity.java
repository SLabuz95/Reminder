package com.example.reminder;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddReminderActivity extends AppCompatActivity {
    final int EDIT_TEMPLATE_REQUEST = 42;
    final int NEW_TEMPLATE_REQUEST = 43;
    final int NEW_REMINDER_REQUEST = 44;
    ReminderTemplatesListAdapter adapter;
    RecyclerView recyclerView;
    List<ReminderTemplatesListData> list = new ArrayList<>();
    ViewListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reminder_activity);
        DatePicker datePicker = ((DatePicker)findViewById(R.id.datePicker));
        datePicker.findViewById(getResources().getIdentifier("year", "id", "android")).setVisibility(View.GONE);

        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        try {
            readReminderTextsFromFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        listener = new ViewListener() {
            @Override
            public void onClick(View view, final int position) {
                switch (view.getId()){
                    case R.id.editButton:
                    {
                        Intent intent = new Intent(AddReminderActivity.this, NewReminderActivity.class);
                        intent.putExtra("templateId", position);
                        intent.putExtra("templateContent", list.get(position).reminderText);
                        startActivityForResult(intent, EDIT_TEMPLATE_REQUEST);
                    }
                    break;
                    case R.id.deleteButton:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        list.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        try {
                                            writeReminderTemplates();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddReminderActivity.this);
                        builder.setMessage("Czy chcesz usunąć szablon \"" + list.get(position).reminderText + "\"?" ).setPositiveButton("Tak", dialogClickListener)
                                .setNegativeButton("Nie", dialogClickListener).show();
                    }
                    break;
                    case R.id.reminderName:
                    case R.id.reminderListElement:
                    {
                        Intent intent = new Intent(AddReminderActivity.this, ContactListActivity.class);
                        String reminder = list.get(position).reminderText;
                        DatePicker datePicker = ((DatePicker)findViewById(R.id.datePicker));
                        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
                        String date = ( (datePicker.getDayOfMonth() < 10)? "0" + String.valueOf(datePicker.getDayOfMonth()) : String.valueOf(datePicker.getDayOfMonth()))
                                + "." + ((datePicker.getMonth() + 1 < 10)? "0" + String.valueOf(datePicker.getMonth() + 1) : String.valueOf(datePicker.getMonth() + 1));
                        String time = ( (timePicker.getHour() < 10)? "0" + String.valueOf(timePicker.getHour()) : String.valueOf(timePicker.getHour()))
                                + ":" + ((timePicker.getMinute()  < 10)? "0" + String.valueOf(timePicker.getMinute())  : String.valueOf(timePicker.getMinute()));
                        reminder = reminder.replaceAll("<date>", date);
                        reminder = reminder.replaceAll("<time>", time);
                        intent.putExtra("reminder", reminder);
                        startActivity(intent);
                    }
                    break;

                }
            }
        };

        //list.add(new ReminderTemplatesListData("Test"));
        //list.add(new ReminderTemplatesListData("abc"));
        recyclerView
                = (RecyclerView)findViewById(
                R.id.reminderTemplatesList);
        adapter
                = new ReminderTemplatesListAdapter(
                list, listener, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(AddReminderActivity.this));
    }


    private void readReminderTextsFromFile() throws IOException {
        FileInputStream fin = openFileInput(getResources().getString(R.string.eor_marker));
        int c;
        String temp="";
        String tempEOF = "<EOF>";
        int tempEOFIndex = -1;
        while( (c = fin.read()) != -1){
            if(tempEOFIndex > -1){
                if(c == tempEOF.charAt(tempEOFIndex + 1)){
                    if (tempEOFIndex + 1 == tempEOF.length() - 1) {
                        list.add(new ReminderTemplatesListData(temp = new String(temp.getBytes("ISO-8859-1"), "UTF-8")));
                        temp = "";
                        tempEOFIndex = -1;
                    }else{
                        tempEOFIndex++;
                    }
                }else{
                    temp += tempEOF.substring(0, tempEOFIndex + 1) + (char)c;
                    tempEOFIndex = -1;
                }
            }else{
                if(c == tempEOF.charAt(0)){
                    tempEOFIndex = 0;
                }else {
                    temp += Character.toString((char) c);
                }
            }
        }
        fin.close();
    }

    private void writeReminderTemplates() throws IOException {
        FileOutputStream file = openFileOutput( getResources().getString(R.string.eor_marker), Context.MODE_PRIVATE);
        final RecyclerView lv = (RecyclerView) findViewById(R.id.reminderTemplatesList);
        ReminderTemplatesListAdapter adapter = (ReminderTemplatesListAdapter) lv.getAdapter();
        for(int i = 0; i < adapter.getItemCount(); i++){
            file.write(adapter.list.get(i).reminderText.toString().getBytes());
            file.write("<EOF>".getBytes());
        }
        file.close(); //File closed
    }

    public void newTemplateButtonOnClick(View view){
        Intent intent = new Intent(AddReminderActivity.this, NewReminderActivity.class);
        startActivityForResult(intent, NEW_TEMPLATE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        switch (requestCode){
            case NEW_TEMPLATE_REQUEST:
            {
                if(resultCode == RESULT_OK) {
                    String newTemplate = intent.getStringExtra("template");
                    if (!newTemplate.isEmpty()) {
                        list.add(new ReminderTemplatesListData(newTemplate));
                        adapter.notifyItemInserted(list.size());
                        try {
                            writeReminderTemplates();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            break;
            case EDIT_TEMPLATE_REQUEST:
            {
                if(resultCode == RESULT_OK) {
                    int templateIndex = intent.getIntExtra("templateId", -1);
                    String newTemplate = intent.getStringExtra("template");
                    if (!newTemplate.isEmpty() && templateIndex > -1) {
                        list.get(templateIndex).reminderText = newTemplate;
                        adapter.notifyItemChanged(templateIndex);
                        try {
                            writeReminderTemplates();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

}
