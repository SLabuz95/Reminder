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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
    private long PressedTime;
    private SearchView searchView = null;
    private final long Timeout = 1000; // Change it to any value you want
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_list_activity);

        readContacts();

        listener = new ViewListener() {
            @Override
            public void onClick(View view, final int position) {
                if (PressedTime + Timeout > System.currentTimeMillis()) return;
                PressedTime = System.currentTimeMillis();

                if (view.getId() == R.id.reminderContactLabel) {
                    Intent intent = new Intent();
                    intent.putExtra("contactName", list.get(position).contactName);
                    intent.putExtra("phoneNumber", list.get(position).phoneNumber);
                    setResult(RESULT_OK, intent);
                    finish();
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
        searchView = (SearchView) searchItem.getActionView();
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

    @Override
    public void onBackPressed()
    {
        if (PressedTime + Timeout > System.currentTimeMillis()) return;
        PressedTime = System.currentTimeMillis();

        setResult(RESULT_CANCELED);
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




