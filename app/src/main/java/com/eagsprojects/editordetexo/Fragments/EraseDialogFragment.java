package com.eagsprojects.editordetexo.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.app.Fragment;
//import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.eagsprojects.editordetexo.MainActivity;
import com.eagsprojects.editordetexo.R;

import java.io.File;
import java.nio.file.Files;

/**
 * Created by Emmanuel on 15/3/2018.
 */

public class EraseDialogFragment extends DialogFragment{

    private static final String TAG = "EraseFragment";
    private File root;
    private String title,directory;

    public EraseDialogFragment(){

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        if(isExternalStorageWritable()){
            alertDialog.setMessage(R.string.erase);
            title = this.getArguments().getString("title");

            alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    File file = new File(root,title);
                    if(file.exists()){
                        Log.w(TAG,"Entré!! "+file.toString());
                        file.delete();
                    }
                    else{
                        Log.w(TAG,"No entré!! "+file.toString());
                    }
                    Intent refresh = new Intent(getActivity(), MainActivity.class);
                    dialogInterface.dismiss();
                    startActivityForResult(refresh,1);
                }
            });

            alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }

        else{
            alertDialog.setMessage(R.string.externalavailable);
            alertDialog.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
        }


        return alertDialog.create();
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            String r = Environment.getExternalStorageDirectory().toString();
            root = new File(r +"/Texts");
            return true;
        }
        else{
            return false;
        }
    }
}
