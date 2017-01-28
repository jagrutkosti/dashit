package dashit.uni.com.dashit.view.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

import dashit.uni.com.dashit.R;

/**
 * Created by Jagrut on 16-Apr-16.
 * View when the user clicks on 'Settings' from Menu item
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsPreferenceFragment()).commit();
    }

    public static class SettingsPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

            EditTextPreference textPref = (EditTextPreference) findPreference("myName");
            if(textPref.getText() != null)
                textPref.setSummary("Your name: " + textPref.getText());
            else
                textPref.setSummary("Your name: Not set");

            textPref = (EditTextPreference) findPreference("myPhoneNumber");
            if(textPref.getText() != null)
                textPref.setSummary("Your phone number: " + textPref.getText());
            else
                textPref.setSummary("Your phone number: Not set");

            textPref = (EditTextPreference) findPreference("contact");
            if(textPref.getText() != null)
                textPref.setSummary("Your emergency contact: " + textPref.getText());
            else
                textPref.setSummary("Your emergency contact: Not set");
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference changedPreference = findPreference(key);
            if(changedPreference instanceof  EditTextPreference){
                EditTextPreference textPref = (EditTextPreference) findPreference(key);
                String title = (String) textPref.getTitle();
                switch(title){
                    case "Name":
                        if(textPref.getText() != null)
                            textPref.setSummary("Your name: " + textPref.getText());
                        else
                            textPref.setSummary("Your name: Not set");
                        break;
                    case "Phone Number":
                        if(textPref.getText() != null)
                            textPref.setSummary("Your phone number: " + textPref.getText());
                        else
                            textPref.setSummary("Your phone number: Not set");
                        break;
                    case "Emergency Contact":
                        if(textPref.getText() != null)
                            textPref.setSummary("Your emergency contact: " + textPref.getText());
                        else
                            textPref.setSummary("Your emergency contact: Not set");
                        break;
                }
            }

        }
    }
}
