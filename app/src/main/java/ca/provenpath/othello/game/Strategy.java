/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
