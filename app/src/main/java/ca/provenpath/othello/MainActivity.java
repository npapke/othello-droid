package ca.provenpath.othello;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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
    private final static int MSG_REDRAW = 341;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        final HumanPlayer human = new HumanPlayer( BoardValue.BLACK );
        ComputerPlayer computer = new ComputerPlayer( BoardValue.WHITE );

        mExecutor = new GameExecutor();
        mExecutor.setPlayer( 0, human );
        mExecutor.setPlayer( 1, computer );
        mExecutor.newGame();

        mBoardAdaptor = new BoardAdapter( this, mExecutor.getBoard() );
        GridView gridview = (GridView) findViewById( R.id.gridview );
        gridview.setAdapter( mBoardAdaptor );

        gridview.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            public void onItemClick( AdapterView<?> parent, View v, int position, long id )
            {
                Toast.makeText( MainActivity.this, "" + position, Toast.LENGTH_SHORT ).show();
                human.attemptMove( position );
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

        mExecutor.addObserver( new Observer()
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
                while (mExecutor.getState() != GameState.GAME_OVER)
                {
                    mExecutor.executeOneTurn();
                }
            }
        }.start();

        mHandler.obtainMessage( MSG_REDRAW ).sendToTarget();
    }


    private void updateDisplay()
    {
        TextView moveNumber = (TextView) findViewById( R.id.move_number );
        TextView humanScore = (TextView) findViewById( R.id.human_score );
        TextView computerScore = (TextView) findViewById( R.id.computer_score );

        moveNumber.setText( Integer.toString( mExecutor.getMoveNumber() ) );
        humanScore.setText( Integer.toString( mExecutor.getBoard().countBoardValues( BoardValue.BLACK ) ) );
        computerScore.setText( Integer.toString( mExecutor.getBoard().countBoardValues( BoardValue.WHITE ) ) );

        mBoardAdaptor.redraw();
    }


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

    private Handler mHandler;
    private GameExecutor mExecutor;
    private BoardAdapter mBoardAdaptor;
}
