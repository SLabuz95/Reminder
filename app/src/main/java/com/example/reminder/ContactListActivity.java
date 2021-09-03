package com.example.reminder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ContactListActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    ContactListAdapter adapter;
    RecyclerView recyclerView;
    List<ContactListData> list = new ArrayList<>();
    ViewListener listener;
    int selected = 0;
    int lastSendIntent = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list_activity);

        Intent intent = getIntent();
        String reminder = intent.getStringExtra("reminder");

        TextView messageToSendView = (TextView)findViewById(R.id.message_to_send_textview);
        messageToSendView.setText(reminder);

        //TextView selectedTextView = (TextView)findViewById(R.id.selected_contacts_textview);
        //selectedTextView.setText("Wybrane: " + String.valueOf(selected));

        if (ContextCompat.checkSelfPermission(ContactListActivity.this,
                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ContactListActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1);
        }
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
        readContacts();

        listener = new ViewListener() {
            @Override
            public void onClick(View view, final int position) {
                switch (view.getId()){
                    case R.id.contact_label:
                    {
                        /* Procedure to react on check box select action
                        CheckBox checkBox = (CheckBox)view;
                        list.get(position).checkBox = checkBox.isChecked();
                        if(checkBox.isChecked())
                            selected++;
                        else
                            selected--;
                        TextView selectedTextView = (TextView)findViewById(R.id.selected_contacts_textview);
                        selectedTextView.setText("Wybrane: " + String.valueOf(selected));*/
                    }
                    break;
                    case R.id.sendMessageButton:
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        sendSMSMessage(list.get(position).phoneNumber);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
                        String nameorphone;
                        if(list.get(position).contactName.isEmpty()){
                            nameorphone = list.get(position).phoneNumber;
                        }else{
                            nameorphone = list.get(position).contactName;
                        }
                        builder.setMessage("Czy chcesz wysłać wiadomość do \"" + nameorphone + "\"?" ).setPositiveButton("Tak", dialogClickListener)
                                .setNegativeButton("Nie", dialogClickListener).show();
                    }
                    break;

                }
            }
        };
        recyclerView
                = (RecyclerView)findViewById(
                R.id.contact_list_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter
                = new ContactListAdapter(
                list, listener, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    public void sendMessageByNumberClicked(View view){
        EditText editText = (EditText)findViewById(R.id.editTextPhone);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        EditText editText = (EditText)findViewById(R.id.editTextPhone);
                        sendSMSMessage(editText.getText().toString());
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactListActivity.this);
        builder.setMessage("Czy chcesz wysłać wiadomość do \"" + editText.getText() + "\"?" ).setPositiveButton("Tak", dialogClickListener)
                .setNegativeButton("Nie", dialogClickListener).show();
    }

    public void sendToSelectedClicked(View view){
        Toast.makeText(ContactListActivity.this, "Tymczasowo wyłączone.", Toast.LENGTH_LONG).show();
    }

    protected void sendSMSMessage(String phoneNumber) {

        TextView messageView = (TextView)findViewById(R.id.message_to_send_textview);
        // Intent Filter Tags for SMS SEND and DELIVER
        String SENT = "SMS_SENT";
// STEP-1___

// STEP-2___
        // SEND BroadcastReceiver
        BroadcastReceiver sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {

                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            if(lastSendIntent == -1) {
                                lastSendIntent = 0;
                                Toast.makeText(getBaseContext(), "SMS wysłano.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            if(lastSendIntent == -1) {
                                lastSendIntent = 0;
                                Toast.makeText(getBaseContext(), "SMS nie wysłano.",
                                        Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
            }
        };


// STEP-3___
        // ---Notify when the SMS has been sent---
        registerReceiver(sendSMS, new IntentFilter(SENT));

        // ---Notify when the SMS has been delivered---
        int MAX_SMS_MESSAGE_LENGTH = 70;
        SmsManager sms = SmsManager.getDefault();

        try {
            if(messageView.getText().length() > MAX_SMS_MESSAGE_LENGTH) {
                ArrayList<String> messageList = SmsManager.getDefault().divideMessage(messageView.getText().toString());
                ArrayList<PendingIntent> sentPI = new ArrayList<PendingIntent>();
                for(int i = 0 ; i < messageList.size(); i++){
                    Intent sendIntent = new Intent(SENT);
                    PendingIntent tempSend = PendingIntent.getBroadcast(this, 0, sendIntent, 0);
                    sentPI.add(tempSend);
                }
                lastSendIntent = -1;
                 sms.sendMultipartTextMessage(phoneNumber, null, messageList, sentPI, null);
            } else {
                Intent sendIntent = new Intent(SENT);
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sendIntent, 0);
                lastSendIntent = -1;
                sms.sendTextMessage(phoneNumber, null, messageView.getText().toString(), sentPI, null);
            }
        } catch (Exception e) {
            Toast.makeText(getBaseContext(), "SMS nie wysłano.",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    private void readContacts(){

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            int i = 0;
            boolean add = true;
            for(i = 0; i < list.size(); i++) {
                if(phoneNumber.equals(list.get(i).phoneNumber)){
                    add = false;
                    break;
                }
            }
            if (add)
                list.add(new ContactListData(name, phoneNumber));
        }

        phones.close();
        }
}




