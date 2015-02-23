/*
 
  $Id: MainBoardImpl.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/** a Board on which a game is played
 *
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 * @see othello.game.BoardImpl
 */
public class MainBoardImpl extends BoardImpl
{
/** Creates new MainBoardImpl 
 *
 *  <p>Calls the parent class's constructor and initializes
 *  the board with the initial game pieces.
 */
    public MainBoardImpl()
    {
        super();
        
        // Set initial game pieces
        setCell( new Move( Util.WHITE_CELL, new Position( 3, 3 ) ) );
        setCell( new Move( Util.BLACK_CELL, new Position( 3, 4 ) ) );
        setCell( new Move( Util.BLACK_CELL, new Position( 4, 3 ) ) );
        setCell( new Move( Util.WHITE_CELL, new Position( 4, 4 ) ) );
    }
}
