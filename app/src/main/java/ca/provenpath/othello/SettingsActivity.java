package ca.provenpath.othello;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by npapke on 4/19/15.
 */
public class SettingsActivity extends Activity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace( android.R.id.content, new SettingsFragment() )
                .commit();
    }
}
