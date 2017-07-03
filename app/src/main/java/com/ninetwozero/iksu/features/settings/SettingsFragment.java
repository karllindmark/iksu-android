package com.ninetwozero.iksu.features.settings;

import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;

import com.ninetwozero.iksu.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_main);
    }
}
