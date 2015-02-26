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
        setColor( color );
    }

    @Override
    public void makeMove( Board board )
    {
        nextMove.clear();   // no stale moves

        for (;;)
        {
            Move move = waitForMove();
            if (board.isValidMove( move ))
            {
                Log.d( TAG, "Applying move " + move );

                board.makeMove( move );
                break;
            }
            else
            {
                Log.d( TAG, "Invalid move " + move );
            }
        }
    }

    public void attemptMove( int lvalue )
    {
        Log.d( TAG, "Move to " + lvalue );

        nextMove.offer( new Move( getColor(), new Position( lvalue ) ) );
    }

    private Move waitForMove()
    {
        for (;;)
        {
            try
            {
                return nextMove.take();
            }
            catch (InterruptedException e)
            {
            }
        }
    }

    BlockingQueue<Move> nextMove = new ArrayBlockingQueue< Move >( 1 );
}
