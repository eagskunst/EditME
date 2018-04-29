package com.eagsprojects.editordetexo.Fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eagsprojects.editordetexo.R;
import com.ms.square.android.expandabletextview.ExpandableTextView;


public class HelpFragment extends Fragment {

    private ExpandableTextView expandableTextView1,expandableTextView2,expandableTextView3;


    public HelpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.help);
        ((NavigationView) getActivity().findViewById(R.id.navigationview_1)).setCheckedItem(R.id.help_item);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help,container,false);

        TextView title1 = view.findViewById(R.id.expandabletext1).findViewById(R.id.expandabletext_title);
        TextView title2 = view.findViewById(R.id.expandabletext2).findViewById(R.id.expandabletext_title);
        TextView title3 = view.findViewById(R.id.expandabletext3).findViewById(R.id.expandabletext_title);

        title1.setText(getResources().getString(R.string.how_to_erase_text));
        title2.setText(getResources().getString(R.string.where_is_the_root));
        title3.setText(getResources().getString(R.string.sync_with_google_drive));

        expandableTextView1 = view.findViewById(R.id.expandabletext1).findViewById(R.id.expand_text_view);
        expandableTextView2 = view.findViewById(R.id.expandabletext2).findViewById(R.id.expand_text_view);
        expandableTextView3 = view.findViewById(R.id.expandabletext3).findViewById(R.id.expand_text_view);

        expandableTextView1.setText(getResources().getString(R.string.erasing_a_text));
        expandableTextView2.setText(getResources().getString(R.string.where_to_find));
        expandableTextView3.setText(getResources().getString(R.string.googledrive_sync));


        return view;
    }
}
