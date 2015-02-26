package ca.provenpath.othello;

import android.content.Context;
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
    public BoardAdapter( Context context, Board board )
    {
        mContext = context;
        mBoard = board;
    }

    @Override
    public int getCount()
    {
        return Board.BOARD_SIZE * Board.BOARD_SIZE;
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
        ImageView imageView;
        if (convertView == null)
        {  // if it's not recycled, initialize some attributes
            imageView = new ImageView( mContext );
            imageView.setLayoutParams( new GridView.LayoutParams( 85, 85 ) );
            imageView.setScaleType( ImageView.ScaleType.FIT_XY );
            imageView.setPadding( 1, 1, 1, 1 );
        } else
        {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource( resourceForCell( mBoard.getLvalue( position ) ) );
        return imageView;
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
    private Board mBoard;
}
