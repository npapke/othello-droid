/*
 
  $Id: BoardImpl.java,v 1.1 2002/10/30 19:59:32 npapke Exp $
  
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
 * BoardImpl implements the Board interface.
 * @author Norbert Papke
 */
public class BoardImpl implements Board, Cloneable
{
    
/** creates a new BoardImpl
 */
    public BoardImpl()
    {
        board = new Cell[Util.DIM][];
        
        for (int i = 0; i < board.length; i++)
        {
            board[i] = new Cell[Util.DIM];
            
            for (int j = 0; j < board[i].length; j++)
            {
                board[i][j] = Util.EMPTY_CELL;
            }
        }
        
        if (moveDeltaTable == null)
        {
            moveDeltaTable = new MoveDeltaTable();
        }
        
        movesOnBoard = 0;
    }
    
/** Get the dimension of the board.  The board is assumed to be square.
 * @return the dimension of the board
 */
    public int getDimension()
    {
        return Util.DIM;
    }
    
/** determines if the specified move is valid on the board
 * @param m the move to check
 * @return true if the move is valid, false otherwise
 */
    public boolean isValidMove( Move m )
    {
        if (isOccupiedCell( m.getPosition() )) return false;
        
        for (int i = 0; i < moveDeltaTable.length; i++)
        {
            if (isFlippable( moveDeltaTable.get(i), m ))
                return true;
        }
        
        return false;
    }
    
    public boolean hasValidMove( Cell player )
    {
        Iterator i = iterator();
        
        while (i.hasNext())
        {
            if (isValidMove( new Move( player, (Position) i.next())))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void determineValidMoves( Cell player )
    {
        Cell validMoveCell;
        
        if (player == Util.WHITE_CELL)
            validMoveCell = Util.VALID_WHITE_CELL;
        else
            validMoveCell = Util.VALID_BLACK_CELL;
        
        Iterator i = iterator();
        
        while (i.hasNext())
        {
            Position pos = (Position) i.next();
            
            if (isValidMove( new Move( player, pos )))
            {
                setCell( new Move( validMoveCell, pos ));
            }
	    else if (! isOccupiedCell( pos ))
	    {
	    	// ensure that old valid moves have been cleared
	    	setCell( new Move( Util.EMPTY_CELL, pos ) );
	    }
        }
    }

/** applies the specied move to the board
 * @param m the move to apply
 * @throws OthelloException the move is invalid
 */
    public void makeMove( Move m ) throws OthelloException
    {
        if (! isValidMove(m))
        {
            throw new OthelloException( "Invalid Move" );
        }
        
        setCell( m );
        
        for (int i = 0; i < moveDeltaTable.length; i++)
        {
            MoveDelta md = moveDeltaTable.get( i );
            
            if (isFlippable( md, m ))
            {
                Cell thisPlayer = m.getCell();
                Position curPos = m.getPosition();
                int curX = curPos.x();
                int curY = curPos.y();
                
                boolean done = false;
                
                while (! done)
                {
                    curX += md.dx;
                    curY += md.dy;
                    
                    if (curX < 0 || curX >= Util.DIM ||
                    curY < 0 || curY >= Util.DIM)
                    {
                        // past the edge of the board
                        throw new OthelloException( "Off board" );
                    }
                    
                    Cell curCell = getCell( curX, curY );
                    
                    if (curCell == thisPlayer)
                    {
                        // finish when reaching own piece
                        done = true;
                    }
                    else
                    {
                        if (isEmptyCell( new Position( curX, curY )))
                        {
                            // Internal consistency check.  We are flipping
                            // existing pieces not occupying empty cells.
                            throw new OthelloException( "Position not occupied" );
                        }
                        
                        setCell( curX, curY, thisPlayer );
                    }
                }
            }
        }
        
        ++movesOnBoard;
    }
    
    
    public Iterator iterator()
    {
        return new PositionIterator();
    }
    
    
    public void print()
    {
        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board[i].length; j++)
            {
                // TODO - this doesn't quite match the iteration. X-Y are reversed
                board[j][i].print();
            }
            
            System.out.println();
        }
    }
    
    public Cell getCell( Position pos )
    {
        return board[pos.x()] [pos.y()];
    }
    
    
    public Cell getCell( int x, int y )
    {
        return board[x] [y];
    }

    public int countCells(Cell player)
    {
        int count = 0;
        
        for (int i = 0; i < board.length; i++)
        {
            for (int j = 0; j < board[i].length; j++)
            {
                if (board[i][j] == player)
                {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    public Object clone()
    {
        BoardImpl clone = new BoardImpl();
        
        for (int i = 0; i < Util.DIM; i++)
        {
            System.arraycopy( this.board[i], 0, clone.board[i], 0, Util.DIM );
        }
        
        clone.movesOnBoard = this.movesOnBoard;
        
        return clone;
    }
    
    // ---------------------------------------------------------
    
    
    protected void setCell( int x, int y, Cell c )
    {
        board [x] [y] = c;
    }
    
    protected void setCell( Move m )
    {
        Position p = m.getPosition();
        
        board [p.x()] [p.y()] = m.getCell();
    }
    
    
/** return true if the cell is occupied
 *
 * Test explicitly against white or black occupation
 * to correctly handle cells that are marked as valid moves.
 * @return true if cell is occupied, false otherwise
 * @param p position on board to check
 */
    protected boolean isOccupiedCell( Position p )
    {
        Cell c = getCell(p.x(), p.y());
        
        return (c == Util.WHITE_CELL ) || (c == Util.BLACK_CELL );
    }
    
    protected boolean isEmptyCell( Position p )
    {
        return ! isOccupiedCell( p );
    }
    
    
    
    
    // ---------------------------------------------------------
    
    
    /**
     * Determine if there are any flippable pieces for the Move
     * in the specified direction
     */
    private boolean isFlippable( MoveDelta md, Move m )
    {
        Cell thisPlayer = m.getCell();
        Position curPos = m.getPosition();
        int curX = curPos.x();
        int curY = curPos.y();
        int	otherPieces = 0;
        
        for(;;)
        {
            curX += md.dx;
            curY += md.dy;
            
            if (curX < 0 || curX >= Util.DIM ||
            curY < 0 || curY >= Util.DIM)
            {
                // past the edge of the board
                return false;
            }
            
            Cell curCell = getCell( curX,curY );
            
            if (curCell == thisPlayer)
            {
                if (otherPieces > 0)
                {
                    // need run of other players pieces followed by own player
                    return true;
                }
                else
                {
                    return false;
                }
            }
            
            if (isEmptyCell( new Position( curX, curY )))
            {
                return false;
            }
            
            ++otherPieces;
        }
    }
    
    private Cell[][] board;
    private int movesOnBoard = 0;
    
    
    // ----------------------------------------------------------------
    
    
/**
 * MoveDelta describes the directions of valid moves
 */
    private class MoveDelta
    {
        public int		dx;
        public int		dy;
        public int		axis;
        
        public MoveDelta( int dx0, int dy0, int axis0 )
        {
            dx = dx0;
            dy = dy0;
            axis = axis0;
        }
    }
    
        /**
         * MoveDeltaTable is the collection of all MoveDeltas
         */
    private class MoveDeltaTable
    {
        public final int length = 8;
        private MoveDelta[] aMoveDelta;
        
        public MoveDeltaTable()
        {
            aMoveDelta = new MoveDelta[length];
            
            aMoveDelta[0] = new MoveDelta( 0, -1, 0 );
            aMoveDelta[1] = new MoveDelta( 0,  1, 0 );
            aMoveDelta[2] = new MoveDelta( 1, -1, 1 );
            aMoveDelta[3] = new MoveDelta( 1,  0, 2 );
            aMoveDelta[4] = new MoveDelta( 1,  1, 3 );
            aMoveDelta[5] = new MoveDelta(-1, -1, 3 );
            aMoveDelta[6] = new MoveDelta(-1,  0, 2 );
            aMoveDelta[7] = new MoveDelta(-1,  1, 1 );
        }
        
        public MoveDelta get( int i )
        {
            return aMoveDelta[i];
        }
    }
    
    private static MoveDeltaTable moveDeltaTable;
    
    
    // ------------------------------------------------------------------
    
    private class PositionIterator implements java.util.Iterator
    {
        private int curX = 0;
        private int curY = 0;
        
        public boolean hasNext()
        {
            return curX < Util.DIM || curY < Util.DIM;
        }
        
        public java.lang.Object next()
        {
            
            if (curX >= Util.DIM && curY >= Util.DIM)
            {
                //                throw new NoSuchElementException();
            }
            
            Position returnVal = new Position( curX, curY );
            
            if (++curX >= Util.DIM)
            {
                curX = 0;
                
                if (++curY >= Util.DIM)
                {
                    // no more positions left
                    curX = Util.DIM;
                    curY = Util.DIM;
                }
            }
            
            return returnVal;
        }
        
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}

