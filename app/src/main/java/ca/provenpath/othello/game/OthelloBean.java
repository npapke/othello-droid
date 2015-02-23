/*
 
  $Id: OthelloBean.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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


import android.util.Log;

import static android.util.Log.*;

/** The OthelloBean class implements a facade for the
 * Othello classes to simplify access from JSPs.
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 */
public class OthelloBean
{    
    /** logger */
    private static final String TAG = OthelloBean.class.getName();

    private Game m_game;
    private Board m_board;
        
/** Construct OthelloBean
 */    
    public OthelloBean()
    {
    }
    
/** Starts a new game by creating an Othello EJB
 * @param difficulty The difficulty (or expertise) of the computer player.  Should be one of "beginner", "intermediate" or "expert"
 * @param color The color of the human player.  Should be one of "white" or "black".
 * @throws Exception All EJB or RMI exceptions are rethrown as Exception.
 */    
    public void startGame( String difficulty, String color ) throws OthelloException
    {
        int diff;
        Cell player;
        
        if (difficulty.equalsIgnoreCase("beginner"))
            diff = Util.BEGINNER;
        else if (difficulty.equalsIgnoreCase("expert"))
            diff = Util.EXPERT;
        else
            diff = Util.INTERMEDIATE;
        
        if (color.equalsIgnoreCase("black"))
            player = Util.BLACK_CELL;
        else
            player = Util.WHITE_CELL;
        
  	    if (m_game != null)
        {
            w( TAG, "Restarting game" );
        } 
        m_game = new Game( player, diff );
        m_board = null;
    }
    
    
/** Determines if a game is currently in progress.
 *
 * <p>A game is in progress if it is configured and the last
 * move has not yet been made.</p>
 * @return true if game is in progress, false otherwise
 */    
    public boolean isGameInProgress()
    {
        return (m_game != null);
    }

/** Retrieves and caches the game board from the Othello EJB.
 * @see #getCell
 * @see #getWhiteScore
 * @see #getBlackScore
 * @see #getMessage
 * @throws OthelloException 
 */    
    public void getBoard() throws OthelloException
    {
    	if (m_game == null)
    	{
            throw new OthelloException( "Othello not initialized." );
        }

        if (m_board == null)
        {
            m_board = m_game.getBoard();
        }
    }

/** Retrieves the content of the specified cell from the game board.
 *
 * <p>getBoard() must be called prior to invoking this method.</p>
 * @see #getBoard
 * @see othello.game.Util
 * @param x The x coordinate of the cell
 * @param y The y coordinate of the cell
 * @return the cell contents
 * @throws Exception if getBoard has not been called
 */    
    public int getCell( int x, int y ) throws OthelloException
    {
        getBoard();
        
    	return m_board.getCell( x, y ).getContent();
    }

/** Retrieves the score of the white player.
 *
 * <p>getBoard() must be called prior to invoking this method.</p>
 * @see #getBoard
 * @return the white player's score
 * @throws Exception if getBoard has not been called
 */    
    public int getWhiteScore() throws OthelloException
    {
        getBoard();
        
    	return m_board.countCells( Util.WHITE_CELL );
    }

 /** Retrieves the score of the black player.
  *
  * <p>getBoard() must be called prior to invoking this method.</p>
  * @see #getBoard
  * @return the black player's score
  * @throws Exception if getBoard has not been called
 */    
   public int getBlackScore() throws OthelloException
    {
        getBoard();
        
    	return m_board.countCells( Util.BLACK_CELL );
    }
    
/** Retrieves the status message from the Othello EJB.
 *
 * <p>getBoard() must be called prior to invoking this method.</p>
 * @see #getBoard
 * @return the status message
 * @throws Exception if getBoard has not been called
 */    
    public String getMessage() throws Exception
    {
    	if (m_game == null)
        {
            throw new Exception( "Board not initialized." );
        }

    	return m_game.getMessage();
    }

/** Makes a move for the human player on the game board.
 * @param x the x-coordinate of the move
 * @param y the y-coordinate of the move
 * @return true if the move could be made, false otherwise
 */    
    public boolean makeMove( String x, String y ) throws OthelloException
    {
        boolean validMove = false;
        
        // A new move invalidates the board
        m_board = null;
        
        try
        {
            m_game.makeMove( 
                new Position( new Integer( x ).intValue(), new Integer( y ).intValue() ) );
            validMove = true;
        }
        catch (GameOverException e)
        {
        }
        catch (OthelloException e)
        {
        }
        
        return validMove;
    }

/** Gets the current version of the Othello EJB.
 * @return the version identifier
 */    
    public String getVersion()
    {
        return "Othello 1.0";
    }
    
}
