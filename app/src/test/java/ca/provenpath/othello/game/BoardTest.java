package ca.provenpath.othello.game;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardTest
{
    @Test
    public void isValidMove()
    {
        Board board = new Board();

        assertTrue( board.isValidMove( new Move( BoardValue.BLACK, new Position( 2, 3 ) ) ) );
    }

    @Test
    public void validMoves()
    {
        String state =
                "........" +
                        "........" +
                        "..wwww.." +
                        "bbwbbw.." +
                        ".w.bww.." +
                        "...bww.." +
                        "...b...." +
                        "........";
        Board board = new Board( state );

        assertEquals( board.getLvalue( 16 ), BoardValue.EMPTY );
        assertEquals( board.getLvalue( 17 ), BoardValue.VALID_BOTH );
        assertEquals( board.getLvalue( 24 ), BoardValue.BLACK );
        assertEquals( board.getLvalue( 32 ), BoardValue.VALID_WHITE );
        assertEquals( board.getLvalue( 42 ), BoardValue.VALID_BOTH );
    }

    @Test
    public void positionIterator()
    {
        Board board = new Board();

        int posCount = 0;
        for (Position pos : board)
        {
            ++posCount;
        }

        assertEquals( 64, posCount );
    }
}