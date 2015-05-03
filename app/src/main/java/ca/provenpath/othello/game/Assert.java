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
