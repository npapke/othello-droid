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

/**
 * A strategy that considers multiple board characterizations:
 * <ul>
 *     <li>score</li>
 *     <li>number of valid moves</li>
 *     <li>how difficult to flip pieces are</li>
 * </ul>
 * The weight of these characterizations changes through the course of the game.
 *
 * Created by npapke on 3/30/15.
 */
public class AdaptiveStrategy extends Strategy
{
    @Override
    public int determineBoardValue( BoardValue player, Board board )
    {
        int score = 0;
        int protectedScore = 0;
        int freedom = 0;
        int threats = 0;
        int numMoves = 0;

        BoardValue otherPlayer = player.otherPlayer();

        for (Position pos : board)
        {
            BoardValue curCell = board.getValue( pos );

            if (curCell == player)
            {
                score++;
                numMoves++;
                protectedScore += calcProtectedScore( board.countProtected( pos ));
            }
            else if (curCell == otherPlayer)
            {
                score--;
                numMoves++;
                protectedScore -= calcProtectedScore( board.countProtected( pos ));
            }
            else if (board.isValidMove( new Move( player, pos ) ))
            {
                freedom++;
            }
            else if (board.isValidMove( new Move( otherPlayer, pos ) ))
            {
                threats++;
            }
        }

        int finalScore;

        freedom = scale( freedom, 0, 16 );  // a large max is not realistic
        threats = scale( threats, 0, 16 );  // a large max is not realistic

        // Allow scores to go negative, i.e., [0,100] -> [-100,100]
        score = scale2( score, -numMoves, numMoves );
        protectedScore = scale2( protectedScore, -numMoves * 10, numMoves * 10 );

        /*
         * The following is conjecture
         */
        if (numMoves < 16)
        {
            finalScore = (freedom * 30) + (protectedScore * 40) + (score * 40) - (threats * 10);
        }
        else if (numMoves < 50)
        {
            finalScore = (freedom * 20) + (protectedScore * 70) + (score * 30) - (threats * 20);
        }
        else if (numMoves < 60)
        {
            finalScore = (freedom * 0) + (protectedScore * 30) + (score * 70) - (threats * 20);
        }
        else if (numMoves < 64)
        {
            finalScore = (freedom * 0) + (protectedScore * 0) + (score * 100) - (threats * 0);
        }
        else
        {
            finalScore = score * 100;
        }

        return finalScore;
    }

    /**
     * Scales input value to [0,100].  input value is considered to be in [min,max].
     * @param value input value
     * @param min lower bound for value
     * @param max upper bound for value
     * @return scaled value
     */
    private int scale( int value, int min, int max )
    {
        int clipped = Math.min( Math.max( value, min ), max );

        return (clipped - min) * 100 / (max - min);
    }

    /**
     * Scales input value to [-100,100].  input value is considered to be in [min,max].
     * @param value input value
     * @param min lower bound for value
     * @param max upper bound for value
     * @return scaled value
     */
    private int scale2( int value, int min, int max )
    {
        int clipped = Math.min( Math.max( value, min ), max );

        return ((clipped - min) * 200 / (max - min)) - 100;
    }

    /**
     * Scores a protected cell count
     * @param count protected axis count
     * @return
     */
    private int calcProtectedScore( int count )
    {
        switch (count)
        {
            case 4: return 10;
            case 3: return 2;
            default: return 0;
        }
    }
}
