package com.example.reminder;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {
    int positionId = -1;
    String contact = new String();
    String contactNumber = new String();

    final String DEFAULT_CONTACT = "Kliknij, by wybrać kontakt";

    final int PICK_CONTACT = 41;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_reminder_activity);

        Intent intent = getIntent();
        positionId = intent.getIntExtra("id", -1);

        DatePicker datePicker = ((DatePicker)findViewById(R.id.datePicker));
        datePicker.findViewById(getResources().getIdentifier("year", "id", "android")).setVisibility(View.GONE);

        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        TextView contactView = (TextView) findViewById(R.id.messageView);

        EditText customPhoneNumber = (EditText) findViewById(R.id.editTextPhone);
        EditText finalCustomPhoneNumber = customPhoneNumber;
        customPhoneNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE){
                    //Clear focus here from edittext
                    finalCustomPhoneNumber.clearFocus();
                }
                return false;
            }
        });
        if(positionId != -1) {
            Bundle data = intent.getExtras();
            contact = data.getString("contact");
            String customNumber = data.getString("customNumber");
            Calendar date = (Calendar) data.get("date");
            int hour = data.getInt("hour");
            int minute = data.getInt("minute");

            if (contact.isEmpty()) {
                customPhoneNumber = (EditText) findViewById(R.id.editTextPhone);
                customPhoneNumber.setText(customNumber);
            } else {
                contactNumber = customNumber;
                contactView.setText(contact + " ("+ contactNumber + ")");
            }
            datePicker.updateDate(datePicker.getYear(), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }
        if(contactView.getText().toString().isEmpty()){
            contactView.setText(DEFAULT_CONTACT);
        }

    }
    public void acceptButtonClicked(View view){
        DatePicker datePicker = ((DatePicker)findViewById(R.id.datePicker));
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);

        TextView contactView = (TextView)findViewById(R.id.messageView);
        EditText customPhoneNumber = (EditText)findViewById(R.id.editTextPhone);
        if(!contactView.getText().toString().equals(DEFAULT_CONTACT) && !customPhoneNumber.getText().toString().isEmpty()){
            Toast.makeText(getBaseContext(), "Wybierz między kontaktem a numerem", Toast.LENGTH_LONG).show();
            return;
        }

        if(contactView.getText().toString().equals(DEFAULT_CONTACT) && customPhoneNumber.getText().toString().isEmpty()){
            Toast.makeText(getBaseContext(), "Wybierz kontakt lub wpisz numer", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        Bundle data = new Bundle();
        if(positionId != -1)
            data.putInt("id", positionId);
        data.putSerializable("date", new Calendar.Builder().setDate(datePicker.getYear(), datePicker.getMonth() - 1, datePicker.getDayOfMonth()).build());
        data.putInt("hour", timePicker.getHour());
        data.putInt("minute", timePicker.getMinute());

        if(contact.isEmpty()){
            data.putString("contact", "");
            data.putString("customNumber", customPhoneNumber.getText().toString());
        }else{
            data.putString("contact", contact);
            data.putString("customNumber", contactNumber);
        }
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
        finish();
    }
    public void cancelButtonClicked(View view){
        setResult(RESULT_CANCELED);
        finish();
    }
    @Override
    public void onBackPressed()
    {
        cancelButtonClicked(null);
    }

    public void contactViewOnClick(View view){
        EditText customPhoneNumber = (EditText)findViewById(R.id.editTextPhone);
        customPhoneNumber.clearFocus();
        Intent intent = new Intent(AddReminderActivity.this, ContactListActivity.class);
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void clearPhoneNumberOnClick(View view){
        EditText phoneTextEdit = (EditText) findViewById(R.id.editTextPhone);
        phoneTextEdit.setText("");
    }

    public void clearContactOnClick(View view){
        TextView contactView = (TextView) findViewById(R.id.messageView);
        contact = new String();
        contactNumber = new String();
        contactView.setText(DEFAULT_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case PICK_CONTACT :
                if (resultCode == RESULT_OK) {
                    contact = data.getStringExtra("contactName");
                    contactNumber = data.getStringExtra("phoneNumber");
                    TextView contactView = (TextView) findViewById(R.id.messageView);
                    contactView.setText(contact + " ("+ contactNumber + ")");
                }
                break;
        }
    }

}
