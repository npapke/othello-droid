package ca.provenpath.othello.game;

import android.util.Log;

/**
 * Created by npapke on 2/22/15.
 */
public class Assert
{
    public final static String TAG = Assert.class.getName();

    public static void notNull( Object o )
    {
        isTrue( o != null );
    }

    public static void isTrue( boolean condition)
    {
        isTrue( condition, "" );
    }

    public static void isTrue( boolean condition, String message )
    {
        if (!condition)
        {
            Log.e( TAG, "Failed assertion: " + message, new Throwable() );
        }
    }
}
