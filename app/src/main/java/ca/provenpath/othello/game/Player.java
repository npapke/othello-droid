/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;


/**
 * Abstraction for a player of the game.
 * @author npapke
 */
public abstract class Player
{
    /** 
     * Gives the player the opportunity to perform a move on
     * the specified game board.
     * @param board the game board to make the move on
     */
    public abstract void makeMove( Board board );

    public abstract void interruptMove();

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
