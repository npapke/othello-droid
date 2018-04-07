package ca.provenpath.othello.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class BoardTest
{

    @Test
    public void isValidMove()
    {
        Board board = new Board();

        assertTrue( board.isValidMove( new Move( BoardValue.BLACK, new Position( 2, 3 ) ) ) );
    }
}