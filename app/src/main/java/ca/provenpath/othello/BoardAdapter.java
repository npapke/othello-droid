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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.BoardValue;
import reactor.util.function.Tuples;

/**
 * Created by npapke on 2/22/15.
 */
public class BoardAdapter extends BaseAdapter {
    public final static String TAG = BoardAdapter.class.getName();

    public BoardAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return Board.BOARD_LSIZE;
    }

    @Override
    public Object getItem(int position) {
        return Tuples.of(mBoard.getLvalue(position), mValidMoveFilter);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        // There is some weirdness with the first cell.  It doesn't render unless ...
        return position == 0 ? Adapter.IGNORE_ITEM_VIEW_TYPE : 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BoardCellView imageView;
        if (convertView == null) {
            // if it's not recycled, initialize some attributes
            imageView = new BoardCellView(mContext);

            int size = Math.min(parent.getHeight(), parent.getWidth()) / 8;

            //Log.v( TAG, String.format( "Position: %d, Parent size: %d x %d, Tile size: %d",
            //    position, parent.getWidth(), parent.getHeight(), size ) );

            imageView.setLayoutParams(new GridView.LayoutParams(size, size));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(0, 0, 0, 0);
            imageView.setAdjustViewBounds(true);
        } else {
            imageView = (BoardCellView) convertView;
        }

        BoardValue bv = mBoard.getLvalue(position);
        imageView.draw(bv, mValidMoveFilter);

        imageView.invalidate();

        boardCellViews[position] = imageView;

        return imageView;
    }


    public void redraw(Board newBoard, BoardValue validMoveFilter) {
        mValidMoveFilter = validMoveFilter;
        mBoard = (newBoard == null) ? new Board() : (Board) newBoard.clone();

        notifyDataSetChanged();
    }

    public void draw(int linear, String valueOf) {
        Log.d(TAG, String.format("%d = %s", linear, valueOf));

        if( boardCellViews[linear] != null) {
            boardCellViews[linear].drawText(valueOf);
        }
    }

    private Context mContext;
    private Board mBoard = new Board();
    private BoardValue mValidMoveFilter;
    private BoardCellView[] boardCellViews = new BoardCellView[Board.BOARD_LSIZE];
}
