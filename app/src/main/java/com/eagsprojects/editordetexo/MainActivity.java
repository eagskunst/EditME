package com.eagsprojects.editordetexo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.eagsprojects.editordetexo.Fragments.ArchivesFragment;
import com.eagsprojects.editordetexo.Fragments.NewTextDialogFragment;
import com.eagsprojects.editordetexo.Fragments.SettingsFragment;


public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";


    private SharedPreferences preferences;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        navigationView = findViewById(R.id.navigationview_1);
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,"EditME",false,View.GONE);

        getFragmentManager().beginTransaction()
                .replace(R.id.container, new ArchivesFragment())
                .addToBackStack(null)
                .commit();
        startDrawerLayout(toolbar);
        startNavigationView();
        navigationView.setCheckedItem(R.id.file_item);
    }



    private void startNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.file_item:
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new ArchivesFragment())
                                .addToBackStack(null)
                                .commit();//Interesante, este no es con getSupportFragmentManager() si no con getFragmentManager()
                        getSupportActionBar().setTitle("EditME");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.edit_item:
                        Toast.makeText(MainActivity.this, "AAAH", Toast.LENGTH_SHORT).show();
                        break;
                    case  R.id.configuration_item:
                        /*setContentView(R.layout.fragment_settings);/*Aquí sucedio algo interesante, llamando simplemente al FragmentManager no respondía
                        bien al cambiar al PrefSettingsFragment. La solución tampoco fue incluir el código de fragment_settings.xml en el activity_main.xml
                        o en el navigation_view_layout.xml, sino llamar a setContentView() y hacer la transición.*/
                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, new SettingsFragment()/*,SETTINGS_TAG*/)
                                .addToBackStack(null)
                                .commit();
                        getSupportActionBar().setTitle(R.string.settings);
                        drawerLayout.closeDrawers();
                        break;
                    default:
                        navigationView.setCheckedItem(R.id.file_item);
                        break;
                }
                return true;
            }
        });
    }


    public void goNewText(View view){
        FragmentManager manager = getSupportFragmentManager();
        NewTextDialogFragment dialog = new NewTextDialogFragment();
        dialog.show(manager,TAG);
    }

    public void showToolbar(Toolbar toolbar, String title, boolean upButton,int showButton){
        Button button = findViewById(R.id.button_toolbar);
        button.setVisibility(showButton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }

    public void startDrawerLayout(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawerlayout_1);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }


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


