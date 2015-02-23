/*
 
  $Id: Game.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/** Allows a (human) player to play against a computer player.
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 */
public class Game
{
    private Board board;
    
    private Player humanPlayer = null;
    private Player computerPlayer = null;
    
    private String message = "";
    
/** Creates new Game
 * @param color the cell value of the human player
 * @param strategy the expertise of the computer player
 */
    public Game( Cell color, int strategy )
    {
        board = new MainBoardImpl();
        
        humanPlayer = new HumanPlayer( color );
        try
        {
            computerPlayer = new ComputerPlayer( otherPlayer( color ), strategy );
            
            if (color == Util.WHITE_CELL)
            {
                // Computer player goes first
                computerPlayer.makeMove( board );
            }
        }
        catch (GameOverException e)
        {
            // There should always be a valid move!
        }
        catch (OthelloException e)
        {
        }
    }
    
/** Gets the status message for the current game.
 * @return status message
 */    
    public String getMessage()
    {
        return message;
    }
    
    private void setMessage( String msg )
    {
        message = msg;
    }
    
/** Gets the current game board.
 * @return game board
 */    
    public Board getBoard()
    {
        board.determineValidMoves( humanPlayer.getColor() );
        
        return board;
    }
    
/** Makes the specified move for the human player.
 * @param m the move to be made
 * @throws OthelloException if a game error occurs
 * @throws GameOverException if no valid moves remain
 */    
    public void makeMove( Position p ) throws OthelloException, GameOverException
    {
        board.makeMove( new Move( humanPlayer.getColor(), p ) );
        
        // Let the computer player have a go
        makeMove();
    }
    
    /** Make move for computer player
     */
    private void makeMove() throws GameOverException
    {
        boolean validMoveSeen = false;
        
        do
        {
            try
            {
                computerPlayer.makeMove( board );
                validMoveSeen = true;
            }
            catch (OthelloException e)
            {
                if( !validMoveSeen)
                {
                    setMessage( "Computer player doesn't have a move" );
                }
                
                if (!board.hasValidMove( humanPlayer.getColor() ))
                {
                    // neither player has a move
                    setMessage( "Game over" );
                    throw new GameOverException();
                }
            }
        }
        while( ! board.hasValidMove( humanPlayer.getColor() ) );
    }
    
/** Determines the cell value of the other player.
 * @param color the player
 * @return the cell value of the other player
 * @throws OthelloException if color is invalid
 */    
    static public Cell otherPlayer( Cell color ) throws OthelloException
    {
        if (color == Util.WHITE_CELL)
        {
            return Util.BLACK_CELL;
        }
        else if (color == Util.BLACK_CELL)
        {
            return Util.WHITE_CELL;
        }
        else
        {
            throw new OthelloException( "Unexpected player" );
        }
    }
}
