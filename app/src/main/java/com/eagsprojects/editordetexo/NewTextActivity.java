package com.eagsprojects.editordetexo;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class NewTextActivity extends AppCompatActivity {

    public String filename,text = new String(),completeText;
    public Button button;
    public File root, myDir,file;
    public boolean overwrite = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String title = getIntent().getExtras().getString("title");
        filename = title;
        setContentView(R.layout.activity_new_text);
        EditText editText = findViewById(R.id.edittext);
        if(getIntent().getExtras().getString("text") != null){
            completeText = getIntent().getExtras().getString("text");
            editText.setText(completeText, TextView.BufferType.EDITABLE);
            overwrite = true;
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
                        text = editText.getText().toString();
                        System.out.println(editText.getText().toString());
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
                setResult(RESULT_OK, null);
                startActivity(intent);
                finish();
            }
        });
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String root = Environment.getExternalStorageDirectory().toString();
            myDir = new File(root +"/Texts");
            return true;
        }
        return false;
    }


}
