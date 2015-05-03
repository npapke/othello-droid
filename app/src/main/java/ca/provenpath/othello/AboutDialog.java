package ca.provenpath.othello;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by npapke on 5/3/15.
 */
public class AboutDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );

        builder.setTitle( R.string.about_title )
                .setView( R.layout.dialog_about );

        return builder.create();
    }
}
