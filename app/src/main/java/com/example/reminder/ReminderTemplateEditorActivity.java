package com.example.reminder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;


public class ReminderTemplateEditorActivity extends AppCompatActivity {
    int templateId = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminder_template_editor_activity);
        Intent intent = getIntent();

        templateId = intent.getIntExtra("templateId", -1);

        if(templateId == -1) {
            // Nothing will be changed
        }else{
            String templateText = intent.getStringExtra("templateContent");
            TextView titleTextView = (TextView)findViewById(R.id.new_reminder_activity_title);
            titleTextView.setText("Edytuj szablon");
            MultiAutoCompleteTextView editor = (MultiAutoCompleteTextView)findViewById(R.id.templateEditor);
            editor.setText(templateText);
       }
    }

    @Override
    public void onBackPressed()
    {
        cancelButtonClicked(null);
    }

    public void dateMarkerButtonClicked(View view){
        MultiAutoCompleteTextView editor = (MultiAutoCompleteTextView)findViewById(R.id.templateEditor);
        if(editor.hasFocus())
        {
            String text = editor.getText().toString();
            int index = editor.getSelectionStart();
            if(index == 0){
                boolean white = text.length() == 0 || Character.isWhitespace(text.charAt(0));
                text = "<date>" + ((white)? "" : " ") + text;
                index += 6 + ((white)? 0 : 1);
            }else{
                if(index == text.length()){
                    boolean white = Character.isWhitespace(text.charAt(text.length() - 1));
                    text = text + ((white)? "" : " ") + "<date> ";
                    index += 6 + ((white)? 0 : 1);
                }else{
                    boolean white1 = Character.isWhitespace(index - 1),
                            white2 = Character.isWhitespace(index);
                    text = text.substring(0, index) +
                            ((white1)? "" : " ") + "<date>" +
                            ((white2)? "" : " ") + text.substring(index);
                    index += 6 + ((white1)? 0 : 1) + ((white2)? 0 : 1);
                }
            }
            editor.setText(text);
            editor.setSelection(index, index);
        }
    }

    public void timeMarkerButtonClicked(View view){
        MultiAutoCompleteTextView editor = (MultiAutoCompleteTextView)findViewById(R.id.templateEditor);
        if(editor.hasFocus())
        {
            String text = editor.getText().toString();
            int index = editor.getSelectionStart();
            if(index == 0){
                boolean white = text.length() == 0 || Character.isWhitespace(text.charAt(0));
                text = "<time>" + ((white)? "" : " ") + text;
                index += 6 + ((white)? 0 : 1);
            }else{
                if(index == text.length()){
                    boolean white = Character.isWhitespace(text.charAt(text.length() - 1));
                    text = text + ((white)? "" : " ") + "<time> ";
                    index += 6 + ((white)? 0 : 1);
                }else{
                    boolean white1 = Character.isWhitespace(index - 1),
                            white2 = Character.isWhitespace(index);
                    text = text.substring(0, index) +
                            ((white1)? "" : " ") + "<time>" +
                            ((white2)? "" : " ") + text.substring(index);
                    index += 6 + ((white1)? 0 : 1) + ((white2)? 0 : 1);
                }
            }
            editor.setText(text);
            editor.setSelection(index, index);
        }
    }

    public void acceptButtonClicked(View view){
        MultiAutoCompleteTextView editor = (MultiAutoCompleteTextView)findViewById(R.id.templateEditor);
        Intent intent = new Intent();
        intent.putExtra("templateId", templateId);
        intent.putExtra("template", editor.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelButtonClicked(View view){
        setResult(RESULT_CANCELED);
        finish();
    }
}