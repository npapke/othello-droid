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

import android.util.Log;

import ca.provenpath.othello.game.observer.GameState;

import java.util.Observable;

/**
 * Execute a game.  Coordinate the game board and players.
 * @author npapke
 */
public class GameExecutor extends Observable
{
    public final static String TAG = GameExecutor.class.getName();

    public void newGame()
    {
        Log.i( TAG, "newGame" );

        board = new Board();
        moveNumber = 1;
        state = GameState.TURN_PLAYER_0;

        setChanged();
        notifyObservers( this );
    }

    public void endGame()
    {
        setState( GameState.GAME_OVER );

        if (player[0] != null)
            player[0].interruptMove();
        if (player[1] != null)
            player[1].interruptMove();

        setChanged();
        notifyObservers( this );
    }

    public boolean isConsistent()
    {
        return (board != null) && board.isConsistent() && (player[0] != null) && (player[1] != null);
    }


    /**
     * Execute one turn on the board.
     * The <code>Player</code>s will be asked to make the actual move.
     */
    public void executeOneTurn()
    {
        Log.d( TAG, "executeOneTurn: state=" + state );

        Assert.notNull( isConsistent() );

        switch (state)
        {
            case TURN_PLAYER_0:
                player[0].makeMove( board );
                moveNumber++;
                if (board.hasValidMove( player[1].getColor() ))
                {
                    setState( GameState.TURN_PLAYER_1 );
                }
                else if (!board.hasValidMove( player[0].getColor() ))
                {
                    setState( GameState.GAME_OVER );
                }
                break;

            case TURN_PLAYER_1:
                player[1].makeMove( board );
                moveNumber++;
                if (board.hasValidMove( player[0].getColor() ))
                {
                    setState( GameState.TURN_PLAYER_0 );
                }
                else if (!board.hasValidMove( player[1].getColor() ))
                {
                    setState( GameState.GAME_OVER );
                }
                break;

            case INACTIVE:
                throw new IllegalStateException( "Game not started" );

            default:
                Log.w( TAG, "executeOneTurn: unexpectedly called while in state=" + state );
                break;
        }

        setChanged();
        notifyObservers( this );
    }

    /**
     * Update state and prevent from being overwritten when game has ended
     * @param state
     */
    private void setState( GameState state )
    {
        if (this.state != GameState.GAME_OVER)
        {
            this.state = state;
        }
    }

    /**
     * Gets the player that will make the next move.
     */
    public Player getNextPlayer()
    {
        switch (state)
        {
            case TURN_PLAYER_0:
                return player[0];
            case TURN_PLAYER_1:
                return player[1];
            default:
                return null;
        }
    }

    //
    // ------------ Bean Pattern ------------
    //
    protected Board board;


    /**
     * Get the value of board
     *
     * @return the value of board
     */
    public Board getBoard()
    {
        return board;
    }


    protected GameState state;


    /**
     * Get the value of state
     *
     * @return the value of state
     */
    public GameState getState()
    {
        return state;
    }


    protected Player[] player = new Player[ 2 ];


    /**
     * Get the value of player
     *
     * @return the value of player
     */
    public Player[] getPlayer()
    {
        return player;
    }


    /**
     * Get the value of player at specified index
     *
     * @param index
     * @return the value of player at specified index
     */
    public Player getPlayer( int index )
    {
        return this.player[index];
    }


    /**
     * Set the value of player at specified index.
     *
     * @param index
     * @param newPlayer new value of player at specified index
     */
    public void setPlayer( int index, Player newPlayer )
    {
        if (this.player[index] != null)
            this.player[index].interruptMove();

        this.player[index] = newPlayer;
    }


    protected int moveNumber = 0;


    /**
     * Get the value of moveNumber
     *
     * @return the value of moveNumber
     */
    public int getMoveNumber()
    {
        return moveNumber;
    }

}
