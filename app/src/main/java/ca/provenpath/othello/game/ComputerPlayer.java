/*
 
  $Id: ComputerPlayer.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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

/** This class implements the computer player.
 * @author Norbert Papke
 * @version $Revision: 1.1 $
 */
public class ComputerPlayer extends Player
{
    Strategy strategy;
    int max_depth;
    
    
/** Creates new ComputerPlayer
 * @param color the color of the computer player's game pieces
 * @param expertise the level of difficulty of the computer player's strategy
 */
    public ComputerPlayer( Cell color, int expertise )
    {
        super( color );
        
        // TODO: A factory class may be a bit cleaner for this ...
        switch( expertise )
        {
            case Util.EXPERT:
                strategy = new ExpertStrategy(color);
                break;
                
            case Util.INTERMEDIATE:
                strategy = new IntermediateStrategy(color);
                break;
                
            case Util.BEGINNER:
            default:
                strategy = new BeginnerStrategy(color);
                break;
        }
        
        max_depth = strategy.getRecursionDepth();
    }
    
/** Make a move on the specified game board.
 * @param board the board to make the move on
 * @throws OthelloException if a game error (such as no possible valid move) occurs
 */    
    public void makeMove( Board board ) throws OthelloException
    {
        if (board.hasValidMove(color))
        {
            Move move = determineBestMove( board );
            board.makeMove( move );
        }
        else
        {
            throw new OthelloException( "No valid move" );
        }
    }
    
    
    private Position bestPos; // the best possible move
    
    private Move determineBestMove( Board board ) throws OthelloException
    {
        Position dummyPos = new Position( 0, 0 );
        
        int result = minimaxAB( board, color, 0, (int)Integer.MIN_VALUE, (int)Integer.MAX_VALUE);
        
        return new Move( color, bestPos );
    }


    /*
     * Build game tree utilizing alpha-beta pruning
     */
    private int minimaxAB
    (
    Board board,
    Cell player,
    int depth,
    int alpha,
    int beta
    ) throws OthelloException
    {
        if (depth >= max_depth)
        {
            return strategy.determineBoardValue( board );
        }
        
        boolean validMoveSeen = false;
        
        Iterator positionIterator = board.iterator();
        
        while (positionIterator.hasNext() && (alpha <= beta))
        {
            Position pos = (Position) positionIterator.next();
            Move m = new Move( player, pos);
            
            if (board.isValidMove( m ))
            {
                Board copyOfBoard = (Board) board.clone();
                
                copyOfBoard.makeMove( m );
                
                int result = minimaxAB( copyOfBoard,
                Game.otherPlayer( player ), depth + 1, alpha, beta );
                
                if (player == color)
                {
                    if (result > alpha)
                    {
                        alpha = result;
                        
                        if (depth == 0)
                        {
                            bestPos = pos;
                        }
                    }
                }
                else
                {
                    if (result < beta)
                    {
                        beta = result;
                    }
                }
                
                validMoveSeen = true;
            }
        }
        
        if (!validMoveSeen)
        {
            // player has to pass ...

	    if (board.hasValidMove( Game.otherPlayer( player )))
	    {
                 return minimaxAB( board, Game.otherPlayer( player ), depth, alpha, beta );
	    }
	    else
	    {
                // neither player has a valid move.  return the score
	         return strategy.determineFinalScore( board );
	    }
        }
        
        if (player == color)
        {
            return alpha;
        }
        else
        {
            return beta;
        }
    }
    
}
