package com.eagsprojects.editordetexo;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eagsprojects.editordetexo.Fragments.ArchivesFragment;
import com.eagsprojects.editordetexo.Fragments.HelpFragment;
import com.eagsprojects.editordetexo.Fragments.NewTextDialogFragment;
import com.eagsprojects.editordetexo.Fragments.SettingsFragment;
import com.eagsprojects.editordetexo.Handlers.DownloadHandler;
import com.eagsprojects.editordetexo.Handlers.TextFilesHandler;
import com.eagsprojects.editordetexo.Handlers.UploadHandler;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ThreadFactory;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "MainActivity";
    private static final String ARCHIVES_TAG = "ArchivesFragment";
    private static final String SETTINGS_TAG = "SettingsFragment";
    private static final String HELP_TAG = "HelpFragment";
    private static final String KEY_IS_SIGN_IN = "pref_is_sign_in";
    private static final int REQUEST_CODE_SIGN_IN = 0;

    //Instance of global variables
    private SharedPreferences preferences;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private GoogleSignInClient signInClient;
    private DriveResourceClient driveResourceClient;
    private DriveClient driveClient;
    private Bundle bundle;
    private TextFilesHandler filesHandler;
    private ProgressBar progressBar;
    private final boolean[] internetAccess = new boolean[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bundle = new Bundle();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar, "EditME", false, View.GONE);

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences.Editor editor = preferences.edit();

        /*
        Setting up the SharePreferences to set if there's an account or not
         */

        if(account != null){
            Log.i(TAG,"CUENTA: "+account.getDisplayName());
            editor.putBoolean(KEY_IS_SIGN_IN,true);
            editor.commit();
            driveClient = Drive.getDriveClient(this, account);
            // Build a drive resource client.
            driveResourceClient = Drive.getDriveResourceClient(this, account);
        }
        else{
            editor.putBoolean(KEY_IS_SIGN_IN,false);
            editor.commit();
            initializeGoogleSignIn();
        }


        /*
        Setting the main fragment as the Activity is created
         */

        filesHandler = new TextFilesHandler();
        bundle.putParcelable("File_handler",filesHandler);

        ArchivesFragment archivesFragment = new ArchivesFragment();
        archivesFragment.setArguments(bundle);
        getFragmentManager().beginTransaction()//This transaction doesn't have addToBackStack in order to have correct back button functionality
                .replace(R.id.container, archivesFragment,ARCHIVES_TAG)
                .commit();

        startDrawerLayout(toolbar);
        startNavigationView();
        navigationView.setCheckedItem(R.id.file_item); //Checking the item

        getFragmentManager().executePendingTransactions();
    }




    private void startNavigationView() {
        /*
        Handling container changes
         */
        navigationView = findViewById(R.id.navigationview_1); 
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.file_item:
                        filesHandler.refreshList();
                        ArchivesFragment archivesFragment = new ArchivesFragment();
                        bundle.putParcelable("File_handler",filesHandler);
                        archivesFragment.setArguments(bundle);
                        fragmentTransaction(archivesFragment,R.string.app_name,ARCHIVES_TAG);
                        break;
                    case  R.id.configuration_item:
                        SettingsFragment settingsFragment = new SettingsFragment();
                        fragmentTransaction(settingsFragment,R.string.settings,SETTINGS_TAG);
                        break;
                    case R.id.help_item:
                        HelpFragment helpFragment = new HelpFragment();
                        fragmentTransaction(helpFragment, R.string.help,HELP_TAG);
                        break;
                    case R.id.googledrivesync_item:
                        filesHandler.refreshList();
                        if(preferences.getBoolean(KEY_IS_SIGN_IN,true)){
                            Thread thread = threadInternetAccess();
                            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                            thread.start();
                            try {
                                thread.join();
                                if(internetAccess[0]){
                                    UploadHandler uploadHandler =
                                            new UploadHandler(driveClient, driveResourceClient, filesHandler,
                                                    MainActivity.this, progressBar);

                                }
                                else{
                                    Toast.makeText(MainActivity.this, R.string.connection_timed_out, Toast.LENGTH_SHORT).show();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        else{
                            Toast.makeText(MainActivity.this, R.string.not_linked_to_google, Toast.LENGTH_SHORT).show();
                        }
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.googledrivedwn_item:
                        if(preferences.getBoolean(KEY_IS_SIGN_IN,true)){
                            Thread thread = threadInternetAccess();
                            thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
                            thread.start();
                            try {
                                thread.join();
                                if(internetAccess[0]){
                                    DownloadHandler downloadHandler =
                                            new DownloadHandler(driveClient, driveResourceClient,
                                                    filesHandler, MainActivity.this, progressBar);
                                }
                                else{
                                    Toast.makeText(MainActivity.this, R.string.connection_timed_out, Toast.LENGTH_SHORT).show();
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            Toast.makeText(MainActivity.this, R.string.not_linked_to_google, Toast.LENGTH_SHORT).show();
                        }
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
        /*
        FAB Listener to create a dialog for creating a new text.
        The FAB is in the fragment_archives.xml file, but when I
        use android:onClick the method must be here.
         */

        FragmentManager manager = getSupportFragmentManager();
        NewTextDialogFragment dialog = new NewTextDialogFragment();
        dialog.show(manager,TAG);
    }

    public void showToolbar(Toolbar toolbar, String title, boolean upButton,int showButton){
        /*
        Pretty cool way for showing the toolbar a manage it.
         */

        Button button = findViewById(R.id.button_toolbar);
        button.setVisibility(showButton);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);

    }

    public void startDrawerLayout(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawerlayout_1);
        //ActionBarDrawerToggle enables integration between drawer functionality and app bar framework.
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,drawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.
                    driveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    // Build a drive resource client.
                    driveResourceClient =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(KEY_IS_SIGN_IN,true);
                    editor.commit();
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                }
                break;
        }
    }

    public void fragmentTransaction(Fragment fragment,int title,String tag){
        getFragmentManager().beginTransaction()//Have to test with SupportFragmentManager
                .replace(R.id.container, fragment,tag)
                .addToBackStack(null)
                .commit();
        getFragmentManager().executePendingTransactions();//In order to be secure that the fragment exist and we could use findFragmentByTag, this line have to be after the commits
        getSupportActionBar().setTitle(title);//Setting the title as the fragment changes
        if(drawerLayout != null){
            drawerLayout.closeDrawers();//Closing the NavigationViewMenu
        }
    }

    /*
    if the device is in airplane mode (or presumably in other situations where there's no available network),
    cm.getActiveNetworkInfo() will be null, so you need to add a null check.
     */

    private static boolean isConnectedToInternet(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    private static boolean hasInternetAccess(Context context) {
        Log.i(TAG,"Entré al internetacess");
        if (isConnectedToInternet(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                Log.i(TAG,"Pidiendo conexión");
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(TAG, "No network available!");
            Toast.makeText(context,R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private Thread threadInternetAccess(){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if (hasInternetAccess(getApplicationContext())) {
                        internetAccess[0] = true;
                        Log.i(TAG, "Entré para crear el download");
                    } else {
                        internetAccess[0] = false;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }


    private GoogleSignInClient buildGoogleSignIn() {
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();

        return GoogleSignIn.getClient(this, signInOptions);

    }
    private void initializeGoogleSignIn() {
        signInClient = buildGoogleSignIn();
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error de conexion!", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "OnConnectionFailed: " + connectionResult);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        SharedPreferences.Editor editor = preferences.edit();

        if(account != null){
            editor.putBoolean(KEY_IS_SIGN_IN,true);
           editor.commit();
        }
        else{
            editor.putBoolean(KEY_IS_SIGN_IN,false);
            editor.commit();
        }
    }
}


