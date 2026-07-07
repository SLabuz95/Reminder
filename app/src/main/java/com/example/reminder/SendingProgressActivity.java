package com.example.reminder;


import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.function.Predicate;

public class SendingProgressActivity extends AppCompatActivity {
    Button cancelButton = null;
    boolean cancelRequested = false;
    SendingProgressListAdapter adapter;
    RecyclerView recyclerView;
    final String SENT = "SMS_SENT";
    BroadcastReceiver sendSMS = null;
    boolean sendSMSRegistered = false;
    List<LogData> fileLogDataList = null;
    List<LogData> logDataList = null;
    List<SendingProgressListData> list = null;
    boolean[] listToUpdate = null;
    enum SaveStatus{
        NotReady,
        ReadyToSave,
        Saved
    }
    SaveStatus[] listToSave = null;
    final int CheckTimeout = 100;
    final int UpdatedTimeout = 10000 / CheckTimeout; // Save after that time if no updates occcured
    int UpdatedCounter = 0;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable checkIfFinished = new Runnable() {
        @Override
        public void run() {
            if(cancelRequested){
                setResult(RESULT_OK);
                finish();
            }
            // Clone update array
            boolean[] updateList = listToUpdate.clone();
            Arrays.fill(listToUpdate, false);
            boolean updated = false;
            // View update
            for(int id = 0; id < list.size(); id++){
                if(updateList[id] == true){
                    updated = true;
                    adapter.notifyItemChanged(id);
                }
            }
            // Decide if ready to finish
            boolean finish = true;
            boolean anyFailed = false;
            for(int id = 0; id < list.size(); id++){
                if(list.get(id).failed == true){
                    anyFailed = true;
                }
                if(listToSave[id] == SaveStatus.NotReady){
                    finish = false;
                    break;
                }
            }

            if(updated == true){
                UpdatedCounter = 0;
                cancelButton.setVisibility(GONE);
            }else{
                if(UpdatedCounter < UpdatedTimeout){
                    UpdatedCounter++;
                }else{
                    UpdatedCounter = 0;
                    ArrayList<LogData> logsToSave = new ArrayList<>();
                    for(int id  =0; id< logDataList.size(); id++){
                        if(listToSave[id] == SaveStatus.ReadyToSave){
                            logDataList.get(id).dateTime = Calendar.getInstance();
                            listToSave[id] = SaveStatus.Saved;
                            logsToSave.add(logDataList.get(id));
                        }
                    }
                    if(logsToSave.size() > 0) {
                        if(fileLogDataList != null)
                            logsToSave.addAll(fileLogDataList);
                        try {
                            writeLogs(logsToSave);
                            Toast.makeText(getBaseContext(), "Zapisano logi diagnostyczne.",
                                    Toast.LENGTH_SHORT).show();
                            fileLogDataList = logsToSave;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    cancelButton.setVisibility(VISIBLE);
                }
            }


            if(finish == true){
                ArrayList<LogData> logsToSave = new ArrayList<>();
                for(int id  =0; id< logDataList.size(); id++){
                    if(listToSave[id] == SaveStatus.ReadyToSave){
                        logDataList.get(id).dateTime = Calendar.getInstance();
                        listToSave[id] = SaveStatus.Saved;
                        logsToSave.add(logDataList.get(id));
                    }
                }
                if(logsToSave.size() > 0) {
                    if(fileLogDataList != null)
                        logsToSave.addAll(fileLogDataList);
                    try {
                        writeLogs(logsToSave);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // If processing finished
                if (anyFailed == false) { // No errors
                    setResult(RESULT_OK);
                } else {
                    setResult(1);
                }
                finish();
            }else{
                if(cancelRequested){
                    setResult(RESULT_OK);
                    finish();
                }else {
                    handler.postDelayed(this, CheckTimeout);
                }
            }

            if(cancelRequested){
                setResult(RESULT_OK);
                finish();
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sending_progress_activity);
        cancelButton = (Button)findViewById(R.id.sending_progress_cancel_button);
        cancelButton.setVisibility(GONE);
        FileInputStream fin = null;
        try {
            fin = openFileInput(getResources().getString(R.string.log_file_name));
            fileLogDataList = LogActivity.readLogFile(fin);

        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fin != null) {
                try {
                    fin.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        // Remove all older than month
        Calendar filterTime = Calendar.getInstance();
        filterTime.add(Calendar.MONTH, -1);
        if(fileLogDataList != null) {
            fileLogDataList.removeIf(new Predicate<LogData>() {
                @Override
                public boolean test(LogData logData) {
                    return logData.dateTime.before(filterTime);
                }
            });
        }

        Intent intent = getIntent();
        logDataList = (ArrayList<LogData>)intent.getSerializableExtra("logList");
        list = new ArrayList<>(logDataList.size());
        for(int i = 0; i < logDataList.size(); i++){
            list.add(new SendingProgressListData(logDataList.get(i).contact));
        }
        listToUpdate = new boolean[list.size()];
        Arrays.fill(listToUpdate, false);

        listToSave = new SaveStatus[list.size()];
        Arrays.fill(listToSave, SaveStatus.NotReady);

        recyclerView
                = (RecyclerView)findViewById(
                R.id.sending_progress_reminders_list);
        adapter
                = new SendingProgressListAdapter(
                list, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(SendingProgressActivity.this));
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());


        sendSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                int id = arg1.getIntExtra("id", -1);
                int partId = arg1.getIntExtra("partId", -1);
                if (id != -1) {
                    LogData logData = logDataList.get(id);
                    SendingProgressListData sendingProgressListData = list.get(id);
                    switch (getResultCode()) {
                        case Activity.RESULT_OK:
                            logData.partsInfo += "Part " +
                                    (partId + 1) + "/" + sendingProgressListData.messageParts.size() + " OK\n";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                        default:
                        {
                            String ExtraError = arg1.getStringExtra("errorCode");
                            logData.partsInfo += "Part " +
                                    (partId + 1) + "/" + sendingProgressListData.messageParts.size() + " NOK "
                                    + getResultCode() + " " + ExtraError + "\n";
                            logData.success = false;
                            sendingProgressListData.failed = true;
                            if(listToSave[id] == SaveStatus.NotReady)
                                listToSave[id] = SaveStatus.ReadyToSave;
                        }
                        break;
                    }
                    sendingProgressListData.status++;
                    if(sendingProgressListData.status >= sendingProgressListData.messageParts.size() &&
                        listToSave[id] == SaveStatus.NotReady){
                        listToSave[id] = SaveStatus.ReadyToSave;
                    }
                    listToUpdate[id] = true;
                }
            }
        };


        sendSmsMessages();
    }

    @Override
    protected void onDestroy(){
        if(sendSMSRegistered == true)
            unregisterReceiver(sendSMS);
        sendSMSRegistered = false;
        super.onDestroy();
    }
    public void cancelButtonClicked(View view){
        cancelRequested = true;
    }
    @Override
    public void onBackPressed()
    {
        // Ignore
    }

    protected void sendSmsMessages(){

        if(sendSMSRegistered == true)
            unregisterReceiver(sendSMS);

        IntentFilter filter = new IntentFilter();

        //sendMessagesProcessingLogList = new ArrayList<>(list.size());

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
            LogData logData = this.logDataList.get(messageIndex);
            SendingProgressListData sendingProgressData = this.list.get(messageIndex);
            sendingProgressData.messageParts =  sms.divideMessage(logData.messageContent);
            int numberOfParts = sendingProgressData.messageParts.size();
            adapter.notifyItemChanged(messageIndex);

            if(numberOfParts == 1){
                filter.addAction("sms"+messageIndex);
            }else{
                for(int i = 0; i < numberOfParts; i++){
                    filter.addAction("sms" + messageIndex + ":" + i);
                }
            }
        }
        sendSMSRegistered = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sendSMS, filter, RECEIVER_EXPORTED);
        }else{
            registerReceiver(sendSMS, filter);
        }
        for(int messageIndex = 0; messageIndex < list.size(); messageIndex++){
            sendSMSMessage(messageIndex, sms);
        }
        handler.postDelayed(checkIfFinished, CheckTimeout);
    }

    protected void sendSMSMessage(int id, SmsManager sms) {
        LogData logData = this.logDataList.get(id);
        SendingProgressListData progressListData = this.list.get(id);
        String phoneNumber = logData.contactNumber;
        String message = logData.messageContent;
        try {
            if(progressListData.messageParts.size() > 1) {
                ArrayList<String> messageList = progressListData.messageParts;
                ArrayList<PendingIntent> sentPI = new ArrayList<PendingIntent>();
                for(int i = 0 ; i < messageList.size(); i++){
                    Intent sendIntent = new Intent(SENT);
                    sendIntent.putExtra("id", id);
                    sendIntent.putExtra("partId", i);
                    sendIntent.setAction("sms"+id+":"+i);
                    PendingIntent tempSend = PendingIntent.getBroadcast(this, 0, sendIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                    sentPI.add(tempSend);
                }
                Log.d("TEST", "TEST");
                sms.sendMultipartTextMessage(phoneNumber, null, messageList, sentPI, null);
            } else {
                Intent sendIntent = new Intent(SENT);
                sendIntent.putExtra("id", id);
                sendIntent.setAction("sms"+id);
                PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sendIntent, FLAG_UPDATE_CURRENT | FLAG_IMMUTABLE);
                sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
            }
        } catch (Exception e) {
            logData.success = false;
            logData.partsInfo = "Błąd - nie wysłano przez aplikację";
            progressListData.failed =  true;
            listToUpdate[id] = true;
            listToSave[id] = SaveStatus.ReadyToSave;
        }

    }
    void writeLogs(ArrayList<LogData> logDataList) throws Exception{
        FileOutputStream file = null;
        try {
            file = openFileOutput( getResources().getString(R.string.log_file_name), Context.MODE_PRIVATE);
            LogActivity.writeLogFile(file, logDataList);
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if(file != null)
                    file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}