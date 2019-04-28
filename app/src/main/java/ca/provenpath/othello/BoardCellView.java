package ca.provenpath.othello;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import ca.provenpath.othello.game.BoardValue;

import java.util.Optional;

public class BoardCellView extends android.support.v7.widget.AppCompatImageView {
    public final static String TAG = BoardCellView.class.getName();

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
                            setImageAlpha(0);
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
            }
        }
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
