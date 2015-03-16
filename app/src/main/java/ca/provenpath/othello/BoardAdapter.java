package ca.provenpath.othello;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.BoardValue;

/**
 * Created by npapke on 2/22/15.
 */
public class BoardAdapter extends BaseAdapter
{
    public final static String TAG = BoardAdapter.class.getName();

    public BoardAdapter( Context context )
    {
        mContext = context;
    }

    @Override
    public int getCount()
    {
        return mBoardImages.length;
    }

    @Override
    public Object getItem( int position )
    {
        return mBoard.getLvalue( position );
    }

    @Override
    public long getItemId( int position )
    {
        return position;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        if (position == 0) Log.w( TAG, "Drawing position 0");

        ImageView imageView;
        if (convertView == null)
        {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView( mContext );
            imageView.setLayoutParams( new GridView.LayoutParams( 120, 120 ) );
            imageView.setScaleType( ImageView.ScaleType.FIT_XY );
            imageView.setPadding( 0, 0, 0, 0 );
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource( resourceForCell( mBoard.getLvalue( position ) ) );
        mBoardImages[position] = imageView;
        return imageView;
    }

    public void redraw( Board newBoard )
    {
        Board oldBoard = mBoard;
        mBoard = (newBoard == null) ? new Board() : (Board) newBoard.clone();

        for (int i = 0; i < Board.BOARD_LSIZE; ++i)
        {
            if (oldBoard.getLvalue( i ) != mBoard.getLvalue( i ))
                getView( i, mBoardImages[ i ], null );
        }
    }

    private int resourceForCell( BoardValue bv )
    {
        switch (bv)
        {
            case BLACK:
                return R.drawable.cell_black;
            case WHITE:
                return R.drawable.cell_white;
            case EMPTY:
                return R.drawable.cell_empty;

            case VALID_BLACK:
            case VALID_WHITE:
            default:
                return R.drawable.cell_valid;
        }
    }

    private Context mContext;
    private Board mBoard = new Board();
    private ImageView[] mBoardImages = new ImageView[Board.BOARD_LSIZE];
}
