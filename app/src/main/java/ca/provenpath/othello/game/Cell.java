/*
 
  $Id: Cell.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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
 * a Cell is one square on the game board
 */

public class Cell
{    
    private int content;
    
    public Cell()
    {
        content = Util.EMPTY;
    }
    
    public Cell( int cont )
    {
        content = cont;
    }
    
    public int getContent()
    {
        return content;
    }
    
    public void print()
    {
        char c;
        
        switch (content)
        {
            case Util.EMPTY:        c = '.'; break;
            case Util.WHITE:        c = 'o'; break;
            case Util.BLACK:        c = 'x'; break;
            case Util.VALID_WHITE:  c = 'w'; break;
            case Util.VALID_BLACK:  c = 'b'; break;
            default:                c = '?'; break;
        }
        System.out.print( c );
    }
}
