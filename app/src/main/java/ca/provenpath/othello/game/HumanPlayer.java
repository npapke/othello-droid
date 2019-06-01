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

import java.time.Duration;
import java.time.Instant;
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

        return userMoves()
                .flatMap(move -> {
                            if (board.isValidMove(getColor(), move)) {
                                endUserMoves();
                                return Flux.just(
                                        new MoveNotification(
                                                new Move(getColor(), new Position(move)), Instant.now()));
                            } else if (move < 0) {
                                // A hint is requested
                                ComputerPlayer computerPlayer = new ComputerPlayer(color);
                                computerPlayer.setDelayInitialNotification(Duration.ZERO);
                                computerPlayer.setShowOverlay(true);
                                computerPlayer.setMinTurnTime(Duration.ZERO);
                                computerPlayer.setMaxDepth(1);
                                computerPlayer.setStrategy(new AdaptiveStrategy());

                                return computerPlayer
                                        .makeMove(board)
                                        .filter(notification -> !(notification instanceof MoveNotification));
                            } else {
                                return Flux.empty();
                            }
                        }
                );
    }

}
