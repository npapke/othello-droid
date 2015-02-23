/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;

/**
 *
 * @author npapke
 */
public class Move
{


    public Move( BoardValue value, Position position)
    {
        this.position = position;
        this.value = value;
    }

    /**
     * Stringifier
     * @return string representation
     */
    @Override
    public String toString()
    {
        return value.toString() + position.toString();
    }
    
    
    //
    // -------------- Bean Pattern -------------
    //
    protected Position position;


    /**
     * Get the value of position
     *
     * @return the value of position
     */
    public Position getPosition()
    {
        return position;
    }


    /**
     * Set the value of position
     *
     * @param position new value of position
     */
    public void setPosition(Position position)
    {
        this.position = position;
    }


    protected BoardValue value;


    /**
     * Get the value of value
     *
     * @return the value of value
     */
    public BoardValue getValue()
    {
        return value;
    }


    /**
     * Set the value of value
     *
     * @param value new value of value
     */
    public void setValue(BoardValue value)
    {
        this.value = value;
    }


}
