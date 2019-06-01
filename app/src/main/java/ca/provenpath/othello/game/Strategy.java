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
    public final static int WIN_BASE = Integer.MAX_VALUE - Board.BOARD_LSIZE;
    public final static int LOSS_BASE = Integer.MIN_VALUE;

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
        int otherScore = 0;
        BoardValue otherPlayer = player.otherPlayer();

        for (int pos = 0; Position.isValid( pos ); pos++)
        {
            BoardValue curCell = board.getLvalue( pos );

            if (curCell == player)
            {
                score++;
            }
            else if (curCell == otherPlayer)
            {
                ++otherScore;
            }
        }

        // Translate the value to ensure that terminal boards are more important
        // than non-terminal boards
        return (score >= otherScore) ? (WIN_BASE + score)  : (LOSS_BASE + score);
    }

    public static int normalizeScore(int score) {

        if (score >= WIN_BASE) {
            return score - WIN_BASE;
        } else if (score <= (LOSS_BASE + Board.BOARD_LSIZE)) {
            return -(score - LOSS_BASE);
        } else {
            return score;
        }
    }

}
