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

import java.util.Objects;

/**
 *
 * @author npapke
 */
public class Move
{

    public Move( Move other )
    {
        this( other.value, other.position );
    }

    public Move( BoardValue value, Position position)
    {
        this.position = new Position( position );
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

    @Override
    public boolean equals( Object o )
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Objects.equals( position, move.position ) &&
                value == move.value;
    }

    @Override
    public int hashCode()
    {

        return Objects.hash( position, value );
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
