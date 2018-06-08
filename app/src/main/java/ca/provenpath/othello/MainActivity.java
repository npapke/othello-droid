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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.ComputerPlayer;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.GameExecutorSerializer;
import ca.provenpath.othello.game.HumanPlayer;
import ca.provenpath.othello.game.Move;
import ca.provenpath.othello.game.Player;
import ca.provenpath.othello.game.Position;
import ca.provenpath.othello.game.StrategyFactory;
import ca.provenpath.othello.game.observer.GameState;


public class MainActivity extends ActionBarActivity
{
    public final static String TAG = MainActivity.class.getName();

    /*
     * ====================================================================
     *
     * Life cycle methods
     *
     * --------------------------------------------------------------------
     */

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        Log.i( TAG, "onCreate" );

        super.onCreate( savedInstanceState );

        PreferenceManager.setDefaultValues( this, R.xml.preferences, false );

        if (savedInstanceState != null)
        {
            mExecutor = GameExecutorSerializer.deserialize( savedInstanceState.getString( KEY_EXECUTOR ) );
            mLastMoveExecutorSerial = savedInstanceState.getString( KEY_LAST_MOVE );
        }

        setContentView( R.layout.activity_main );

        mBoardAdaptor = new BoardAdapter( this );
        GridView gridview = (GridView) findViewById( R.id.gridview );
        gridview.setAdapter( mBoardAdaptor );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                // Toast.makeText( MainActivity.this, "" + position, Toast.LENGTH_SHORT ).show();
                attemptMove( position );
            }
        } );

        /*
        gridview.setOnItemLongClickListener( new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick( AdapterView<?> parent, View v, int position, long id )
            {
                simulateMove( position );
                return true;
            }
        } );
        */


        findViewById( R.id.undo ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                undoGame();
            }
        } );


        findViewById( R.id.newgame ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                newGame();
            }
        } );

        /*
         * Process requests on the UI thread.
         */
        mHandler = new Handler( Looper.getMainLooper() )
        {
            @Override
            public void handleMessage( Message msg )
            {
                switch (msg.what)
                {
                    case MSG_REDRAW:
                        updateDisplay( GameExecutorSerializer.deserialize( (String) msg.obj ) );
                        break;

                    default:
                        super.handleMessage( msg );
                        break;
                }
            }
        };

        // Do this after everything is initialized
        if (mExecutor == null)
        {
            newGame();
        }
        else
        {
            runExecutor( mExecutor );
        }
    }

    @Override
    protected void onDestroy()
    {
        Log.i( TAG, "onDestroy" );

        if (mExecutor != null)
            mExecutor.endGame();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState( Bundle bundle )
    {
        Log.i( TAG, "onSaveInstanceState" );

        super.onSaveInstanceState( bundle );

        if (mExecutor != null)
            bundle.putString( KEY_EXECUTOR, GameExecutorSerializer.serialize( mExecutor ) );

        if (mLastMoveExecutorSerial != null)
            bundle.putString( KEY_LAST_MOVE, mLastMoveExecutorSerial );
    }

    @Override
    protected void onRestoreInstanceState( Bundle bundle )
    {
        Log.i( TAG, "onSaveInstanceState" );

        super.onRestoreInstanceState( bundle );
    }

    /*
     * ====================================================================
     *
     * Option menu methods
     *
     * --------------------------------------------------------------------
     */

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            Intent intent = new Intent( this, SettingsActivity.class );
            startActivity( intent );
            return true;
        }
        else if (id == R.id.action_about)
        {
            new AboutDialog().show( getFragmentManager(), null );
            return true;
        }
        else if (id == R.id.action_help)
        {
            // Cheat by sending user off to a web site
            Intent helpIntent = new Intent();
            helpIntent.setAction( Intent.ACTION_VIEW );
            helpIntent.setData( Uri.parse( getResources().getString( R.string.help_url ) ) );

            // Verify that the intent will resolve to an activity
            if (helpIntent.resolveActivity( getPackageManager() ) != null)
            {
                startActivity( helpIntent );
                return true;
            }
            else
            {
                Log.w( TAG, "No takers for intent: " + getResources().getText( R.string.help_url ) );
            }
        }
        else if (id == R.id.action_exit)
        {
            finish();
        }

        return super.onOptionsItemSelected( item );
    }


    /*
     * ====================================================================
     *
     * Manage display
     *
     * --------------------------------------------------------------------
     */

    /**
     * (Re)initializes the game state.
     */
    private void newGame()
    {
        if (mExecutor != null)
        {
            mExecutor.endGame();
        }

        mExecutor = new GameExecutor();

        mExecutor.newGame();
        runExecutor( mExecutor );
    }

    /**
     * Restores an earlier game state, if possible.
     */
    private void undoGame()
    {
        if (mLastMoveExecutorSerial != null)
        {
            GameExecutor lastMoveExecutor = GameExecutorSerializer.deserialize( mLastMoveExecutorSerial );

            if (lastMoveExecutor != null)
            {
                if (mExecutor != null)
                {
                    mExecutor.endGame();
                }

                mExecutor = lastMoveExecutor;

                runExecutor( mExecutor );
            }

            mLastMoveExecutorSerial = null;
        }
    }

    private void runExecutor( final GameExecutor executor )
    {
        if (executor != null)
        {
            new Thread("GameExecutor" )
            {
                @Override
                public void run()
                {
                    while (executor.getState() != GameState.GAME_OVER)
                    {
                        applyPreferences( executor );
                        sendRedrawRequest( executor );

                        // This is to provide a visible pause for fast computer moves.
                        long delay = 2000;
                        long noMoveBefore = System.currentTimeMillis() + delay;

                        executor.executeOneTurn();

                        sendRedrawRequest( executor );

                        try
                        {
                            Thread.sleep( Math.max( 0, noMoveBefore - System.currentTimeMillis() ) );
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                }
            }.start();
        }
    }

    /** Sends redraw request to UI thread. */
    private void sendRedrawRequest( GameExecutor executor )
    {
        Message msg = mHandler.obtainMessage( MSG_REDRAW );
        msg.obj = GameExecutorSerializer.serialize( executor );
        msg.sendToTarget();
    }

    private void applyPreferences( GameExecutor executor )
    {
        applyPreferences( executor, BoardValue.BLACK, 0 );
        applyPreferences( executor, BoardValue.WHITE, 1 );
    }

    private void applyPreferences( GameExecutor executor, BoardValue color, int index )
    {
        try
        {
            SharedPreferences prefs = getSharedPreferences(
                    PlayerSettingsFragment.getSharedPreferencesName( color.name() ),
                    Context.MODE_PRIVATE );

            if (prefs.getBoolean( PlayerSettingsFragment.KEY_ISCOMPUTER, false ))
            {
                ComputerPlayer cplayer = new ComputerPlayer( color );

                cplayer.setMaxDepth( Integer.parseInt( prefs.getString( PlayerSettingsFragment.KEY_LOOKAHEAD, "4" ) ) );
                cplayer.setStrategy( StrategyFactory.getObject( prefs.getString( PlayerSettingsFragment.KEY_STRATEGY, "" ) ) );

                executor.setPlayer( index, cplayer );
            }
            else
            {
                executor.setPlayer( index, new HumanPlayer( color ) );
            }
        }
        catch (Exception e)
        {
            Log.w( TAG, "Cannot apply preferences", e );
        }
    }

    private void updateDisplay( GameExecutor executor )
    {
        // TODO make this translatable
        TextView messageView = (TextView) findViewById( R.id.message );

        findViewById( R.id.undo ).setEnabled( mLastMoveExecutorSerial != null );

        if (executor == null)
        {
            messageView.setText( "Select 'New Game' to start" );

            mBoardAdaptor.redraw( null, BoardValue.EMPTY );
        }
        else
        {
            StringBuilder buf = new StringBuilder();

            int blackScore = executor.getBoard().countBoardValues( BoardValue.BLACK );
            int whiteScore = executor.getBoard().countBoardValues( BoardValue.WHITE );

            buf.append( String.format( "%d remaining, score %d - %d.\n",
                    61 - mExecutor.getMoveNumber(),
                    blackScore,
                    whiteScore ) );

            Player player = executor.getNextPlayer();

            switch (executor.getState())
            {
                case TURN_PLAYER_0:
                case TURN_PLAYER_1:
                    if (player != null)
                    {
                        if (player.isComputer())
                        {
                            buf.append( "Processing for " + player.getColor().name() );
                        }
                        else
                        {
                            buf.append( player.getColor().name() + ", your turn." );
                        }
                    }
                    else
                    {
                        buf.append( "missing player" );
                    }
                    break;

                case GAME_OVER:
                {
                    // Toast.makeText( MainActivity.this, "Game over", Toast.LENGTH_SHORT ).show();
                    if (blackScore > whiteScore)
                        buf.append( "BLACK wins" );
                    else if (blackScore < whiteScore)
                        buf.append( "WHITE wins" );
                    else
                        buf.append( "Tied game." );
                    break;
                }
            }

            messageView.setText( buf.toString() );

            showValidMoves( executor );
        }
    }

    private void showValidMoves( GameExecutor executor )
    {
        // Only for human player
        Player player = executor.getNextPlayer();

        if ((player != null) && !player.isComputer())
        {
            Board boardWithValid = (Board) mExecutor.getBoard().clone();

            BoardValue validMoveFilter = (player.getColor() == BoardValue.BLACK)
                ? BoardValue.VALID_BLACK
                : BoardValue.VALID_WHITE;

            mBoardAdaptor.redraw( boardWithValid, validMoveFilter );
        }
        else
        {
            mBoardAdaptor.redraw( executor.getBoard(), BoardValue.EMPTY );
        }
    }

    private void attemptMove( int position )
    {
        if (mExecutor != null)
        {
            Player human = mExecutor.getNextPlayer();
            if ((human != null) && !human.isComputer())
            {
                // FIXME this breaks encapsulation.  Probably should have the game executor
                // track undo moves.
                if (mExecutor.getBoard().isValidMove( new Move( human.getColor(), new Position( position ) ) ))
                {
                    mLastMoveExecutorSerial = GameExecutorSerializer.serialize( mExecutor );
                }

                ((HumanPlayer) human).attemptMove( position );
            }
        }
    }

    private final static int MSG_REDRAW = 341;
    private final static int MSG_SHOWVALID = 342;

    private final static String KEY_EXECUTOR = MainActivity.class.getName() + ".executor";
    private final static String KEY_LAST_MOVE = MainActivity.class.getName() + ".lastMove";

    private Handler mHandler;
    private GameExecutor mExecutor;
    private String mLastMoveExecutorSerial;
    private BoardAdapter mBoardAdaptor;
}
