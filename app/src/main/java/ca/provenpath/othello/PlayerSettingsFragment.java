/*
 *  Copyright (c) 2015 Norbert Papke <npapke@acm.org>
 *
 *  This file is part of Othello.
 *
 *  Othello is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Othello is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Othello.  If not, see <http://www.gnu.org/licenses/>.
 */

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

    public final static String KEY_CATEGORY = "pref_category";
    public final static String KEY_ISCOMPUTER = "pref_iscomputer";
    public final static String KEY_STRATEGY = "pref_strategy";
    public final static String KEY_LOOKAHEAD = "pref_lookahead";

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
        findPreference( KEY_CATEGORY ).setTitle( playerColor );

        // Display current values
        onSharedPreferenceChanged( prefs, KEY_CATEGORY );
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
        Preference pref = findPreference( key );

        if (key.equals( KEY_STRATEGY ) || key.equals( KEY_LOOKAHEAD ))
        {
            // Set summary to be the user-description for the selected value
            pref.setSummary( sharedPreferences.getString( key, "" ) );
        }
    }

    public static String getSharedPreferencesName( String player )
    {
        return player + "_prefs";
    }
}

