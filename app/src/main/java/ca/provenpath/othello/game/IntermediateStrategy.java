/*
 
  $Id: IntermediateStrategy.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
  eOthello, an implementation of the Othello board game using
  a JSP front end with an EJB backend.

  Copyright 2001 Norbert Papke (npapke@acm.org). All Rights Reserved.

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  
*/


package ca.provenpath.othello.game;

import java.util.Iterator;

/** Strategy for an intermediate-level player.
 *
 * <p>This strategy utilizes a weighted cell matrix for
 * non-terminal boards.  Different cells on the board are
 * assigned different values to reflect the fact that they
 * are more desirable.  For instance, a corner is the most
 * desirable cell because once occupied, it cannot be lost.</p>
 *
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 */
public class IntermediateStrategy extends Strategy
{
    // These values where generated using my "coth" Othello implementation
    static int weight[][] =
    {
	{ 8125,  133, 1125,  637, 637, 1125,  133, 8125, },
	{  133,-1250,  152,  189, 189,  152,-1250,  133, },
	{ 1125,  152,  468,  341, 341,  468,  152, 1125, },
	{  637,  189,  341,  250, 250,  341,  189,  637, },
	{  637,  189,  341,  250, 250,  341,  189,  637, },
	{ 1125,  152,  468,  341, 341,  468,  152, 1125, },
	{  133,-1250,  152,  189, 189,  152,-1250,  133, },
	{ 8125,  133, 1125,  637, 637, 1125,  133, 8125, }
    };

    
/** Creates new IntermediateStrategy
 * @param player the color of the player's game pieces
 */
    public IntermediateStrategy( Cell player )
    {
        super( player, 3 );
    }
    
/** Creates a new instance
 * @param color the color of the player's game pieces
 * @param depth the recursion depth (or look-ahead level)
 */    
    public IntermediateStrategy( Cell color, int depth )
    {
    	super( color, depth );
    }

/** Assign a numberic value to the board utilizing a weighted cell matrix.
 * @param board the board to evaluate
 * @return the value of the board
 * @throws OthelloException if a game error occurs
 */    
    public int determineBoardValue( Board board ) throws OthelloException
    {
        int score = 0;
        Cell otherPlayer = Game.otherPlayer( player );
        
        Iterator positionIterator = board.iterator();
        
        while (positionIterator.hasNext())
        {
            Position pos = (Position) positionIterator.next();
            Cell curCell = board.getCell( pos );
            
            if (curCell == player)
            {
                score = score + weight[pos.x()][pos.y()];
            }
            else if (curCell == otherPlayer)
            {
                score = score - weight[pos.x()][pos.y()];
            }
        }
        
        return score;
    }
}
