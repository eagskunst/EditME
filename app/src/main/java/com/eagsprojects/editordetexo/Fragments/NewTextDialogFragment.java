package com.eagsprojects.editordetexo.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
//import android.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.eagsprojects.editordetexo.NewTextActivity;
import com.eagsprojects.editordetexo.R;


public class NewTextDialogFragment extends DialogFragment {

    String title_new;

    public NewTextDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.newtextdialog_layout,null);
        final EditText editText = view.findViewById(R.id.edittext_dialoglayout);


        builder.setView(view)//Ojo, aqui volví a inflar el layout y por eso el string siempre venía vacío.
        .setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                title_new = editText.getText().toString();
                if(title_new.isEmpty()) title_new = getString(R.string.newtext);
                Intent intent = new Intent(getActivity(),NewTextActivity.class);
                intent.putExtra("title",title_new);
                dialogInterface.dismiss();
                startActivityForResult(intent,1);
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }

}
