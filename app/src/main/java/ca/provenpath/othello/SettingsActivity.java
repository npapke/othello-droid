package ca.provenpath.othello;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import java.util.List;

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
    }

    @Override
    protected boolean isValidFragment( String fragmentName )
    {
        return "ca.provenpath.othello.PlayerSettingsFragment".equals( fragmentName );
    }
}
