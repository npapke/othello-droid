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
 * Represents the possible values a board location can take on.
 * @author npapke
 */
public enum BoardValue
{
    EMPTY,
    WHITE,
    BLACK,
    VALID_WHITE,
    VALID_BLACK,
    VALID_BOTH;


    /**
     * Does the value represent a player?
     * @return true iff the value is a player
     */
    public final boolean isPlayer()
    {
        switch (this)
        {
            case WHITE:
            case BLACK:
                return true;

            default:
                return false;
        }
    }


    /**
     * Is this the black player?  Bean-style convenience method.
     * @return true iff black player
     */
    public final boolean isBlack()
    {
        return this == BLACK;
    }


    /**
     * Is this the white player?  Bean-style convenience method.
     * @return true iff white player
     */
    public final boolean isWhite()
    {
        return this == WHITE;
    }


    /**
     * Is this cell empty?  Bean-style convenience method.
     * @return true iff empty
     */
    public final boolean isEmpty()
    {
        return this == EMPTY;
    }


    /**
     * Is this a valid move (black or white)?  Bean-style convenience method.
     * @return true iff valid move
     */
    public final boolean isValidMove()
    {
        switch (this)
        {
            case VALID_BOTH:
            case VALID_BLACK:
            case VALID_WHITE:
                return true;

            default:
                return false;
        }
    }


    /**
     * Return the opposite player
     * @return the other player
     */
    public final BoardValue otherPlayer()
    {
        switch (this)
        {
            case WHITE:
                return BLACK;

            case BLACK:
                return WHITE;

            default:
                throw new IllegalArgumentException( "Not a player: " + this );
        }
    }


    /**
     * Stingifier.
     * @return string representation
     */
    @Override
    public String toString()
    {
        switch (this)
        {
            case WHITE:
                return "W";

            case BLACK:
                return "B";

            case EMPTY:
                return ".";

            case VALID_WHITE:
                return "w";

            case VALID_BLACK:
                return "b";

            case VALID_BOTH:
                return "*";

            default:
                throw new IllegalStateException( "Unexpected value " + this.name() );
        }
    }


    /**
     * De-stringifier
     * @param c character representation
     * @return the board value
     */
    static BoardValue fromChar( char c )
    {
        switch (c)
        {
            case 'W':
                return WHITE;

            case 'B':
                return BLACK;

            case '.':
                return EMPTY;

            case 'w':
                return VALID_WHITE;

            case 'b':
                return VALID_BLACK;

            case '*':
                return VALID_BOTH;

            default:
                throw new IllegalStateException( "Unexpected value: '" + c + "'" );
        }
    }

}
