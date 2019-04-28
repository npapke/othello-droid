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
import ca.provenpath.othello.game.observer.MoveNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.Optional;

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
    public Flux<GameNotification> makeMove(Board board) {

        return Flux
                .<Optional<Integer>>create(sink -> {
                    moveSink = sink;
                })
                .flatMap(move -> {
                    if (move.isPresent()) {
                        if (board.isValidMove(getColor(), move.get())) {
                            moveSink.complete();
                            return Flux.just(new Move(getColor(), new Position(move.get())));
                        }
                    } else {
                        // interrupted move
                        moveSink.complete();
                    }
                    return Flux.empty();
                })
                .<GameNotification>map(move -> new MoveNotification(move))
                .doOnComplete(() -> moveSink = null);

    }

    @Override
    public void interruptMove() {
        Log.i(TAG, "interruptMove");
        // Signal the end
        if (moveSink != null) {
            moveSink.next(Optional.empty());
        }
    }

    public boolean offerMove(int lvalue) {
        Log.d(TAG, "Move to " + lvalue);

        if (moveSink != null) {
            moveSink.next(Optional.of(lvalue));
            return true;
        }
        return false;
    }

    private transient volatile FluxSink<Optional<Integer>> moveSink;
}
