/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class Strategy
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
        return score * 10000;
    }


}
