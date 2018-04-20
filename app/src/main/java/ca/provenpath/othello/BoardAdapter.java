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
            int size = 110; // Math.min( parent.getHeight(), parent.getWidth() ) / 8;

            // Log.v( TAG, String.format( "Parent size: %d x %d", parent.getWidth(), parent.getHeight() ) );

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
        boolean isAnimated = !bv.equals( mOldBoard.getLvalue( position ) );

        if (isAnimated)
        {
            switch (bv)
            {
                case BLACK:
                case WHITE:
                {
                    imageView.setBackgroundResource( resourceForCell( mOldBoard.getLvalue( position ) ) );

                    // "Fade in" animation for changed tiles
                    imageView.setImageAlpha( 0 );
                    mAnimators[ position ] = ObjectAnimator.ofInt( imageView, "imageAlpha", 0, 255 );
                    mAnimators[ position ].setAutoCancel( true );

                    boolean isLastMove = (mBoard.getLastMove() != null)
                        && (position == mBoard.getLastMove().getPosition().getLinear());
                    if (isLastMove)
                    {
                        mAnimators[ position ].setDuration( 250 );
                        mAnimators[ position ].setRepeatCount( 4 );
                        mAnimators[ position ].setRepeatMode( ObjectAnimator.REVERSE );
                    }
                    else
                    {
                        mAnimators[ position ].setStartDelay( 500 );
                        mAnimators[ position ].setDuration( 750 );
                    }

                    mAnimators[ position ].setStartDelay( 100 );
                    mAnimators[ position ].start();

                    break;
                }

                case VALID_BLACK:
                case VALID_WHITE:
                case VALID_BOTH:
                {
                    imageView.setBackgroundResource( resourceForCell( BoardValue.EMPTY ) );

                    // "Fade in" animation for changed tiles
                    imageView.setImageAlpha( 0 );
                    mAnimators[ position ] = ObjectAnimator.ofInt( imageView, "imageAlpha", 0, 255 );
                    mAnimators[ position ].setAutoCancel( true );

                    mAnimators[ position ].setStartDelay( 100 );
                    mAnimators[ position ].setDuration( 100 );
                    mAnimators[ position ].setStartDelay( 1500 );

                    mAnimators[ position ].start();

                    break;
                }
            }
        }

        return imageView;
    }

    public void redraw( Board newBoard, BoardValue validMoveFilter )
    {
        mOldBoard = mBoard;
        mValidMoveFilter = validMoveFilter;
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
            case VALID_BOTH:
            default:
                return (((bv == BoardValue.VALID_BOTH) && (mValidMoveFilter != BoardValue.EMPTY)) || (bv == mValidMoveFilter))
                        ? R.drawable.cell_valid
                        : R.drawable.cell_empty;
        }
    }

    private Context mContext;
    private Board mBoard = new Board();
    private Board mOldBoard = new Board();
    private ImageView[] mBoardImages = new ImageView[Board.BOARD_LSIZE];
    private ObjectAnimator[] mAnimators = new ObjectAnimator[Board.BOARD_LSIZE];
    private BoardValue mValidMoveFilter;
}
