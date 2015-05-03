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
 * Strength evaluation for a board.
 * <p>This strategy utilizes a weighted cell matrix for
 * non-terminal boards.  Different cells on the board are
 * assigned different values to reflect the fact that they
 * are more desirable.  For instance, a corner is the most
 * desirable cell because once occupied, it cannot be lost.</p>
 *
 * @author npapke
 */
public class StaticStrategy extends Strategy
{
    // These values where generated using my "coth" Othello implementation
    static int weight[] =
            {
                    8125, 133, 1125, 637, 637, 1125, 133, 8125,
                    133, -1250, 152, 189, 189, 152, -1250, 133,
                    1125, 152, 468, 341, 341, 468, 152, 1125,
                    637, 189, 341, 250, 250, 341, 189, 637,
                    637, 189, 341, 250, 250, 341, 189, 637,
                    1125, 152, 468, 341, 341, 468, 152, 1125,
                    133, -1250, 152, 189, 189, 152, -1250, 133,
                    8125, 133, 1125, 637, 637, 1125, 133, 8125
            };


    /** Assign a numeric value to the board utilizing a weighted cell matrix.
     * @param player the perspective of the player to evaluate the board from
     * @param board the board to evaluate
     * @return the value of the board
     */
    @Override
    public int determineBoardValue( BoardValue player, Board board )
    {
        int score = 0;
        BoardValue otherPlayer = player.otherPlayer();

        for (Position pos : board)
        {
            BoardValue curCell = board.getValue( pos );

            if (curCell == player)
            {
                score = score + weight[pos.getLinear()];
            }
            else if (curCell == otherPlayer)
            {
                score = score - weight[pos.getLinear()];
            }
        }

        return score;
    }


}
