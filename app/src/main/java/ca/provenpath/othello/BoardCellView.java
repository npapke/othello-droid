package ca.provenpath.othello;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.CpuUsageInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import ca.provenpath.othello.game.BoardValue;

import java.util.Optional;

public class BoardCellView extends android.support.v7.widget.AppCompatImageView {
    public final static String TAG = BoardCellView.class.getName();

    private static class TextDrawable extends Drawable {

        Paint paint = new Paint();
        String text;

        public TextDrawable(String text) {
            paint.setARGB(255, 128, 128, 128);
            paint.setAntiAlias(true);
            this.text = text;

        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Log.v(TAG, String.format("Drawing on canvas. w=%d, h%d", getBounds().width(), getBounds().height()));

            paint.setColor(Color.DKGRAY);
            paint.setTextSize(50);
            canvas.drawText(text, getBounds().width() * 0.1f, getBounds().height() * 0.3f, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

    Optional<Integer> lastResId = Optional.empty();

    public BoardCellView(Context context) {
        super(context);
    }

    public BoardCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoardCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void draw(BoardValue boardValue, BoardValue moveFilter) {

        setForeground(null);
        int resId = resourceForCell(boardValue, moveFilter);
        if (!lastResId.isPresent() || (lastResId.isPresent() && lastResId.get() != resId)) {

            setImageResource(resId);

            if (lastResId.isPresent()) {
                ObjectAnimator animator = ObjectAnimator.ofInt(this, "imageAlpha", 0, 255);
                animator.setAutoCancel(true);

                switch (boardValue) {
                    case BLACK:
                    case WHITE: {
                        // "Fade in" animation for changed tiles
                        boolean isLastMove = lastResId
                                .map(id -> id == R.drawable.ic_cell_empty || id == R.drawable.ic_cell_valid)
                                .orElse(false);
                        if (isLastMove) {
                            // user placed this piece
                            setBackgroundResource(R.drawable.ic_cell_empty);
                            animator.setDuration(250);
                            animator.setRepeatCount(4);
                            animator.setRepeatMode(ObjectAnimator.REVERSE);
                        } else {
                            // this piece was flipped
                            setBackgroundResource(lastResId.get());
                            animator.setStartDelay(500);
                            animator.setDuration(1500);
                        }

                        break;
                    }

                    default: {
                        // valid moves
                        setBackgroundResource(R.drawable.ic_cell_empty);
                        setImageAlpha(0);

                        animator.setStartDelay(1000);
                        animator.setDuration(3000);
                        break;
                    }
                }

                animator.start();
                //Log.d(TAG, "Animator started");
            }

            lastResId = Optional.of(resId);
        }
    }

    public void drawText(String text) {
        Log.v(TAG, "drawText: " + text);

        Drawable foreground = new TextDrawable(text);
        setForeground(foreground);
    }

    private void draw() {
//        LayerDrawable layes = new LayerDrawable();
    }

    private int resourceForCell(BoardValue bv, BoardValue moveFilter) {
        switch (bv) {
            case BLACK:
                return R.drawable.ic_cell_black;
            case WHITE:
                return R.drawable.ic_cell_white;
            case EMPTY:
                return R.drawable.ic_cell_empty;

            case VALID_BLACK:
            case VALID_WHITE:
            case VALID_BOTH:
            default:
                return (((bv == BoardValue.VALID_BOTH) && (moveFilter != BoardValue.EMPTY)) || (bv == moveFilter))
                        ? R.drawable.ic_cell_valid
                        : R.drawable.ic_cell_empty;
        }
    }

}
