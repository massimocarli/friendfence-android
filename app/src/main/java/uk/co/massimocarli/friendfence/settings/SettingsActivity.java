package uk.co.massimocarli.friendfence.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import uk.co.massimocarli.friendfence.R;

/**
 * Created by Massimo Carli on 14/09/14.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
