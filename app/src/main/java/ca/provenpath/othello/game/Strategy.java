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

public abstract class Strategy
{
    /** Assign a numeric value to the board.
     * @param player the perspective of the player to evaluate the board from
     * @param board the board to evaluate
     * @return the value of the board
     */
    public abstract int determineBoardValue( BoardValue player, Board board );

    /** Determines the value of terminal boards
     * @param player the perspective of the player to evaluate the board from
     * @param board the board to evaluate
     * @return the value of the board
     */
    public final int determineFinalScore( BoardValue player, Board board )
    {
        int score = 0;
        BoardValue otherPlayer = player.otherPlayer();

        for (Position pos : board)
        {
            BoardValue curCell = board.getValue( pos );

            if (curCell == player)
            {
                score++;
            }
            else if (curCell == otherPlayer)
            {
                score--;
            }
        }

        // Scale the value to ensure that terminal boards are more important
        // than non-terminal boards
        return (score > 0) ? (100000 + score * 100) : (-100000 + score * 100);
    }


}
