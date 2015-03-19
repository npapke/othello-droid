package ca.provenpath.othello;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.ComputerPlayer;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.HumanPlayer;
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
        Log.i(TAG, "onCreate");

        super.onCreate( savedInstanceState );

        if (savedInstanceState != null)
        {
            try
            {
                Gson deserializer = new Gson();
                String data = savedInstanceState.getString( KEY_EXECUTOR );
                Log.w( TAG, "serialized=" + data );
                mExecutor = deserializer.fromJson( savedInstanceState.getString( KEY_EXECUTOR ), GameExecutor.class );
            }
            catch (Exception e)
            {
                Log.w( TAG, "Deserialization failed.", e );
                mExecutor = null;
            }
        }

        setContentView( R.layout.activity_main );

        mBoardAdaptor = new BoardAdapter( this );
        GridView gridview = (GridView) findViewById( R.id.gridview );
        gridview.setAdapter( mBoardAdaptor );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                // Toast.makeText( MainActivity.this, "" + position, Toast.LENGTH_SHORT ).show();
                attemptMove( position );
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
                        updateDisplay();
                        break;

                    case MSG_SHOWVALID:
                        showValidMoves();
                        break;

                    default:
                        super.handleMessage( msg );
                        break;
                }
            }
        };

        mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();

        // Do this after everything is initialized
        runExecutor( mExecutor );
    }

    @Override
    protected void onDestroy()
    {
        Log.i(TAG, "onDestroy");

        if (mExecutor != null)
            mExecutor.endGame();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState( Bundle bundle )
    {
        Log.i(TAG, "onSaveInstanceState");

        super.onSaveInstanceState( bundle );

        try
        {
            Gson serializer = new Gson();

            // FIXME Thread-safety
            String data = serializer.toJson( mExecutor );

            Log.i( TAG, "Serialized=" + data );

            String noObservers = "\"observers\":\\[null\\],";
            Pattern pattern = Pattern.compile( noObservers );
            Matcher matcher = pattern.matcher( data );
            data = matcher.replaceAll( "" );

            Log.i( TAG, "Serialized=" + data );

            bundle.putString( KEY_EXECUTOR, data );
        }
        catch (Exception e)
        {
            Log.w( TAG, "Serialization failed", e );
        }
    }

    @Override
    protected void onRestoreInstanceState( Bundle bundle )
    {
        Log.i(TAG, "onSaveInstanceState");

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
            return true;
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

    private void runExecutor( final GameExecutor executor )
    {
        if (executor != null)
        {
            executor.addObserver( new Observer()
            {
                @Override
                public void update( Observable observable, Object data )
                {
                    // Send redraw request to UI thread.
                    mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();
                }
            } );

            new Thread()
            {
                @Override
                public void run()
                {
                    mHandler.obtainMessage( MSG_SHOWVALID ).sendToTarget();

                    while (executor.getState() != GameState.GAME_OVER)
                    {
                        executor.executeOneTurn();
                        try
                        {
                            // This is to provide a visible pause for fast computer moves.
                            Thread.sleep( 500, 0 );
                            mHandler.obtainMessage( MSG_SHOWVALID ).sendToTarget();
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                }
            }.start();
        }
    }

    private void updateDisplay()
    {
        // TODO make this translatable
        TextView messageView = (TextView) findViewById( R.id.message );

        if (mExecutor == null)
        {
            messageView.setText( "Select 'New Game' to start" );

            mBoardAdaptor.redraw( null );
        }
        else
        {
            StringBuilder buf = new StringBuilder();

            int humanScore = mExecutor.getBoard().countBoardValues( BoardValue.BLACK );
            int computerScore = mExecutor.getBoard().countBoardValues( BoardValue.WHITE );

            buf.append( String.format( "%d remaining, score %d - %d.",
                    61 - mExecutor.getMoveNumber(),
                    humanScore,
                    computerScore ) );

            switch (mExecutor.getState())
            {
                case TURN_PLAYER_0:
                    buf.append( "  Your turn." );
                    break;

                case TURN_PLAYER_1:
                    buf.append( "  Processing." );
                    break;

                case GAME_OVER:
                {
                    Toast.makeText( MainActivity.this, "Game over", Toast.LENGTH_SHORT ).show();
                    if (humanScore > computerScore)
                        buf.append( "  You WIN!" );
                    else if (humanScore < computerScore)
                        buf.append( "  Game over.  You did not win." );
                    else
                        buf.append( "  Tied game." );
                    break;
                }
            }

            messageView.setText( buf.toString() );

            mBoardAdaptor.redraw( mExecutor.getBoard() );
        }
    }

    private void showValidMoves()
    {
        // Only for human player
        if ((mExecutor.getState() == GameState.TURN_PLAYER_0) && (mHumanPlayer == 0))
        {
            Board boardWithValid = (Board) mExecutor.getBoard().clone();
            boardWithValid.determineValidMoves( mExecutor.getPlayer( mHumanPlayer ).getColor() );

            mBoardAdaptor.redraw( boardWithValid );
        }
    }

    private void attemptMove( int position )
    {
        if (mExecutor != null)
        {
            HumanPlayer human = (HumanPlayer) mExecutor.getPlayer( mHumanPlayer );
            if (human != null)
                human.attemptMove( position );
        }
    }


    private final static int MSG_REDRAW = 341;
    private final static int MSG_SHOWVALID = 342;

    private final static String KEY_EXECUTOR = MainActivity.class.getName() + ".executor";

    private int mHumanPlayer = 0;
    private int mComputerPlayer = 1;

    private Handler mHandler;
    private GameExecutor mExecutor;
    private BoardAdapter mBoardAdaptor;
}
