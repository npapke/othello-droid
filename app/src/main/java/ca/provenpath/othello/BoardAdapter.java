package ca.provenpath.othello;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
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
    public int getItemViewType( int position )
    {
        return Adapter.IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public boolean hasStableIds()
    {
        return true;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        ImageView imageView;
        if (convertView == null)
        {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView( mContext );

            // TODO determine size dynamically
            int size =  110; // Math.min( parent.getHeight(), parent.getWidth() ) / 8;

            Log.d( TAG, String.format( "Parent size: %d x %d", parent.getWidth(), parent.getHeight() ) );

            imageView.setLayoutParams( new GridView.LayoutParams( size, size ) );
            imageView.setScaleType( ImageView.ScaleType.FIT_XY );
            imageView.setPadding( 0, 0, 0, 0 );
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource( resourceForCell( mBoard.getLvalue( position ) ) );
        mBoardImages[position] = imageView;

        BoardValue bv = mBoard.getLvalue( position );
        boolean isAnimated = ! bv.equals( mOldBoard.getLvalue( position ) )
                && ((bv == BoardValue.BLACK) || (bv == BoardValue.WHITE));

        if ( isAnimated )
        {
            // "Expand" animation for changed tiles
            ObjectAnimator animatorX = ObjectAnimator.ofFloat( imageView, "scaleX", 0f, 1f );
            animatorX.setDuration( 250 );
            animatorX.start();

            ObjectAnimator animatorY = ObjectAnimator.ofFloat( imageView, "scaleY", 0f, 1f );
            animatorY.setDuration( 250 );
            animatorY.start();
        }

        return imageView;
    }

    public void redraw( Board newBoard )
    {
        mOldBoard = mBoard;
        mBoard = (newBoard == null) ? new Board() : (Board) newBoard.clone();

        notifyDataSetChanged();
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
    private Board mOldBoard = new Board();
    private ImageView[] mBoardImages = new ImageView[Board.BOARD_LSIZE];
}
