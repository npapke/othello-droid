/*
 *  Copyright (c) 2015 Norbert Papke <npapke@acm.org>
 *
 *  This file is part of Othello.
 *
 *  Othello is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Othello is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Othello.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.provenpath.othello.game;

/**
 * Position on a board
 * @author npapke
 */
public class Position
{
    protected int linear;


    /**
     * Constructor based on linear position
     * @param p the linear postion
     */
    public Position( int p )
    {
        setLinear( p );
    }


    /**
     * Constructor based on x-y position
     * @param x x-ordinate
     * @param y y-ordinate
     */
    public Position( int x, int y )
    {
        setLinear( makeLinear( x, y ) );
    }


    /**
     * Equality comparator
     * @param obj tight-hand side operator
     * @return true iff equal
     */
    @Override
    public boolean equals( Object obj )
    {
        try
        {
            Position other = (Position) obj;
            return (this.linear == other.linear);
        }
        catch (Exception e)
        {
            return false;
        }
    }


    /**
     * Hash code
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return this.linear;
    }


    /**
     * Stringifier
     * @return grid representation
     */
    @Override
    public String toString()
    {
        return "(" + getX() + "," + getY() + ")";
    }


    /**
     * Get the value of linear
     *
     * @return the value of linear
     */
    public int getLinear()
    {
        return linear;
    }


    /**
     * Calculate linear position from x-y coordinates
     * @param x x-ordinate
     * @param y y-ordinate
     * @return linear position
     */
    public static int makeLinear( int x, int y )
    {
        return x * Board.BOARD_SIZE + y;
    }


    /**
     * Set the value of linear
     *
     * @param linear new value of linear
     */
    public void setLinear( int linear )
    {
        this.linear = linear;
    }

    /**
     * Adjust a position.  The original position is unchanged.
     * <p>
     * Note that resultant position may be invalid.
     * @param linearOffset
     * @return an updated position
     */
    public Position add( int linearOffset )
    {
        return new Position( this.linear + linearOffset );
    }


    /**
     * Determine if the specified position is adjacent to this one.
     * This method is intended
     * @param p the position to compare to
     * @return <code>true</code> iff positions are adjacent
     */
    public boolean isAdjacent( Position p )
    {
        int xdiff = Math.abs( this.getX() - p.getX() );
        int ydiff = Math.abs( this.getY() - p.getY() );

        return ((xdiff + ydiff) == 1) || ((xdiff + ydiff) == 2);
    }

    /**
     * Get the value of x
     *
     * @return the value of x
     */
    public int getX()
    {
        return getLinear() / Board.BOARD_SIZE;
    }


    /**
     * Get the value of y
     *
     * @return the value of y
     */
    public int getY()
    {
        return getLinear() % Board.BOARD_SIZE;
    }


    /**
     * Is the position contained on the board?
     * @return true iff position is valid
     */
    public boolean isValid()
    {
        return this.linear >= 0 && this.linear < (Board.BOARD_SIZE * Board.BOARD_SIZE);
    }
}
