/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;


import java.security.PublicKey;

/**
 * Abstraction for a player of the game.
 * @author npapke
 */
public class Player
{
    public Player( BoardValue color )
    {
        this.color = color;
    }

    public Player( String serial )
    {
        this.color = BoardValue.valueOf( serial );
    }

    @Override
    public String toString()
    {
        return color.name();
    }

    /**
     * Gives the player the opportunity to perform a move on
     * the specified game board.
     * @param board the game board to make the move on
     */
    public void makeMove( Board board ) {}

    public void interruptMove() {}

    //
    // ------------------- Bean Pattern ----------------
    //
    protected BoardValue color;


    /**
     * Get the value of color
     *
     * @return the value of color
     */
    public BoardValue getColor()
    {
        return color;
    }


    /**
     * Set the value of color
     *
     * @param color new value of color
     */
    public void setColor( BoardValue color )
    {
        Assert.isTrue( color == BoardValue.BLACK || color == BoardValue.WHITE );
        
        this.color = color;
    }
}
