package ca.provenpath.othello;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * A GridView that has square dimensions
 */
public class SquareGridView extends GridView
{
    public SquareGridView( Context context )
    {
        super( context );
    }

    public SquareGridView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
    }

    public SquareGridView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
    }

    public SquareGridView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );
    }


    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
    {
        int widthSize = MeasureSpec.getSize( widthMeasureSpec );
        int heightSize = MeasureSpec.getSize( heightMeasureSpec );

        int desiredSize = Integer.min( widthSize, heightSize );

        // Lie to the parent and get it to create the exact size we desire
        int fixedMeasure = MeasureSpec.makeMeasureSpec( desiredSize, MeasureSpec.EXACTLY );
        super.onMeasure( fixedMeasure, fixedMeasure );
    }
}
