package com.eagsprojects.editordetexo;



import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.eagsprojects.editordetexo.Adapter.TextsAdapter;
import com.eagsprojects.editordetexo.Fragments.NewTextDialogFragment;
import com.eagsprojects.editordetexo.Model.TextModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    public TextView textView;
    public File root;
    public RecyclerView rv;
    public List<TextModel> textList = new ArrayList<>();
    public ArrayList<String> titles = new ArrayList<>(),subtexts = new ArrayList<>(),completeText = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textview_main);
        rv = findViewById(R.id.recyclerview);

        textView.setVisibility(View.GONE);
        rv.setVisibility(View.INVISIBLE);
        root = new File(Environment.getExternalStorageDirectory(),"Texts");
        if(!root.exists()){
            root.mkdir();
        }

        String s[] = root.list();


        setTitlesAndSubtexts(s,titles,subtexts,completeText);
        startRecyclerView(s,titles,subtexts,completeText);

        if(s.length == 0){
            textView.setVisibility(View.VISIBLE);
        }
        else{
            textView.setVisibility(View.GONE);
            rv.setVisibility(View.VISIBLE);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        showToolbar(toolbar,"EditME",false,View.GONE);
        startDrawerLayout(toolbar);

        /*NavigationView navigationView = (NavigationView) findViewById(R.id.navigationview_1);
        navigationView.setNavigationItemSelectedListener(new OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.file_item:
                        Toast.makeText(MainActivity.this, "AAAH", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.edit_item:
                        Toast.makeText(MainActivity.this, "AAAH", Toast.LENGTH_SHORT).show();
                        break;
                    case  R.id.preferences_item:
                        Toast.makeText(MainActivity.this, "AAAH", Toast.LENGTH_SHORT).show();
                        break;
                }

                return false;
            }
        });*/
    }


    private void setTitlesAndSubtexts(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        StringBuilder texts = new StringBuilder();
        for (int i = 0;i<s.length;i++){
            titles.add(s[i]);
            File file = new File(root,s[i]);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line,subtext;
                line = br.readLine();
                if(line.length()>155){
                    subtext = line.substring(0,155);
                    subtexts.add(subtext+"...");
                }
                else{
                    subtexts.add(line);
                }
                br.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0;i<s.length;i++){
            File file = new File(root,s[i]);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line,txt = "";

                while ((line = br.readLine()) != null) {
                    txt += line;
                    txt+= "\n";
                }
                completeText.add(txt);
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }



    private void startRecyclerView(String[] s, ArrayList<String> titles, ArrayList<String> subtexts, ArrayList<String> completeText) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        if(rv != null){
            rv.setHasFixedSize(true);
        }

        for(int i = 0;i < s.length; i++){
            TextModel textModel = new TextModel(titles.get(i),subtexts.get(i), completeText.get(i));
            textList.add(textModel);
        }

        for (int i = 0;i<s.length;i++){
            System.out.println(completeText.get(i));
        }

        TextsAdapter textsAdapter = new TextsAdapter(textList, new TextsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TextModel item) {
                Intent intent = new Intent(MainActivity.this,NewTextActivity.class);
                intent.putExtra("title",item.getTitle());
                intent.putExtra("text",item.getText());

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    /*Explode explode = new Explode();
                    explode.setDuration(1000);
                    getWindow().setExitTransition(explode);*/
                    startActivity(intent);
                }
                else{
                    startActivity(intent);
                }
            }
        });

        rv.setLayoutManager(layoutManager);
        rv.setAdapter(textsAdapter);
        textsAdapter.notifyDataSetChanged();
    }


    public void goNewText(View view){
        FragmentManager manager = getSupportFragmentManager();
        NewTextDialogFragment dialog = new NewTextDialogFragment();
        dialog.show(manager,"");
    }

    public void showToolbar(Toolbar toolbar, String title, boolean upButton,int showButton){
        Button button = findViewById(R.id.button_toolbar);
        button.setVisibility(showButton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }

    public void startDrawerLayout(Toolbar toolbar) {
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout_1);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        String s[] = root.list();
        setTitlesAndSubtexts(s,titles,subtexts);
        if(s.length == 0){
            textView.setVisibility(View.VISIBLE);
        }
        else{
            textView.setVisibility(View.GONE);
            //rv.setVisibility(View.VISIBLE);
            //startRecyclerView(s,titles,subtexts);
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK)
        {
            Intent refresh = new Intent(this, MainActivity.class);
            startActivity(refresh);
            this.finish();
        }
    }


}


