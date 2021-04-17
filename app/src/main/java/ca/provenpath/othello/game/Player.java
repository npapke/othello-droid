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
import ca.provenpath.othello.game.observer.GameNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

/**
 * Abstraction for a player of the game.
 *
 * @author npapke
 */
public abstract class Player {
    public final static String TAG = Player.class.getName();

    private class Holder {
        protected transient volatile FluxSink<Integer> moveSink;
    }

    private transient Holder holder = new Holder();
    protected transient volatile boolean isInterrupted = false;


    public Player(BoardValue color) {
        this.color = color;
    }

    public Player(String serial) {
        this.color = BoardValue.valueOf(serial);
    }

    @Override
    public String toString() {
        return color.name();
    }

    protected Flux<Integer> userMoves() {

        final Holder h = holder;

        return Flux
                .<Integer>create(sink -> {
                    Log.d(TAG, "Creating user move publisher");
                    h.moveSink = sink;
                })
                .doOnNext(move->Log.d(TAG, "publish: Move to " + move))
                .doOnComplete(() -> { h.moveSink = null; Log.d(TAG,"Destroyed sink");});
    }

    protected void endUserMoves() {
        if (holder.moveSink != null) {
            // FIXME race
            holder.moveSink.complete();
        }
    }

    /**
     * Gives the player the opportunity to perform a move on
     * the specified game board.
     *
     * @param board the game board to make the move on
     */
    public Flux<GameNotification> makeMove(Board board)
    {
        isInterrupted = false;
        return doMakeMove(board);
    }

    protected abstract Flux<GameNotification> doMakeMove(Board board);

    public void interruptMove() {
        Log.i(TAG, color + " interruptMove");
        isInterrupted = true;
        // Signal the end
        if (holder.moveSink != null) {
            holder.moveSink.complete();
        }
    }

    public boolean offerMove(int lvalue) {
        Log.d(TAG, color + " Move to " + lvalue);

        if (holder.moveSink != null) {
            holder.moveSink.next(lvalue);
            return true;
        }
        return false;
    }

    public void hint() {
        Log.d(TAG, color + " hint requested" );

        if (holder.moveSink != null) {
            holder.moveSink.next(-1);
        }
    }

    //
    // ------------------- Bean Pattern ----------------
    //
    protected BoardValue color;


    /**
     * Get the value of color
     *
     * @return the value of color
     */
    public BoardValue getColor() {
        return color;
    }


    /**
     * Set the value of color
     *
     * @param color new value of color
     */
    public void setColor(BoardValue color) {
        Assert.isTrue(color == BoardValue.BLACK || color == BoardValue.WHITE);

        this.color = color;
    }

    public boolean isComputer() {
        return false;
    }
}
