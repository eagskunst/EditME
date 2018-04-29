package com.eagsprojects.editordetexo.Fragments;

/**
 * Created by Emmanuel on 19/3/2018.
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.eagsprojects.editordetexo.MainActivity;
import com.eagsprojects.editordetexo.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static android.app.Activity.RESULT_OK;
import static com.eagsprojects.editordetexo.Fragments.SettingsFragment.Settings.KEY_IS_SIGN_IN;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    private SharedPreferences preferences;


    private DriveResourceClient driveResourceClient;
    private DriveClient driveClient;
    private static final int REQUEST_CODE_SIGN_IN = 0;

    public SettingsFragment(){

    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings);
        ((NavigationView) getActivity().findViewById(R.id.navigationview_1)).setCheckedItem(R.id.configuration_item);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
        * This statement here is super important when working with PreferenceFragment
        * So, we must first create the fragment that will be the one who has the PreferenceFragment inside it.
        * To accomplish this, we must make another transaction here BUT the container must be
        * the FrameLayout we have in our fragment_settings.xml layout!
        * Check out that when I make the transaction, I create a Settings object, an inner class who extends PreferenceFragment!
         */

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.frame_container,new Settings())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    /*
    Changing preferences if we are loggin in
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Log.i(TAG, "Sign in request code");
                // Called after user is signed in.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");
                    // Use the last signed in account here since it already have a Drive scope.
                    driveClient = Drive.getDriveClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()));
                    // Build a drive resource client.
                    driveResourceClient =
                            Drive.getDriveResourceClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()));

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(KEY_IS_SIGN_IN,true);
                    editor.commit();
                }
                break;
        }
    }


    public static class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String TAG = "PrefSettingsFragment";
        public static final String KEY_FONT_TYPES = "pref_fonttype";
        public static final String KEY_FONT_SIZE = "pref_fontsize";
        public static final String KEY_CARD_COLOR = "pref_cardcolor";
        public static final String KEY_IS_SIGN_IN = "pref_is_sign_in";
        private static final int REQUEST_CODE_SIGN_IN = 0;

        private SharedPreferences preferences;
        private ListPreference listPreference1,listPreference2,listPreference3;//Have to instance this in order to catch their changes.
        private SwitchPreference switchPreference;

        private GoogleSignInClient signInClient;


        public Settings(){}


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Context context = getActivity();
            preferences = PreferenceManager.getDefaultSharedPreferences(context);
            listPreference1 = (ListPreference) getPreferenceManager().findPreference(KEY_FONT_TYPES);
            listPreference2 = (ListPreference) getPreferenceManager().findPreference(KEY_FONT_SIZE);
            listPreference3 = (ListPreference) getPreferenceManager().findPreference(KEY_CARD_COLOR);
            switchPreference = (SwitchPreference) getPreferenceManager().findPreference(KEY_IS_SIGN_IN);


        }

        /*
        Always override onResume and onPause like this to
        update the changes on the preferences!
         */

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onStart() {
            super.onStart();
            if(getPreferenceManager().getSharedPreferences().getBoolean(KEY_IS_SIGN_IN,true)){
                switchPreference.setTitle(R.string.sign_out_google);
            }
            else{
                switchPreference.setTitle(R.string.sign_in_google);
            }
            switchPreference.setOnPreferenceChangeListener(switchPreferenceListener());
        }

        /*
        Implementing the interface
         */

        @Override
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String s) {

            if(s.equals(KEY_FONT_TYPES)){ //'s' is the Key Value
                Log.w(TAG,"Entré.");
                final SharedPreferences.Editor editor = preferences.edit(); //Editing the preferences
                listPreference1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        editor.putString(KEY_FONT_TYPES,o.toString());//'o' is the new value, but it comes in Object.
                        return true;
                    }
                });
                editor.commit();
            }

            else if(s.equals(KEY_FONT_SIZE)){
                Log.w(TAG,"Entré.");
                final SharedPreferences.Editor editor = preferences.edit();
                listPreference2.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        editor.putString(KEY_FONT_SIZE,o.toString());
                        return true;
                    }
                });
                editor.commit();
            }

            else if(s.equals(KEY_CARD_COLOR)){
                Log.w(TAG,"Entré.");
                final SharedPreferences.Editor editor = preferences.edit();
                listPreference3.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        editor.putString(KEY_CARD_COLOR,o.toString());
                        return true;
                    }
                });
                editor.commit();
            }


            else if(s.equals(KEY_IS_SIGN_IN)){
                Log.w(TAG,"Entré al swith prefrence.");
                switchPreference.setOnPreferenceChangeListener(switchPreferenceListener());
            }

            else{
                Log.w(TAG,"No entré. S: "+s);
            }

        }
        /*
        Simple Google signOut
         */

        private void startSignOut() {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .build();
            signInClient = GoogleSignIn.getClient(getActivity(),signInOptions);
            signInClient.signOut()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            SharedPreferences.Editor editor = preferences.edit();
                            switchPreference.setTitle(R.string.sign_in_google);
                            switchPreference.setChecked(false);
                            editor.putBoolean(KEY_IS_SIGN_IN,false);
                            editor.commit();
                        }
                    });

        }


        private GoogleSignInClient buildGoogleSignIn() {
            GoogleSignInOptions signInOptions =
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestScopes(Drive.SCOPE_FILE)
                            .build();

            return GoogleSignIn.getClient(getActivity(), signInOptions);

        }
        private void initializeGoogleSignIn() {
            signInClient = buildGoogleSignIn();
            Intent intent = signInClient.getSignInIntent();
            startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
        }

        /*
        I have to make this method in order to have a working listener since the start
        of the fragment.
         */
        private Preference.OnPreferenceChangeListener switchPreferenceListener(){
            final SharedPreferences.Editor editor = preferences.edit();
            Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Log.i(TAG,"Entering listener...");
                    if(switchPreference.isChecked()){
                        Log.i(TAG,"Entré! al isChecked = true");
                        startSignOut();
                        return true;
                    }
                    else{
                        Log.i(TAG,"Entré! al isChecked = false");
                        initializeGoogleSignIn();
                        switchPreference.setTitle(R.string.sign_out_google);
                        switchPreference.setChecked(true);
                        return true;
                    }
                }
            };
            return listener;
        }

    }
}
