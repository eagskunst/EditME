package com.eagsprojects.editordetexo.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
//import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.eagsprojects.editordetexo.R;

import java.io.File;

/**
 * Created by Emmanuel on 15/3/2018.
 * Using DialogFragments for the first time here!
 */

public class EraseDialogFragment extends DialogFragment{

    /*
    Interface that would help us to make a better approach for refreshing the recycler view
     */

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    private static final String TAG = "EraseFragment";
    private static final String ARCHIVES_TAG = "ArchivesFragment";
    private File root;
    private String title;
    private int position;

    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;


    public EraseDialogFragment(){

    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        //Instancing this fragment

        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(ARCHIVES_TAG);

        try{
            // Try/catch because the fragment may not be initialized
            mListener = (NoticeDialogListener) fragment;
        }catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(fragment.toString()
                    + " must implement NoticeDialogListener");
        }



        if(isExternalStorageWritable()){
            alertDialog.setMessage(R.string.erase);
            title = this.getArguments().getString("title");//Getting the title from the Bundle.
            position = this.getArguments().getInt("position");

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
                    if(mListener == null){
                        Log.i(TAG,"Es null...");
                    }
                    mListener.onDialogPositiveClick(EraseDialogFragment.this);
                }
            });

            alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onDialogNegativeClick(EraseDialogFragment.this);
                    if(mListener == null){
                        Log.i(TAG,"Es null...");
                    }
                }
            });
        }

        else{
            alertDialog.setMessage(R.string.externalavailable);
            alertDialog.setNegativeButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mListener.onDialogNegativeClick(EraseDialogFragment.this);
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

    public void setmListener(NoticeDialogListener mListener){
        this.mListener = mListener;
    }


}
