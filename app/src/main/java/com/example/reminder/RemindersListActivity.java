package com.example.reminder;


import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.view.View;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

public class RemindersListActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    String messageTemplate;
    final int EDIT_REQUEST = 42;
    final int NEW_REQUEST = 43;
    final int SEND_MESSAGES = 44;
    final static int SHOW_LOG = 100;
    RemindersListAdapter adapter;
    RecyclerView recyclerView;
    List<RemindersListData> list = new ArrayList<>();
    boolean sendMessagesProcessing = false;
    private long PressedTime;
    private final long Timeout = 1000; // Change it to any value you want
    ViewListener listener;
    public static String prepareMessage(RemindersListData data){
        String message = data.messageTemplate;
        int month = data.date.get(Calendar.MONTH) + 1;
        int day = data.date.get(Calendar.DAY_OF_MONTH);
        String date = ( (day < 10)? "0" + day : String.valueOf(day))
                + "." + ((month < 10)? "0" + month : String.valueOf(month));
        String time = ( (data.hour < 10)? "0" + data.hour : String.valueOf(data.hour))
                + ":" + ((data.minute  < 10)? "0" + data.minute  : String.valueOf(data.minute));
        message = message.replaceAll("<date>", date);
        message = message.replaceAll("<time>", time);

        return message;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_list);

        Intent intent = getIntent();
        messageTemplate = intent.getStringExtra("reminderTemplate");
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }
        listener = new ViewListener() {
            @Override
            public void onClick(View view, final int position) {
                if (PressedTime + Timeout > System.currentTimeMillis()) return;
                PressedTime = System.currentTimeMillis();
                switch (view.getId()){
                    case R.id.deleteButton:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        list.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(RemindersListActivity.this);
                        builder.setMessage("Czy chcesz usunąć  \"" + list.get(position).getContact() + "\"?" ).setPositiveButton("Tak", dialogClickListener)
                                .setNegativeButton("Nie", dialogClickListener).show();
                    }
                    break;
                    case R.id.messageView:
                    {
                        Intent intent = new Intent(RemindersListActivity.this, AddReminderActivity.class);
                        intent.putExtra("id", position);
                        intent.putExtra("contact", list.get(position).contact);
                        intent.putExtra("customNumber", list.get(position).customNumber);
                        intent.putExtra("hour", list.get(position).hour);
                        intent.putExtra("minute", list.get(position).minute);
                        intent.putExtra("date", list.get(position).date);
                        startActivityForResult(intent, EDIT_REQUEST);
                    }
                    break;
                }
            }
        };

        recyclerView
                = (RecyclerView)findViewById(
                R.id.remindersList);
        adapter
                = new RemindersListAdapter(
                list, listener, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(RemindersListActivity.this));
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
        newReminderButtonOnClick(null);
    }

    public void newReminderButtonOnClick(View view){
        if (PressedTime + Timeout > System.currentTimeMillis()) return;
        PressedTime = System.currentTimeMillis();

        Intent intent = new Intent(RemindersListActivity.this, AddReminderActivity.class);
        startActivityForResult(intent, NEW_REQUEST);
    }
    public void acceptButtonClicked(View view){
        if (PressedTime + Timeout > System.currentTimeMillis()) return;
        PressedTime = System.currentTimeMillis();

        if(!list.isEmpty()){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            sendSmsMessages();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(RemindersListActivity.this);
            builder.setMessage("Czy chcesz wysłać te wiadomości?" ).setPositiveButton("Tak", dialogClickListener)
                    .setNegativeButton("Nie", dialogClickListener).show();

        }else{
            Toast.makeText(getBaseContext(), "Brak wiadomości do wysłania",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelButtonClicked(View view){
        if (PressedTime + Timeout > System.currentTimeMillis()) return;
        PressedTime = System.currentTimeMillis();

        if(!list.isEmpty()){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            setResult(RESULT_CANCELED);
                            finish();
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(RemindersListActivity.this);
            builder.setMessage("Czy chcesz zrezygnować z wysłania tych wiadomości?" ).setPositiveButton("Tak", dialogClickListener)
                    .setNegativeButton("Nie", dialogClickListener).show();
        }else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }
    @Override
    public void onBackPressed()
    {
        cancelButtonClicked(null);
    }
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent intent) {
        boolean requestFinish = false;
        switch (requestCode){
            case NEW_REQUEST:
            {
                if(resultCode == RESULT_OK) {
                    Bundle data = intent.getExtras();
                    String contact = data.getString("contact");
                    String customNumber = data.getString("customNumber");
                    Calendar date = (Calendar) data.get("date");
                    int hour = data.getInt("hour");
                    int minute = data.getInt("minute");
                    list.add(new RemindersListData(contact, customNumber, date, hour, minute, messageTemplate));
                    adapter.notifyItemInserted(list.size());
                }else{
                    if(resultCode == RESULT_CANCELED){
                        if(list.isEmpty()){
                            requestFinish = true;
                        }
                    }
                }
            }
            break;
            case EDIT_REQUEST:
            {
                if(resultCode == RESULT_OK) {
                    Bundle data = intent.getExtras();
                    String contact = data.getString("contact");
                    String customNumber = data.getString("customNumber");
                    Calendar date = (Calendar) data.get("date");
                    int hour = data.getInt("hour");
                    int minute = data.getInt("minute");
                    int index = data.getInt("id", -1);
                    if (index > -1) {
                        list.get(index).contact = contact;
                        list.get(index).customNumber = customNumber;
                        list.get(index).date = date;
                        list.get(index).hour = hour;
                        list.get(index).minute = minute;
                        adapter.notifyItemChanged(index);
                    }
                }
            }
            break;
            case SEND_MESSAGES:
            {
                if(resultCode != RESULT_OK)
                    setResult(SHOW_LOG);
                else
                    setResult(RESULT_CANCELED);
                finish();
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestFinish == true){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    protected void sendSmsMessages(){
        if(sendMessagesProcessing == false){
            sendMessagesProcessing = true;

            Toast.makeText(getBaseContext(), "Przygotowanie do wysłania...",
                    Toast.LENGTH_SHORT).show();

            List<LogData> logList = new ArrayList<>(list.size());

            for(int messageIndex = 0; messageIndex < list.size(); messageIndex++){
                RemindersListData reminderData = this.list.get(messageIndex);
                String message = prepareMessage(reminderData);
                logList.add(new LogData(reminderData.contact.isEmpty()? reminderData.customNumber : reminderData.contact,
                        reminderData.customNumber, message));
            }

            Intent intent = new Intent(RemindersListActivity.this, SendingProgressActivity.class);
            intent.putExtra("logList",  (Serializable) logList);
            startActivityForResult(intent, SEND_MESSAGES);

        }else{
            Toast.makeText(getBaseContext(), "Już trwa wysyłanie.",
                    Toast.LENGTH_SHORT).show();
        }
    }



}