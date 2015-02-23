/*
 
  $Id: Move.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/**
 * A Move occupies a Position on a board with a Cell
 * @author Norbert Papke
 */

public class Move
{
    
    private Position position;
    private Cell     cell;
    
/** creates a move from a cell and a position
 * @param c cell (player) to create move for
 * @param p position to move to
 */    
    public Move( Cell c, Position p )
    {
        position = p;
        cell = c;
    }
    
/** returns the position of the move
 * @return position of move
 */    
    public Position getPosition()
    {
        return position;
    }
    
/** returns the cell (player) of the move
 * @return cell value of move
 */    
    public Cell getCell()
    {
        return cell;
    }
    
}
