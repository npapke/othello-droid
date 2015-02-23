/*
 
  $Id: TestDriver.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

//import othello.ejb.*;

public class TestDriver
{
    public static void main( String[] args )
    {
        System.out.println("Hello World!");
        
        try
        {
	/*
	    OthelloEJB oejb = new OthelloEJB();

	    oejb.ejbCreate( new Integer(1), new Integer(1) );

	    Othello.OthelloBoard b = oejb.getBoard();

            System.out.println("EJB passed");
	*/

            MainBoardImpl board = new MainBoardImpl();
            
            board.print();
            
            ComputerPlayer player1 = new ComputerPlayer( Util.WHITE_CELL, 1 );
            ComputerPlayer player2 = new ComputerPlayer( Util.BLACK_CELL, 1 );
            
            board.determineValidMoves(Util.WHITE_CELL);
            board.print();
            player1.makeMove( board );
            board.print();
            
            board.determineValidMoves(Util.BLACK_CELL);
            board.print();
            player2.makeMove( board );
            board.print();
        }
        catch (OthelloException e)
        {
            System.err.println( "Caught OthelloException " + e);
            
            e.printStackTrace();
        }
    }
}
