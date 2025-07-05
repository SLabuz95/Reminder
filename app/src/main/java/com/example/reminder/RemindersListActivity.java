package com.example.reminder;


import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Intent.FILL_IN_DATA;
import static android.telephony.SubscriptionManager.getDefaultSmsSubscriptionId;

import static androidx.core.view.WindowCompat.getInsetsController;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
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
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RemindersListActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    String messageTemplate;
    final int EDIT_REQUEST = 42;
    final int NEW_REQUEST = 43;
    final String SENT = "SMS_SENT";
    RemindersListAdapter adapter;
    RecyclerView recyclerView;
    List<RemindersListData> list = new ArrayList<>();
    List<RemindersListData> sendMessagesProcessingList = null;
    public static class RemindersListDataInfo{
        public RemindersListDataInfo(String message, int numbOfParts){
            this.message = message;
            this.numbOfParts = numbOfParts;
        }
        public String message;
        public int numbOfParts = 1;
        public boolean success = true;
    };
    List<RemindersListDataInfo> sendMessagesProcessingInfoList = null;
    boolean sendMessagesProcessing = false;
    ViewListener listener;
    BroadcastReceiver sendSMS = null;
    boolean sendSMSRegistered = false;
    private long PressedTime;
    private final long Timeout = 1000; // Change it to any value you want
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
        sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                if(sendMessagesProcessing == true) {
                    int id = arg1.getIntExtra("id", -1);
                    int partId = arg1.getIntExtra("partId", -1);
                    if (id != -1) {
                        switch (getResultCode()) {
                            case Activity.RESULT_OK:
                                break;
                            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            case SmsManager.RESULT_ERROR_NO_SERVICE:
                            case SmsManager.RESULT_ERROR_NULL_PDU:
                            case SmsManager.RESULT_ERROR_RADIO_OFF:
                            default:
                            {
                                if(sendMessagesProcessingInfoList.get(id).success == true){ // Fail only once
                                    sendMessagesProcessingInfoList.get(id).success = false;
                                    sendMessagesProcessingList.add(list.get(id));
                                }
                            }
                            break;
                        }
                        if (id + 1 == list.size()) { // Finalize
                            if (sendMessagesProcessingInfoList.get(id).numbOfParts != 1) {
                                if (sendMessagesProcessingInfoList.get(id).success == false) { // no success - finish
                                    sendMessagesProcessing = false;
                                }else{
                                    if(partId + 1 == sendMessagesProcessingInfoList.get(id).numbOfParts){ // Finish with success
                                        sendMessagesProcessing = false;
                                    }
                                }
                            } else { //Single Part message
                                sendMessagesProcessing = false;
                            }
                        }
                        // If processing finished
                        if(sendMessagesProcessing == false) {
                            if (sendMessagesProcessingList.isEmpty()) { // No errors
                                finish();
                                Toast.makeText(getBaseContext(), "Wysyłanie zakończone.\n 0 błędów.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getBaseContext(), "Wysyłanie zakończone.\n" + sendMessagesProcessingList.size() + " błędów.",
                                        Toast.LENGTH_SHORT).show();
                                list = sendMessagesProcessingList;
                                adapter
                                        = new RemindersListAdapter(
                                        list, listener, getApplication());
                                recyclerView.setAdapter(adapter);
                            }
                        }
                    }
                }
            }
        };
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
                        //DatePicker datePicker = ((DatePicker)findViewById(R.id.datePicker));
                        //TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
                        //String date = ( (datePicker.getDayOfMonth() < 10)? "0" + String.valueOf(datePicker.getDayOfMonth()) : String.valueOf(datePicker.getDayOfMonth()))
                        //        + "." + ((datePicker.getMonth() + 1 < 10)? "0" + String.valueOf(datePicker.getMonth() + 1) : String.valueOf(datePicker.getMonth() + 1));
                        //String time = ( (timePicker.getHour() < 10)? "0" + String.valueOf(timePicker.getHour()) : String.valueOf(timePicker.getHour()))
                        //        + ":" + ((timePicker.getMinute()  < 10)? "0" + String.valueOf(timePicker.getMinute())  : String.valueOf(timePicker.getMinute()));
                        //reminder = reminder.replaceAll("<date>", date);
                        //reminder = reminder.replaceAll("<time>", time);
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
        }
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestFinish == true){
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    protected void sendSmsMessages(){
        if(sendMessagesProcessing == false){

            Toast.makeText(getBaseContext(), "Przygotowanie do wysłania...",
                    Toast.LENGTH_SHORT).show();
            if(sendSMSRegistered == true)
                unregisterReceiver(sendSMS);
            IntentFilter filter = new IntentFilter();

            sendMessagesProcessing = true;
            sendMessagesProcessingList = new ArrayList<>(list.size());
            sendMessagesProcessingInfoList = new ArrayList<>(list.size());
            // ---Notify when the SMS has been delivered---
            int SubscriptionId = SubscriptionManager.getDefaultSubscriptionId  ();

// less than 23 - code not applicable - keep for know how purpose
//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//              SmsManager  sms = SmsManager.getDefault();
//            }
// less than 31 - code not applicable - keep for know how purpose
//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S){
//              SmsManager  sms = SmsManager.getSmsManagerForSubscriptionId(SubscriptionId);
//            }
// greater or equals to 31 - code not applicable - keep for know how purpose
            SmsManager sms =  getApplicationContext().getSystemService(SmsManager.class) .createForSubscriptionId(SubscriptionId);

            for(int messageIndex = 0; messageIndex < list.size(); messageIndex++){
                String message = prepareMessage(this.list.get(messageIndex));
                int partsNumb =  sms.divideMessage(message).size();
                if(partsNumb == 1){
                    filter.addAction("sms"+messageIndex);
                }else{
                    for(int i = 0; i < partsNumb; i++){
                        filter.addAction("sms" + messageIndex + ":" + i);
                    }
                }
                sendMessagesProcessingInfoList.add(new RemindersListDataInfo(message, partsNumb));
            }
            sendSMSRegistered = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(sendSMS, filter, RECEIVER_EXPORTED);
            }else{
                registerReceiver(sendSMS, filter);
            }
            for(int messageIndex = 0; messageIndex < list.size(); messageIndex++){
                sendSMSMessage(list.get(messageIndex).customNumber, messageIndex, sms);
            }
        }else{
            Toast.makeText(getBaseContext(), "Już trwa wysyłanie.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    protected void sendSMSMessage(String phoneNumber, int id, SmsManager sms) {
        String message = sendMessagesProcessingInfoList.get(id).message;

        try {
            if(sendMessagesProcessingInfoList.get(id).numbOfParts > 1) {
                ArrayList<String> messageList = sms.divideMessage(message);
                ArrayList<PendingIntent> sentPI = new ArrayList<PendingIntent>();
                for(int i = 0 ; i < messageList.size(); i++){
                    Intent sendIntent = new Intent(SENT);
                    sendIntent.putExtra("id", id);
                    sendIntent.putExtra("partId", i);
                    sendIntent.setAction("sms"+id+":"+i);
                    PendingIntent tempSend = PendingIntent.getBroadcast(this, 0, sendIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                    sentPI.add(tempSend);
                }
                sms.sendMultipartTextMessage(phoneNumber, null, messageList, sentPI, null);
            } else {
                Intent sendIntent = new Intent(SENT);
                sendIntent.putExtra("id", id);
                sendIntent.setAction("sms"+id);
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sendIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "SMS nie wysłano.",
                    Toast.LENGTH_SHORT).show();
        }

    }

}