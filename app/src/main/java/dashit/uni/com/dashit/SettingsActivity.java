package dashit.uni.com.dashit;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Jagrut on 16-Apr-16.
 * View when the user clicks on 'Settings' from Menu item
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            EditTextPreference textPref = (EditTextPreference) findPreference("myName");
            textPref.setSummary(textPref.getText());

            textPref = (EditTextPreference) findPreference("myPhoneNumber");
            textPref.setSummary(textPref.getText());

            textPref = (EditTextPreference) findPreference("contact");
            textPref.setSummary(textPref.getText());
        }
    }
}
