package com.example.reminder;
import java.io.Serializable;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class LogData implements Serializable {
    String contact;
    String contactNumber;
    Calendar dateTime;
    String partsInfo;
    String messageContent;
    boolean success = true;
    boolean expanded = false;
    LogData(String contact, String customNumber, String messageContent)
    {
        Calendar localDateTime = Calendar.getInstance();
        localDateTime.set(2000, 0, 1, 0, 0, 0);
        this.contact = contact;
        this.contactNumber = customNumber;
        this.dateTime = localDateTime;
        this.partsInfo = "";
        this.messageContent = messageContent;
    }
    LogData(String contact, int day, int month, int year, int hour, int minute, int second, String partsInfo, String messageContent, boolean status)
    {
        Calendar localDateTime = Calendar.getInstance();
        localDateTime.set(year, month, day, hour, minute, second);
        this.contact = contact;
        this.dateTime = localDateTime;
        this.partsInfo = partsInfo;
        this.messageContent = messageContent;
        this.success = status;
    }
}
