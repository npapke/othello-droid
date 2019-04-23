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
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by npapke on 2/25/15.
 */
public class HumanPlayer extends Player {
    public final static String TAG = HumanPlayer.class.getName();

    public HumanPlayer(BoardValue color) {
        super(color);
    }

    public HumanPlayer(String serial) {
        super(serial);
    }

    @Override
    public Flux<MoveNotification> makeMove(Board board) {

        Flux<HumanMove> getMove = Flux
                .create(sink -> {
                    moveSink = sink;
                });

        return getMove
                .filter(move -> {
                    Move boardMove = move.getMove();

                    return (boardMove != null) && board.isValidMove(boardMove);
                })
                .doOnNext(n -> {
                    moveSink.complete();
                    moveSink = null;
                })
                .map(move -> new MoveNotification(true, move.getMove(), board));

    }

    @Override
    public void interruptMove() {
        // Signal the end
        if (moveSink != null) {
            moveSink.next(new HumanMove());
        }
    }

    public void attemptMove(int lvalue) {
        Log.d(TAG, "Move to " + lvalue);

        if (moveSink != null) {
            moveSink.next(new HumanMove(lvalue));
        }
    }

    /**
     * Just an apparatus to signal "no move"
     */
    private class HumanMove {
        public HumanMove() {
            mMove = null;
        }

        public HumanMove(int lvalue) {
            mMove = new Move(getColor(), new Position(lvalue));
        }

        public Move getMove() {
            return mMove;
        }

        private Move mMove;
    }

    private transient FluxSink moveSink;
}
