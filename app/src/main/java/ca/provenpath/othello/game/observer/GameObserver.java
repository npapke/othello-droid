/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.provenpath.othello.game.observer;

/**
 *
 * @author npapke
 */
public interface GameObserver
{
    void changeState( GameState newState );
}
