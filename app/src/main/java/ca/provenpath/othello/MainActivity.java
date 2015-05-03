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

        mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();
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
        mExecutor.setPlayer( mHumanPlayer, new HumanPlayer( BoardValue.BLACK ) );
        mExecutor.setPlayer( mComputerPlayer, new ComputerPlayer( BoardValue.WHITE ) );

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
            new Thread()
            {
                private long noDisplayUpdateBefore = 0;

                @Override
                public void run()
                {
                    executor.addObserver( new Observer()
                    {
                        @Override
                        public void update( Observable observable, Object data )
                        {
                            try
                            {
                                Thread.sleep( Math.max( 0, noDisplayUpdateBefore - System.currentTimeMillis() ) );
                            }
                            catch (InterruptedException e)
                            {
                            }

                            // Send redraw request to UI thread.
                            Message msg = mHandler.obtainMessage( MSG_REDRAW );
                            msg.obj = GameExecutorSerializer.serialize( (GameExecutor) data );
                            msg.sendToTarget();
                        }
                    } );

                    // Send redraw request to UI thread.
                    Message msg = mHandler.obtainMessage( MSG_REDRAW );
                    msg.obj = GameExecutorSerializer.serialize( executor );
                    msg.sendToTarget();

                    while (executor.getState() != GameState.GAME_OVER)
                    {
                        applyPreferences( executor );

                        // This is to provide a visible pause for fast computer moves.
                        long delay = 2000;
                        noDisplayUpdateBefore = System.currentTimeMillis() + delay;

                        executor.executeOneTurn();
                    }
                }
            }.start();
        }
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
                cplayer.setParallel( prefs.getBoolean( PlayerSettingsFragment.KEY_PARALLEL, false ) );
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

            mBoardAdaptor.redraw( null );
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
                    if (player.isComputer())
                    {
                        buf.append( "Processing for " + player.getColor().name() );
                    }
                    else
                    {
                        buf.append( "Your turn." );
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
            boardWithValid.determineValidMoves( player.getColor() );

            mBoardAdaptor.redraw( boardWithValid );
        }
        else
        {
            mBoardAdaptor.redraw( executor.getBoard() );
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

    private void simulateMove( int position )
    {
        if (mExecutor != null)
        {
            Board board = (Board) mExecutor.getBoard().clone();
            Move m = new Move( mExecutor.getPlayer( mHumanPlayer ).getColor(), new Position( position ) );
            if (board.isValidMove( m ))
            {
                board.makeMove( m );
                mBoardAdaptor.redraw( board );
            }
        }
    }


    private final static int MSG_REDRAW = 341;
    private final static int MSG_SHOWVALID = 342;

    private final static String KEY_EXECUTOR = MainActivity.class.getName() + ".executor";
    private final static String KEY_LAST_MOVE = MainActivity.class.getName() + ".lastMove";

    private int mHumanPlayer = 0;
    private int mComputerPlayer = 1;

    private Handler mHandler;
    private GameExecutor mExecutor;
    private String mLastMoveExecutorSerial;
    private BoardAdapter mBoardAdaptor;
}
