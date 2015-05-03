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
        int numMoves = 0;

        BoardValue otherPlayer = player.otherPlayer();

        for (Position pos : board)
        {
            BoardValue curCell = board.getValue( pos );

            if (curCell == player)
            {
                score++;
                numMoves++;
                protectedScore += board.countProtected( pos );
            }
            else if (curCell == otherPlayer)
            {
                score--;
                numMoves++;
                protectedScore -= board.countProtected( pos );
            }
            else if (board.isValidMove( new Move( player, pos ) ))
            {
                freedom++;
            }
        }

        int finalScore;

        freedom = scale( freedom, 0, 16 );
        score = scale( score, -64, 64 );
        protectedScore = scale( protectedScore, -128, 128 );  // max range is [-256,256]

        if (numMoves < 12)
        {
            finalScore = (freedom * 30) + (protectedScore * 70) + (score * 0);
        }
        else if (numMoves < 50)
        {
            finalScore = (freedom * 20) + (protectedScore * 50) + (score * 30);
        }
        else
        {
            finalScore = (freedom * 0) + (protectedScore * 30) + (score * 70);
        }

        return finalScore;
    }

    private int scale( int value, int min, int max )
    {
        int clipped = Math.min( Math.max( value, min ), max );

        return (clipped - min) * 100 / max;
    }
}
