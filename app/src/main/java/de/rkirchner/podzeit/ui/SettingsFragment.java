package de.rkirchner.podzeit.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import de.rkirchner.podzeit.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);
    }
}
