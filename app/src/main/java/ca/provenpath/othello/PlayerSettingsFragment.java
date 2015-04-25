package ca.provenpath.othello;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.Player;

public class PlayerSettingsFragment extends PreferenceFragment
    implements SharedPreferences.OnSharedPreferenceChangeListener
{
    public final static String TAG = PlayerSettingsFragment.class.getName();

    public final static String KEY_ISCOMPUTER = "pref_iscomputer";
    public final static String KEY_STRATEGY = "pref_strategy";
    public final static String KEY_LOOKAHEAD = "pref_lookahead";
    public final static String KEY_PARALLEL = "pref_parallel";

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        String playerColor = getArguments().getString( "player" );

        Log.i( TAG, "Player color: " + playerColor );

        getPreferenceManager().setSharedPreferencesName( getSharedPreferencesName( playerColor ) );

        // Load the player_preferences from an XML resource
        addPreferencesFromResource( R.xml.player_preferences );

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();

        // Display current values
        onSharedPreferenceChanged( prefs, KEY_STRATEGY );
        onSharedPreferenceChanged( prefs, KEY_LOOKAHEAD );

        prefs.registerOnSharedPreferenceChangeListener( this );
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

    public static String getSharedPreferencesName( String player )
    {
        return player + "_prefs";
    }
}

