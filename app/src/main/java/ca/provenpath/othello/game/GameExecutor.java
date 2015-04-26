/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game;

import android.util.Log;

import ca.provenpath.othello.game.observer.GameState;

import java.io.Serializable;
import java.util.Arrays;
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

        Assert.notNull( player[0] );
        Assert.notNull( player[1] );

        board = new Board();
        moveNumber = 1;
        state = GameState.TURN_PLAYER_0;

        setChanged();
        notifyObservers( this );
    }

    public void endGame()
    {
        isEnded = true;

        player[0].interruptMove();
        player[1].interruptMove();

        state = GameState.GAME_OVER;

        setChanged();
        notifyObservers( this );
    }


    /**
     * Execute one turn on the board.
     * The <code>Player</code>s will be asked to make the actual move.
     */
    public void executeOneTurn()
    {
        Log.d( TAG, "executeOneTurn: state=" + state );

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
        if (!isEnded)
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

    private volatile boolean isEnded = false;

}
