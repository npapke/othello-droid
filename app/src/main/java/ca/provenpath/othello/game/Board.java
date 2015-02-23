/*
 
  $Id: Board.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/**
 * The Board interface defines how an othello game board is accessed.
 */

public interface Board extends Cloneable
{

    public int getDimension();

    /**
     * returns true if the move is valid according to the
     * game rules
     */
    public boolean isValidMove( Move m );
    public boolean hasValidMove( Cell player );
    public void determineValidMoves( Cell player );
    public int countCells( Cell player );

    public void makeMove( Move m ) throws OthelloException;

    public Cell getCell( int x, int y );
    public Cell getCell( Position pos );

    public Iterator iterator();
    
    public Object clone();

}
