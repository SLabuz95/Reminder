package com.example.reminder;

import java.util.ArrayList;

public class SendingProgressListData {
    String contact;
    ArrayList<String> messageParts = null;
    int status = 0;
    //int numberOfParts = 0;
    boolean failed = false;
    SendingProgressListData(String contact)
    {
        this.contact = contact;
    }
}
