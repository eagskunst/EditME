package com.eagsprojects.editordetexo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class NewTextActivity extends AppCompatActivity {

    private static final String KEY_FONT_TYPES = "pref_fonttype";
    private static final String KEY_FONT_SIZE = "pref_fontsize";
    private static final String TAG = "NewTextActivity";



    private String filename,text,completeText;
    private Button button;
    private File myDir,file;
    private boolean overwrite = false;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_text);
        Typeface font = Typeface.createFromAsset(getAssets(),"font/future.ttf");

        //Getting the title from the NewTextDialongFragment or the ArchivesFragment
        String title = getIntent().getExtras().getString("title");
        filename = title;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        editText = findViewById(R.id.edittext);
        changeTypeface(editText,preferences,font);//check the typeface from the preferences, then change it
        changeTextSize(editText,preferences);//same but for text size

        if(getIntent().getExtras().getString("text") != null){//checking if we are starting with a new text or one we have created
            completeText = getIntent().getExtras().getString("text");
            editText.setText(completeText, TextView.BufferType.EDITABLE);
            overwrite = true;//If we are going to edit previous text, then overwrite it
        }
        showToolbar(title,true, View.VISIBLE,editText);
    }


    public void showToolbar(String title, boolean upButton, int showButton, final EditText editText){
        button = findViewById(R.id.button_toolbar);
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        button.setVisibility(showButton);
        button.setText(R.string.save);

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isExternalStorageWritable()){
                    if(!overwrite){
                        file = new File(myDir,filename + ".txt");
                    }
                    else{
                        file = new File(myDir,filename);
                    }
                    try {
                        Writer output = new BufferedWriter(new FileWriter(file));
                        text = editText.getText().toString();//getting the text
                        if(text.isEmpty()){
                            text = " ";
                        }
                        output.write(text);
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else{
                    Toast.makeText(view.getContext(),R.string.externalavailable,Toast.LENGTH_LONG).show();
                }

                Intent intent = new Intent(NewTextActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }


    /* Checks if external storage is available for read and write
    * copied this method from Android Documentation
    * */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String root = Environment.getExternalStorageDirectory().toString();//Getting the main directory of the internal storage
            myDir = new File(root +"/Texts");//setting myDir to point to Texts folder
            return true;
        }
        else{
            return false;
        }
    }

    private void changeTypeface(EditText editText, SharedPreferences sharedPreferences, Typeface font) {
        String typeface = sharedPreferences.getString(KEY_FONT_TYPES,getString(R.string.pref_fonttype_default));
        Log.d(TAG,"Typeface: "+typeface);

        switch (typeface) {
            case "1":
                editText.setTypeface(Typeface.DEFAULT);
                Log.d(TAG, "Typeface: " + editText.getTypeface());
                break;
            case "2":
                editText.setTypeface(font);
                Log.d(TAG, "Typeface: " + editText.getTypeface());
                break;
            case "3":
                editText.setTypeface(Typeface.SERIF);
                Log.d(TAG, "Typeface: " + editText.getTypeface());
                break;
            case "4":
                editText.setTypeface(Typeface.MONOSPACE);
                Log.d(TAG, "Typeface: " + editText.getTypeface());
                break;
            default:
                Log.d(TAG, "NONE. Typeface: " + editText.getTypeface());
                break;
        }
    }

    private void changeTextSize(EditText editText, SharedPreferences preferences) {
        String textsize = preferences.getString(KEY_FONT_SIZE,getString(R.string.pref_fontsize_default));
        Log.d(TAG,"Text size: "+textsize);

        switch (textsize) {
            case "1":
                editText.setTextSize(14f);
                Log.d(TAG, "Text size: " + editText.getTextSize());
                break;
            case "2":
                editText.setTextSize(18f);
                Log.d(TAG, "Text size: " + editText.getTextSize());
                break;
            case "3":
                editText.setTextSize(22f);
                Log.d(TAG, "Text size: " + editText.getTextSize());
                break;
            default:
                Log.d(TAG, "NONE. TEXT SIZE: " + editText.getTextSize());
                break;
        }
    }

}
