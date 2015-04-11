/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;


import android.util.Log;

/**
 * A computer player.  Automatically determines the optimal move for
 * a given board based on a strategy.  The strategy is implemented
 * separately as it is independent from the search algorithm.
 * 
 * @author npapke
 */
public class ComputerPlayer extends Player
{
    public final static String TAG = ComputerPlayer.class.getName();

    /**
     * Best position for current board.
     * <br/>
     * TODO this should be handled as an output/return value of the minimaxAB() method
     */
    Position bestPos;

    public ComputerPlayer( BoardValue color )
    {
        super( color );
    }

    public ComputerPlayer( String serial )
    {
        super( serial );
    }

    /**
     * Make a move on the board.
     * @param board the board to move on
     */
    @Override
    public void makeMove( Board board )
    {
        Assert.notNull( color );
        Assert.notNull( strategy );
        Assert.isTrue( maxDepth > 0 );
        Assert.isTrue( board.hasValidMove( color ) );

        isInterrupted = false;
        bestPos = null;

        int result = minimaxAB( board, color, 0, Integer.MIN_VALUE, Integer.MAX_VALUE );

        Assert.notNull( bestPos );

        Log.i( TAG, "makeMove: " + bestPos + ", value: " + result );

        board.makeMove( new Move( color, bestPos ) );
    }

    @Override
    public void interruptMove()
    {
        isInterrupted = true;
    }


    /**
     * Recursively build game tree.  Find the move that leads to the
     * strongest board for the player.  Utilize alpha-beta pruning
     * to narrow search.
     * 
     * @param board the board to evaluate
     * @param player the player who's move it is
     * @param depth the current recursion depth (in half-moves)
     * @param alpha the alpha maximum
     * @param beta the beta maximum
     * @return the value of board
     */
    private int minimaxAB(
            Board board,
            BoardValue player,
            int depth,
            int alpha,
            int beta )
    {
        if (depth >= maxDepth || isInterrupted)
        {
            return strategy.determineBoardValue( color, board );
        }

        boolean validMoveSeen = false;
        int value;

        if (player == color)
        {
            value = Integer.MIN_VALUE;

            for (Position pos : board)
            {
                Move m = new Move( player, pos );

                if (board.isValidMove( m ))
                {
                    validMoveSeen = true;

                    Board copyOfBoard = (Board) board.clone();
                    copyOfBoard.makeMove( m );

                    int result = minimaxAB( copyOfBoard, player.otherPlayer(), depth + 1, alpha, beta );
                    if (result > value)
                    {
                        value = result;
                        if (depth == 0)
                            bestPos = pos;
                    }

                    alpha = Math.max( value, alpha );
                    if (beta <= alpha)
                        break;
                }
            }
        }
        else
        {
            value = Integer.MAX_VALUE;

            for (Position pos : board)
            {
                Move m = new Move( player, pos );

                if (board.isValidMove( m ))
                {
                    validMoveSeen = true;

                    Board copyOfBoard = (Board) board.clone();
                    copyOfBoard.makeMove( m );

                    value = Math.min( value, minimaxAB( copyOfBoard, player.otherPlayer(), depth + 1, alpha, beta ) );
                    beta = Math.min( value, beta );

                    if (beta <= alpha)
                        break;
                }
            }
        }

        if (!validMoveSeen)
        {
            // player has to pass ...

            if (board.hasValidMove( player.otherPlayer() ))
            {
                value = minimaxAB( board, player.otherPlayer(), depth, alpha, beta );
            }
            else
            {
                // neither player has a valid move.  return the score
                value = strategy.determineFinalScore( player, board );
            }
        }

        return value;
    }


    //
    // ---------------- Bean Pattern ---------------
    //
    protected int maxDepth = 4;


    /**
     * Get the value of maxDepth
     *
     * @return the value of maxDepth
     */
    public int getMaxDepth()
    {
        return maxDepth;
    }


    /**
     * Set the value of maxDepth
     *
     * @param maxDepth new value of maxDepth
     */
    public void setMaxDepth( int maxDepth )
    {
        this.maxDepth = maxDepth;
    }


    protected Strategy strategy = new AdaptiveStrategy();


    /**
     * Get the value of strategy
     *
     * @return the value of strategy
     */
    public Strategy getStrategy()
    {
        return strategy;
    }


    /**
     * Set the value of strategy
     *
     * @param strategy new value of strategy
     */
    public void setStrategy( Strategy strategy )
    {
        this.strategy = strategy;
    }


    private volatile boolean isInterrupted = false;
}
