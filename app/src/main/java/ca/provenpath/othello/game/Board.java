/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;

import android.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;

import static ca.provenpath.othello.game.BoardValue.*;

/**
 * A game board.
 * @author npapke
 */
public class Board implements Cloneable, Iterable<Position>
{
    public final static String TAG = Board.class.getName();

    /**
     * Size of the board.  Board is square.
     */
    public static final int BOARD_SIZE = 8;

    /**
     * The board cells.  A linear array is more convenient
     * (and efficient) than a grid.
     */
    private BoardValue[] board = new BoardValue[ BOARD_SIZE * BOARD_SIZE ];

    /**
     * Linear offset for adjacent cells.
     */
    private int adjacentOffsetTable[] =
    {
        1,
        -1,
        BOARD_SIZE,
        -BOARD_SIZE,
        BOARD_SIZE + 1,
        BOARD_SIZE - 1,
        -BOARD_SIZE + 1,
        -BOARD_SIZE - 1
    };


    /**
     * Construct a board and initialize the cells for a game start.
     */
    public Board()
    {
        for (int i = 0; i < board.length; i++)
        {
            board[i] = EMPTY;
        }

        board[Position.getLinear( 3, 3 )] = WHITE;
        board[Position.getLinear( 4, 4 )] = WHITE;
        board[Position.getLinear( 3, 4 )] = BLACK;
        board[Position.getLinear( 4, 3 )] = BLACK;
    }


    /**
     * Construct a board and initialize the cells for a game start.
     * @param other the board to copy
     */
    public Board( Board other )
    {
        for (int i = 0; i < board.length; i++)
        {
            this.board[i] = other.board[i];
        }
    }


    /**
     * Create a copy of the board.  Expose the cloneable interface.
     * @return copy of board
     */
    @Override
    protected Object clone()
    {
        return new Board( this );
    }


    /**
     * Equality check.  Boards are equal iff all cells are equal.
     * @param obj
     * @return
     */
    @Override
    public boolean equals( Object obj )
    {
        try
        {
            Board other = (Board) obj;

            for (int i = 0; i < this.board.length; i++)
            {
                if (this.board[i] != other.board[i])
                {
                    return false;
                }
            }

            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    /**
     * Hashcode.
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        int hash = 3;
        for (int i = 0; i < this.board.length; i++)
        {
            hash += this.board[i].ordinal() * (i + 1);
        }

        return hash;
    }


    /** 
     * Determine if the specified move is valid on the board.
     * @param m the move to check
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove( Move m )
    {
        if (isOccupiedBoardValue( m.getPosition() ))
        {
            return false;
        }

        for (int direction : adjacentOffsetTable)
        {
            if (isFlippable( direction, m ))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Does the specified player have a valid move?
     * @param player player to check
     * @return true iff valid move
     */
    public boolean hasValidMove( BoardValue player )
    {
        Assert.isTrue( player.isPlayer() );

        for (Position pos : this)
        {
            if (isValidMove( new Move( player, pos ) ))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Mark valid moves for the specified player on the board.
     * @param player to mark moves for
     */
    public void determineValidMoves( BoardValue player )
    {
        BoardValue validMoveBoardValue;

        if (player == WHITE)
        {
            validMoveBoardValue = VALID_WHITE;
        }
        else
        {
            validMoveBoardValue = VALID_BLACK;
        }

        for (Position pos : this)
        {
            if (isValidMove( new Move( player, pos ) ))
            {
                setBoardValue( new Move( validMoveBoardValue, pos ) );
            }
            else if (!isOccupiedBoardValue( pos ))
            {
                // ensure that old valid moves have been cleared
                setBoardValue( new Move( EMPTY, pos ) );
            }
        }
    }


    /** 
     * applies the specied move to the board
     * @param m the move to apply
     */
    public void makeMove( Move m )
    {
        Log.d( TAG, "makeMove: " + m );

        Assert.isTrue( isValidMove( m ), "Invalid move" );

        BoardValue thisPlayer = m.getValue();
        setBoardValue( m );

        for (int direction : adjacentOffsetTable)
        {
            if (isFlippable( direction, m ))
            {
                Position cur = m.getPosition();

                boolean done = false;

                while (!done)
                {
                    cur = cur.add( direction );

                    Assert.isTrue( cur.isValid() );

                    BoardValue curBoardValue = getValue( cur );

                    if (curBoardValue == thisPlayer)
                    {
                        // finish when reaching own piece
                        done = true;
                    }
                    else
                    {
                        // Internal consistency check.  We are flipping
                        // existing pieces not occupying empty cells.
                        Assert.isTrue( !isEmptyBoardValue( cur ), "Position not occupied" );

                        setBoardValue( cur, thisPlayer );
                    }
                }
            }
        }
    }


    /**
     * Expose an iterator for accessing cells on the board.
     * @return the iterator
     */
    @Override
    public Iterator<Position> iterator()
    {
        return new PositionIterator();
    }



    /**
     * Stringifier
     * @return String representation
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        int i = 0;
        for (BoardValue cell : board)
        {
            buf.append( cell.toString() );

            if ((++i % BOARD_SIZE) == 0)
            {
                buf.append( '\n' );
            }
        }

        return buf.toString();
    }


    /**
     * De-stringifier
     * @see #toString()
     * @param str string representation
     * @return new board
     */
    public static Board fromString( String str )
    {
        Reader reader = new StringReader( str );

        Board b = new Board();

        int bp = 0;
        while (true)
        {
            try
            {
                int c = reader.read();
                if (c < 0)
                {
                    break;
                }

                if (!Character.isWhitespace( c ))
                {
                    b.setBoardValue( new Position( bp ), BoardValue.fromChar( (char) c ) );
                    bp++;
                }
            }
            catch (IOException ex)
            {
                // highly unexpected
                Log.e( TAG, "Reconstitution failed", ex );
                break;
            }
        }

        return b;
    }


    /**
     * Accessor for a cell on the board
     * @param pos cell to access
     * @return the cell
     */
    public BoardValue getValue( Position pos )
    {
        return board[pos.getLinear()];
    }


    /**
     * Accessor for a cell on the board
     * @return the linear board
     */
    public BoardValue[] getLvalue()
    {
        return board;
    }


    /**
     * Accessor for a cell on the board
     * @param linear linear position
     * @return the cell
     */
    public BoardValue getLvalue( int linear )
    {
        Log.d( TAG, "getLvalue: [" + linear + "] = " +
            ((board[linear] == null) ? "null" : board[linear].toString() ) );

        return board[linear];
    }


    /**
     * Accessor for a cell on the board
     * @param x x-ordinate of cell to access
     * @param y y-ordinate of cell to access
     * @return the cell
     */
    public BoardValue getValue( int x, int y )
    {
        return board[new Position( x, y ).getLinear()];
    }


    /**
     * Set a cell
     * @param p position of cell to set
     * @param c value to set the cell to
     */
    protected void setBoardValue( Position p, BoardValue c )
    {
        board[p.getLinear()] = c;
    }


    /**
     * Set a cell
     * @param m position and value of cell to set
     */
    protected void setBoardValue( Move m )
    {
        Position p = m.getPosition();

        board[p.getLinear()] = m.getValue();
    }


    /**
     * How many pieces does the specified player own?
     * @param player player to count pieces for
     * @return the number of pieces
     */
    public int countBoardValues( BoardValue player )
    {
        int count = 0;

        for (BoardValue cell : board)
        {
            if (cell == player)
            {
                count++;
            }
        }

        return count;
    }


    /** return true if the cell is occupied
     *
     * Test explicitly against white or black occupation
     * to correctly handle cells that are marked as valid moves.
     * @param p position on board to check
     * @return true if cell is occupied, false otherwise
     */
    protected boolean isOccupiedBoardValue( Position p )
    {
        BoardValue c = getValue( p );

        return c.isPlayer();
    }


    /**
     * Test whether cell is empty.  By definition, empty == !occupied.
     * @param p position to check
     * @return true iff empty
     */
    protected boolean isEmptyBoardValue( Position p )
    {
        return !isOccupiedBoardValue( p );
    }


    /**
     * Determine if there are any flippable pieces for the Move
     * in the specified direction
     */
    private boolean isFlippable( int direction, Move m )
    {
        Assert.isTrue( direction != 0 );

        final BoardValue thisPlayer = m.getValue();
        Position cur = new Position( m.getPosition().getLinear() );
        int otherPieces = 0;

        for (;;)
        {
            Position last = new Position( cur.getLinear() );
            cur = cur.add( direction );

            if (!cur.isValid() || !cur.isAdjacent( last ))
            {
                // past the edge of the board
                return false;
            }

            BoardValue curBoardValue = getValue( cur );

            if (curBoardValue == thisPlayer)
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

            if (isEmptyBoardValue( cur ))
            {
                return false;
            }

            ++otherPieces;
        }
    }


    /**
     * Iterator across the entire board
     */
    private class PositionIterator implements java.util.Iterator<Position>
    {
        private int cur = 0;


        public boolean hasNext()
        {
            return cur < (Board.BOARD_SIZE * Board.BOARD_SIZE);
        }


        public Position next()
        {
            return new Position( cur++ );
        }


        public void remove()
        {
            throw new UnsupportedOperationException();
        }


    }


}
