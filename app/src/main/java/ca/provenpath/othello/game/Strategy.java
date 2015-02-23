/*
 
  $Id: Strategy.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/** The abstraction for a computer player's strategy.  A strategy
 * assigns numeric values to game boards and determines the recursion
 * depth.
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 */
public abstract class Strategy
{
    private int max_depth;
/** The color of the player's game pieces.
 */    
    protected Cell player;
    
/** Creates new Strategy
 * @param color the color of the player's game pieces
 * @param depth the recursion level or look-ahead
 */
    public Strategy( Cell color, int depth )
    {
        player = color;
        max_depth = depth;
    }
    
/** Returns the recursion level
 * @return the recursion level
 */    
    public final int getRecursionDepth()
    {
        return max_depth;
    }
    
/** Returns a numeric value describing the <i>quality</i> of the
 * board.  The higher the returned value, the more valuable the
 * board.
 * @param board the board to evaluate
 * @return the value of the board
 * @throws OthelloException if a game error occurs
 */    
    public abstract int determineBoardValue( Board board ) throws OthelloException;

/** Determines the value of terminal boards
 * @param board the board to evaluate
 * @return the value of the board
 * @throws OthelloException if a game error occurs
 */    
    public final int determineFinalScore( Board board ) throws OthelloException
    {
        int score = 0;
        Cell otherPlayer = Game.otherPlayer( player );
        
        Iterator positionIterator = board.iterator();
        
        while (positionIterator.hasNext())
        {
            Cell curCell = board.getCell((Position) positionIterator.next());
            
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
