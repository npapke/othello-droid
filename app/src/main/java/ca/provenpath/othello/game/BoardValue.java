/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
    VALID_BLACK;


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
        return ((this == VALID_BLACK) || (this == VALID_WHITE));
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

            default:
                throw new IllegalStateException( "Unexpected value: '" + c + "'" );
        }
    }

}
