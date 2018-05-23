package com.eagsprojects.editordetexo.Fragments;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.eagsprojects.editordetexo.Adapter.TextsAdapter;
import com.eagsprojects.editordetexo.Handlers.TextFilesHandler;
import com.eagsprojects.editordetexo.Model.TextModel;
import com.eagsprojects.editordetexo.NewTextActivity;
import com.eagsprojects.editordetexo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ArchivesFragment extends Fragment implements EraseDialogFragment.NoticeDialogListener{

    private static final String TAG = "ArchivesFragment";
    private static final String DIALOG_TAG ="EraseDialogFragment";
    public static final String KEY_CARD_COLOR = "pref_cardcolor";

    private SharedPreferences preferences;
    private File root;
    private TextFilesHandler filesHandler;
    private static int cvColor;
    private RecyclerView rv;
    private TextView textView;
    private TextsAdapter textsAdapter;
    private List<TextModel> textList = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>(),subtexts = new ArrayList<>(),completeText = new ArrayList<>();


    public ArchivesFragment() {
        // Required empty public constructor
    }


    /*
    * So when I go to another item of the NavigationView, I want the title to change when I touch the back button.
    * In order to that, I override onResume and cast to objects (I don't even need to initialize those) son they would have
    * what they must have.
     */

    @Override
    public void onResume() {
        super.onResume();


        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.app_name);
        ((NavigationView) getActivity().findViewById(R.id.navigationview_1)).setCheckedItem(R.id.file_item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Instantiating everything

        View view = inflater.inflate(R.layout.fragment_archives, container, false);
        textView = view.findViewById(R.id.textview_main);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        rv = view.findViewById(R.id.recyclerview);
        textView.setVisibility(View.GONE);
        rv.setVisibility(View.INVISIBLE);

        /*
        * Clearing the everything so RecyclerView doesn't clone the items every time I get into the fragment
         */

        textList.clear();
        root = new File(Environment.getExternalStorageDirectory(),"Texts");
        if(!root.exists()){//if the directory on the internal storage doesn't exist, create it.
            root.mkdir();
        }

        String s[] = root.list();//Getting the titles of the files
        setCardViewColor(preferences);
        filesHandler = this.getArguments().getParcelable("File_handler");
        titles =  filesHandler.getTitles();
        subtexts = filesHandler.getSubtexts();
        completeText = filesHandler.getCompleteText();
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

    private void startRecyclerView(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setAutoMeasureEnabled(false);



        if(rv != null){
            rv.setHasFixedSize(true);
        }


        for(int i = 0;i < s.length; i++){
            TextModel textModel = new TextModel(titles.get(i),subtexts.get(i), completeText.get(i));
            textList.add(textModel);
        }

        textsAdapter = new TextsAdapter(textList,cvColor,
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
                    public void onItemLongClick(TextModel item,int position) {

                        /*
                        *This how you pass arguments from a Fragment to another Fragment.
                        *
                         */
                        Bundle bundle = new Bundle();
                        EraseDialogFragment eraseFragment = new EraseDialogFragment();
                        bundle.putString("title",item.getTitle());
                        bundle.putInt("position",position);
                        eraseFragment.setArguments(bundle);
                        eraseFragment.show(getFragmentManager(),DIALOG_TAG);
                        Toast.makeText(getActivity(),"You're gonna carry that weight", Toast.LENGTH_SHORT).show();

                    }
                });


        rv.setLayoutManager(layoutManager);
        rv.setAdapter(textsAdapter);
        textsAdapter.notifyDataSetChanged();
    }


    private void setCardViewColor(SharedPreferences preferences) {
        /*
        Setting the color of the cardView to them pass it to the Adapter for setting it.
         */

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

    public void refreshRecyclerView(){
        String s[] = root.list();//Getting the titles of the files
        filesHandler.refreshList();
        titles =  filesHandler.getTitles();
        subtexts = filesHandler.getSubtexts();
        completeText = filesHandler.getCompleteText();
        if(s.length != 0){
            textView.setVisibility(View.INVISIBLE);
            for(int i = 0;i < s.length; i++){
                TextModel textModel = new TextModel(titles.get(i),subtexts.get(i), completeText.get(i));
                textList.add(textModel);
            }
            textsAdapter.setTextList(textList);
            textsAdapter.notifyDataSetChanged();
        }
        else {
            textView.setVisibility(View.VISIBLE);
        }

    }

    /*
    Better approach for refreshing the items in the fragment.
    Implementing an interface from the EraseDialogFragment then removing the item and notifying the RecyclerV
    that the data has changed.
    If there are no text, put a text view.
     */

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        textList.remove(dialog.getArguments().getInt("position"));
        rv.removeViewAt(dialog.getArguments().getInt("position"));
        textsAdapter.notifyItemRemoved(dialog.getArguments().getInt("position"));
        textsAdapter.notifyItemRangeChanged(dialog.getArguments().getInt("position"),textList.size());
        textsAdapter.notifyDataSetChanged();
        if(textList.isEmpty()){
            rv.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
        dialog.dismiss();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        dialog.dismiss();
    }
}
