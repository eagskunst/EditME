package com.eagsprojects.editordetexo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.DialogFragment;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.eagsprojects.editordetexo.Adapter.TextsAdapter;
import com.eagsprojects.editordetexo.Model.TextModel;
import com.eagsprojects.editordetexo.NewTextActivity;
import com.eagsprojects.editordetexo.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class ArchivesFragment extends Fragment {

    private static final String TAG = "ArchivesFragment";
    public static final String KEY_CARD_COLOR = "pref_cardcolor";

    private SharedPreferences preferences;
    private File root;
    private static int cvColor;
    private RecyclerView rv;
    private CardView cardView;
    private List<TextModel> textList = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>(),subtexts = new ArrayList<>(),completeText = new ArrayList<>();


    public ArchivesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_archives, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        TextView textView = view.findViewById(R.id.textview_main);
        cardView = view.findViewById(R.id.cardview);
        if(cardView == null){
            Log.e(TAG,"CardView is null");
        }
        rv = view.findViewById(R.id.recyclerview);
        textView.setVisibility(View.GONE);
        rv.setVisibility(View.INVISIBLE);

        root = new File(Environment.getExternalStorageDirectory(),"Texts");
        if(!root.exists()){
            root.mkdir();
        }

        String s[] = root.list();
        setCardViewColor(preferences);

        setTitlesAndSubtexts(s,titles,subtexts,completeText);
        startRecyclerView(s,titles,subtexts,completeText);

        if(s.length == 0){
            textView.setVisibility(View.VISIBLE);
        }
        else{
            textView.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void setTitlesAndSubtexts(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        for (int i = 0;i<s.length;i++){
            titles.add(s[i]);
            File file = new File(root,s[i]);
            StringBuilder texts = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line,subtext;
                line = br.readLine();
                if(line.length()>155){
                    subtext = line.substring(0,155);
                    subtexts.add(subtext+"...");
                    texts.append(line);
                    texts.append('\n');
                }
                else{
                    subtexts.add(line);
                    texts.append(line);
                }
                while ((line = br.readLine()) != null) {
                    texts.append(line);
                    texts.append('\n');
                }
                completeText.add(texts.toString());
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private void startRecyclerView(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        if(rv != null){
            rv.setHasFixedSize(true);
        }

        for(int i = 0;i < s.length; i++){
            TextModel textModel = new TextModel(titles.get(i),subtexts.get(i), completeText.get(i));
            textList.add(textModel);
        }

        TextsAdapter textsAdapter = new TextsAdapter(textList,cvColor,
                new TextsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(TextModel item) {
                        Intent intent = new Intent(getActivity(), NewTextActivity.class);
                        intent.putExtra("title", item.getTitle());
                        intent.putExtra("text", item.getText());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    /*Explode explode = new Explode();
                    explode.setDuration(1000);
                    getWindow().setExitTransition(explode);*/
                            startActivity(intent);
                        } else {
                            startActivity(intent);
                        }
                    }
                },
                new TextsAdapter.OnLongItemClickListener() {
                    @Override
                    public void onItemLongClick(TextModel item) {
                        Bundle bundle = new Bundle();

                        EraseDialogFragment eraseFragment = new EraseDialogFragment();

                        bundle.putString("title",item.getTitle());
                        eraseFragment.setArguments(bundle);
                        eraseFragment.show(getFragmentManager(),TAG);
                        Toast.makeText(getActivity(),"You're gonna carry that weight", Toast.LENGTH_SHORT).show();
                    }
                });

        rv.setLayoutManager(layoutManager);
        rv.setAdapter(textsAdapter);
        textsAdapter.notifyDataSetChanged();
    }


    public void showToolbar(Toolbar toolbar, boolean upButton,int showButton,View view){
        Button button = view.findViewById(R.id.button_toolbar);
        button.setVisibility(showButton);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }

    private void setCardViewColor(SharedPreferences preferences) {
        String color = preferences.getString(KEY_CARD_COLOR,getString(R.string.pref_fonttype_default));
        Log.d(TAG,"Color: "+color);

        switch (color) {
            case "1":
                cvColor = Color.WHITE;
                break;
            case "2":
                cvColor = Color.BLUE;
                break;
            case "3":
                cvColor = Color.YELLOW;
                break;
            case "4":
                cvColor = Color.GREEN;
                break;
        }
    }


}
