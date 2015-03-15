package ca.provenpath.othello;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.ComputerPlayer;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.HumanPlayer;
import ca.provenpath.othello.game.observer.GameState;


public class MainActivity extends ActionBarActivity
{
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
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        mBoardAdaptor = new BoardAdapter( this );
        GridView gridview = (GridView) findViewById( R.id.gridview );
        gridview.setAdapter( mBoardAdaptor );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                Toast.makeText( MainActivity.this, "" + position, Toast.LENGTH_SHORT ).show();
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

                    default:
                        super.handleMessage( msg );
                        break;
                }
            }
        };

        mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();
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

        mExecutor.addObserver( new Observer()
        {
            @Override
            public void update( Observable observable, Object data )
            {
                // Send redraw request to UI thread.
                mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();
            }
        } );

        mExecutor.newGame();

        // Run the game on a separate thread
        new Thread()
        {
            @Override
            public void run()
            {
                while (mExecutor.getState() != GameState.GAME_OVER)
                {
                    mExecutor.executeOneTurn();
                }
            }
        }.start();

    }

    private void updateDisplay()
    {
        TextView moveNumber = (TextView) findViewById( R.id.move_number );
        TextView humanScore = (TextView) findViewById( R.id.human_score );
        TextView computerScore = (TextView) findViewById( R.id.computer_score );

        if (mExecutor == null)
        {
            moveNumber.setText( "" );
            humanScore.setText( "" );
            computerScore.setText( "" );

            mBoardAdaptor.redraw( null );
        }
        else
        {
            moveNumber.setText( Integer.toString( mExecutor.getMoveNumber() ) );
            humanScore.setText( Integer.toString( mExecutor.getBoard().countBoardValues( BoardValue.BLACK ) ) );
            computerScore.setText( Integer.toString( mExecutor.getBoard().countBoardValues( BoardValue.WHITE ) ) );

            mBoardAdaptor.redraw( mExecutor.getBoard() );
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

    private int mHumanPlayer = 0;
    private int mComputerPlayer = 1;

    private Handler mHandler;
    private GameExecutor mExecutor;
    private BoardAdapter mBoardAdaptor;
}
