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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by npapke on 2/25/15.
 */
public class HumanPlayer extends Player
{
    public final static String TAG = HumanPlayer.class.getName();

    public HumanPlayer( BoardValue color )
    {
        super( color );
    }

    public HumanPlayer( String serial )
    {
        super( serial );
    }

    @Override
    public void makeMove( Board board )
    {
        nextMove.clear();   // no stale moves

        for (;;)
        {
            HumanMove humanMove = waitForMove();
            Move boardMove = humanMove.getMove();
            if (boardMove == null)
                return;

            if (board.isValidMove( boardMove ))
            {
                Log.i( TAG, "Applying move " + boardMove );

                board.makeMove( boardMove );
                break;
            }
            else
            {
                Log.d( TAG, "Invalid move " + boardMove );
            }
        }
    }

    @Override
    public void interruptMove()
    {
        // Signal the end
        nextMove.offer( new HumanMove() );
    }

    public void attemptMove( int lvalue )
    {
        Log.d( TAG, "Move to " + lvalue );

        nextMove.offer( new HumanMove( lvalue ) );
    }

    private HumanMove waitForMove()
    {
        for (;;)
        {
            try
            {
                return nextMove.take( );
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    /**
     * Just an apparatus to signal "no move"
     */
    private class HumanMove
    {
        public HumanMove()
        {
            mMove = null;
        }

        public HumanMove( int lvalue )
        {
            mMove = new Move( getColor(), new Position( lvalue ) );
        }

        public Move getMove()
        {
            return mMove;
        }

        private Move mMove;
    }

    private BlockingQueue<HumanMove> nextMove = new ArrayBlockingQueue< HumanMove >( 1 );
}
