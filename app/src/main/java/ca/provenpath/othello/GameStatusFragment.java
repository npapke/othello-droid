package ca.provenpath.othello;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.Player;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GameStatusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GameStatusFragment extends Fragment
{
    private static final String TAG = GameStatusFragment.class.getSimpleName();

    public GameStatusFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BoardStatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GameStatusFragment newInstance( String param1, String param2 )
    {
        GameStatusFragment fragment = new GameStatusFragment();
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        return inflater.inflate( R.layout.fragment_game_status, container, false );
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );

        Log.i( TAG, "Attaching a " + context.getClass().getSimpleName() );
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    public void update( GameExecutor executor )
    {
        // TODO make this translatable

        if (executor == null)
        {
            drawText( R.id.message, "Select 'New Game' to start" );
        }
        else
        {
            StringBuilder buf = new StringBuilder();

            int blackScore = executor.getBoard().countBoardValues( BoardValue.BLACK );
            int whiteScore = executor.getBoard().countBoardValues( BoardValue.WHITE );

            drawText( R.id.status_black_score, Integer.toString( blackScore ) );
            drawText( R.id.status_white_score, Integer.toString( whiteScore ) );
            drawText( R.id.status_remaining, Integer.toString( 61 - executor.getMoveNumber() ) );

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

            drawText( R.id.message, buf.toString() );
        }
    }

    private void drawText( int viewId, String msg )
    {
        View view = getView().findViewById( viewId );
        if (view instanceof TextView)
        {
            ((TextView) view).setText( msg );
        }
    }
}
