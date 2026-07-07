package com.example.reminder;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.SearchView;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LogActivity extends AppCompatActivity {


    LogAdapter adapter;
    RecyclerView recyclerView;
    List<LogData> list = new ArrayList<>();
    List<LogData> onlyFailedList = new ArrayList<>();
    ViewListener listener;
    private long PressedTime;
    private SearchView searchView = null;
    private final long Timeout = 1000; // Change it to any value you want
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_list);

        //Intent intent = getIntent();

        FileInputStream fin = null;
        try {
            fin = openFileInput(getResources().getString(R.string.log_file_name));
            list = readLogFile(fin);
            onlyFailedList.clear();
            for(int i = 0; i < list.size(); i++){
                if(list.get(i).success == false)
                    onlyFailedList.add(list.get(i));
            }
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
//        list.add(new LogData("Abc", 12, 7, 2024, 15, 45, 46,
//                "Part 1 OK\nPart 2 OK\n Part 3 NOK", "Wiadomość\nCos tam", false));
//        list.add(new LogData("Edf", 13, 8, 2024, 15, 45, 46,
//                "Part 1 OK\nPart 2 OK\n Part 3 NOK", "Wiadomość\nCos tam", true));
//        list.add(new LogData("Agf", 13, 8, 2024, 25, 45, 46,
//                "Part 1 OK\nPart 2 OK\n Part 3 NOK", "Wiadomość\nCos tam", false));
        onlyFailedList.clear();
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).success == false)
                onlyFailedList.add(list.get(i));
        }

        listener = new ViewListener() {
            @Override
            public void onClick(View view, final int position) {
                if (PressedTime + Timeout > System.currentTimeMillis()) return;
                PressedTime = System.currentTimeMillis();
                switch (view.getId()){
                    case R.id.callerView:
                    case R.id.partsInfoView:
                    case R.id.dateTimeView:
                    case R.id.messageContentView:
                    case R.id.failIcon:
                    {
                        list.get(position).expanded = !list.get(position).expanded;
                        adapter.notifyItemChanged(position);

                    }
                    break;
                }
            }
        };

        recyclerView
                = (RecyclerView)findViewById(
                R.id.logList);
        adapter
                = new LogAdapter(
                list, listener, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(LogActivity.this));
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
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
    public void cancelButtonClicked(View view){
        setResult(RESULT_CANCELED);
        finish();
    }
    @Override
    public void onBackPressed()
    {
        cancelButtonClicked(null);
    }
    public void showFailsOnlyOnClick(View view){
        CheckBox checkbox = (CheckBox) view;
        boolean checked = checkbox.isChecked();
        if(checked){
            adapter.list = onlyFailedList;
            adapter.listFull = new ArrayList<>( onlyFailedList);
        }else{
            adapter.list = list;
            adapter.listFull = new ArrayList<>( list);
        }
        adapter.notifyDataSetChanged();
    }

    public static  List<LogData> readLogFile(FileInputStream fin) throws IOException {
        try {
            int size = fin.available();
            byte[] buffer = new byte[size];
            int readBytes = fin.read(buffer);
            if(readBytes != size){
                throw new IOException("Wrong number of bytes. Expected bytes: " + size + " Read bytes:" + readBytes );
            }
            fin.close();
            fin = null;
            String content = new String(buffer, StandardCharsets.UTF_8);
            return readLogFileData(content);
        }finally{
            if(fin != null){
                fin.close();
            }
        }
    }
    private static  List<LogData> readLogFileData(String logFileData)throws IOException{
        List<LogData> logDataList = new ArrayList<LogData>();

        try {
            JSONArray logDataJsonArray = new JSONArray(logFileData);
            for(int i = 0; i < logDataJsonArray.length();i++){
                String receiver = "";
                int day = 0;
                int month = 0;
                int year = 0;
                int hour = 0;
                int minute = 0;
                int second = 0;
                String partsInfo = "";
                String message = "";
                boolean success;
                JSONObject logDataItemJsonObject = logDataJsonArray.getJSONObject(i);
                receiver = logDataItemJsonObject.getString("receiver");
                day = logDataItemJsonObject.getInt("day");
                month = logDataItemJsonObject.getInt("month");
                year = logDataItemJsonObject.getInt("year");
                hour = logDataItemJsonObject.getInt("hour");
                minute = logDataItemJsonObject.getInt("minute");
                second = logDataItemJsonObject.getInt("second");
                partsInfo = logDataItemJsonObject.getString("partsInfo");
                message = logDataItemJsonObject.getString("message");
                success = logDataItemJsonObject.getBoolean("success");
                logDataList.add(new LogData(receiver, day, month, year, hour, minute, second, partsInfo, message, success));

            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return logDataList;
    }
    static public void writeLogFile(FileOutputStream file, List<LogData> logDataList) throws Exception {
        JSONArray logDataListJson = new JSONArray();

        for(LogData logData : logDataList){
            JSONObject logDataJsonObject = new JSONObject();
            logDataJsonObject.put("receiver", logData.contact);
            logDataJsonObject.put("day", logData.dateTime.get(Calendar.DAY_OF_MONTH));
            logDataJsonObject.put("month", logData.dateTime.get(Calendar.MONTH));
            logDataJsonObject.put("year", logData.dateTime.get(Calendar.YEAR));
            logDataJsonObject.put("hour", logData.dateTime.get(Calendar.HOUR_OF_DAY));
            logDataJsonObject.put("minute", logData.dateTime.get(Calendar.MINUTE));
            logDataJsonObject.put("second", logData.dateTime.get(Calendar.SECOND));
            logDataJsonObject.put("partsInfo", logData.partsInfo);
            logDataJsonObject.put("message", logData.messageContent);
            logDataJsonObject.put("success", logData.success);
            logDataListJson.put(logDataJsonObject);
        }

        file.write(logDataListJson.toString().getBytes());
        file.close(); //File closed
    }


}