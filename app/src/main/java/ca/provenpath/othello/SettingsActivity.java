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

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.List;

import ca.provenpath.othello.game.BoardValue;

/**
 * Created by npapke on 4/19/15.
 */
public class SettingsActivity extends PreferenceActivity
{
    public final static String TAG = SettingsActivity.class.getName();

    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource( R.xml.preference_headers, target );

        for (Header header : target)
        {
            if (header.extras != null)
            {
                String color = header.extras.getString( "player" );

                SharedPreferences prefs = getSharedPreferences(
                        PlayerSettingsFragment.getSharedPreferencesName( color ),
                        Context.MODE_PRIVATE );

                header.summary = prefs.getBoolean( PlayerSettingsFragment.KEY_ISCOMPUTER, false )
                        ? "Computer"
                        : "Human";
            }
        }

    }

    @Override
    protected boolean isValidFragment( String fragmentName )
    {
        return PlayerSettingsFragment.class.getName().equals( fragmentName );
    }
}
