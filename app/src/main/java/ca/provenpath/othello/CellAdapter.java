package ca.provenpath.othello;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import ca.provenpath.othello.game.Board;

/**
 * Created by npapke on 2/22/15.
 */
public class CellAdapter extends BaseAdapter
{
    public CellAdapter( Context context )
    {
        mContext = context;
    }

    @Override
    public int getCount()
    {
        return Board.BOARD_SIZE * Board.BOARD_SIZE;
    }

    @Override
    public Object getItem( int position )
    {
        return null;
    }

    @Override
    public long getItemId( int position )
    {
        return 0;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        return null;
    }

    private Context mContext;
}
