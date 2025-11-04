package com.bdajaya.adminku.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.bdajaya.adminku.AdminkuApplication;
import com.bdajaya.adminku.R;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_container, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            // Use the same SharedPreferences as BrowseUnitActivity and AdminkuApplication
            getPreferenceManager().setSharedPreferencesName("app_preferences");
            getPreferenceManager().setSharedPreferencesMode(android.content.Context.MODE_PRIVATE);

            setPreferencesFromResource(R.xml.preferences, rootKey);

            // Set up dark mode preference listener
            SwitchPreferenceCompat darkModePreference = findPreference("dark_mode");
            if (darkModePreference != null) {
                darkModePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isDarkModeEnabled = (Boolean) newValue;
                    AdminkuApplication app = (AdminkuApplication) requireActivity().getApplication();
                    app.toggleDarkMode(isDarkModeEnabled);
                    return true;
                });
            }
        }
    }
}
