package com.example.reminder;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Calendar;

public class RemindersListData {
    String contact;
    String customNumber;
    Calendar date;
    int hour;
    int minute;
    String messageTemplate;

    public String getContact(){return contact.isEmpty()? customNumber : contact;}
    //boolean checkBox = false;
    RemindersListData(String contact, String customNumber, Calendar date, int hour, int minute, String messageTemplate)
    {
        this.contact = contact;
        this.customNumber = customNumber;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
        this.messageTemplate = messageTemplate;
    }
}
