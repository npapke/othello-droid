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

package ca.provenpath.othello.game;


import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    public ComputerPlayer( BoardValue color )
    {
        super( color );
    }

    public ComputerPlayer( String serial )
    {
        super( serial );
    }

    public boolean isComputer()
    {
        return true;
    }

    /**
     * Make a move on the board.
     *
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

        Stats stats = new Stats();
        MiniMaxResult result = minimaxAB( board, color, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, stats );

        Assert.notNull( result.getBestMove() );

        Log.i( TAG, "makeMove: " + result.getBestMove() + ", value: " + result.getValue() );
        long duration = stats.duration();
        Log.i( TAG, String.format( "%d boards evaluated in %d ms. %d boards/sec",
                stats.getBoardsEvaluated(), duration,
                duration != 0 ? stats.getBoardsEvaluated() * 1000 / duration : 999999 ) );

        board.makeMove( new Move( color, result.getBestMove() ) );
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
     * @param board  the board to evaluate
     * @param player the player who's move it is
     * @param depth  the current recursion depth (in half-moves)
     * @param alpha  the alpha maximum
     * @param beta   the beta maximum
     * @param stats  performance statistics
     * @return the value of board
     */
    private MiniMaxResult minimaxAB(
            Board board,
            BoardValue player,
            int depth,
            int alpha,
            int beta,
            Stats stats )
    {
        if (depth >= maxDepth || isInterrupted)
        {
            stats.incBoard();
            return new MiniMaxResult( strategy.determineBoardValue( color, board ) );
        }

        boolean validMoveSeen = false;
        int value;
        Position bestPos = new Position();

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

                    int result = minimaxAB( copyOfBoard, player.otherPlayer(), depth + 1, alpha, beta, stats ).getValue();
                    if (result > value)
                    {
                        value = result;
                        bestPos.copy( pos );
                    }

                    alpha = Math.max( value, alpha );
                    if (beta <= alpha)
                        break;
                }

                if (isInterrupted)
                    break;
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

                    int result = minimaxAB( copyOfBoard, player.otherPlayer(), depth + 1, alpha, beta, stats ).getValue();
                    if (result < value)
                    {
                        value = result;
                        bestPos.copy( pos );
                    }

                    beta = Math.min( value, beta );
                    if (beta <= alpha)
                        break;
                }

                if (isInterrupted)
                    break;
            }
        }

        if (!validMoveSeen)
        {
            // player has to pass ...

            if (board.hasValidMove( player.otherPlayer() ))
            {
                value = minimaxAB( board, player.otherPlayer(), depth, alpha, beta, stats ).getValue();
            }
            else
            {
                // neither player has a valid move.  return the score
                value = strategy.determineFinalScore( color, board );
            }
        }

        return new MiniMaxResult( value, bestPos );
    }


    private class MiniMaxResult
    {
        public MiniMaxResult( int value )
        {
            this.value = value;
        }

        public MiniMaxResult( int value, Position bestMove )
        {
            this.value = value;
            this.bestMove = bestMove;
        }

        public int getValue()
        {
            return value;
        }

        public void setValue( int value )
        {
            this.value = value;
        }

        private int value;

        public Position getBestMove()
        {
            return bestMove;
        }

        public void setBestMove( Position bestMove )
        {
            this.bestMove = bestMove;
        }

        private Position bestMove;
    }

    /**
     * Helper for performance statistics.  Thread-safe.
     */
    private class Stats
    {
        private long start = System.currentTimeMillis();
        private AtomicInteger boardsEvaluated = new AtomicInteger( 0 );

        public void incBoard()
        {
            boardsEvaluated.getAndIncrement();
        }

        public int getBoardsEvaluated()
        {
            return boardsEvaluated.get();
        }

        public long duration()
        {
            return System.currentTimeMillis() - start;
        }
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

    private transient volatile boolean isInterrupted = false;
}
