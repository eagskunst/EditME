package com.eagsprojects.editordetexo.Fragments;

/**
 * Created by Emmanuel on 19/3/2018.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eagsprojects.editordetexo.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment(){

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().getFragmentManager().beginTransaction()
                .replace(R.id.frame_container,new Settings())
                .commit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        return view;
    }

    public static class Settings extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static final String TAG = "PrefSettingsFragment";
        public static final String KEY_FONT_TYPES = "pref_fonttype";
        public static final String KEY_FONT_SIZE = "pref_fontsize";
        public static final String KEY_CARD_COLOR = "pref_cardcolor";
        private SharedPreferences preferences;
        private ListPreference listPreference1,listPreference2,listPreference3;

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

        }


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
        public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String s) {

            if(s.equals(KEY_FONT_TYPES)){
                Log.w(TAG,"Entré.");
                final SharedPreferences.Editor editor = preferences.edit();
                listPreference1.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        editor.putString(KEY_FONT_TYPES,o.toString());
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

            else{
                Log.w(TAG,"No entré. S: "+s);
            }

        }

    }
}
