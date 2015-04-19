package ca.provenpath.othello;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class SettingsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public final static String KEY_STRATEGY = "pref_strategy";
    public final static String KEY_LOOKAHEAD = "pref_lookahead";
    public final static String KEY_PARALLEL = "pref_parallel";

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // Load the preferences from an XML resource
        addPreferencesFromResource( R.xml.preferences );

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onPause()
    {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener( this );
    }

    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        if (key.equals( KEY_STRATEGY ) || key.equals( KEY_LOOKAHEAD ))
        {
            Preference pref = findPreference( key );

            // Set summary to be the user-description for the selected value
            pref.setSummary( sharedPreferences.getString( key, "" ) );
        }
    }

}

