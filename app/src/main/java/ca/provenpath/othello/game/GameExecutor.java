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

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import ca.provenpath.othello.MainActivity;
import ca.provenpath.othello.PlayerSettingsFragment;
import ca.provenpath.othello.game.observer.GameNotification;
import ca.provenpath.othello.game.observer.GameState;
import ca.provenpath.othello.game.observer.MoveNotification;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Function;

/**
 * Execute a game.  Coordinate the game board and players.
 *
 * @author npapke
 */
public class GameExecutor {
    public final static String TAG = GameExecutor.class.getName();

    //region [Singleton]
    // TODO Look into dependency injection
    private static GameExecutor mInstance;

    public static synchronized GameExecutor instance() {
        if (mInstance == null) {
            Hooks.onOperatorDebug();
            mInstance = new GameExecutor();
        }
        return mInstance;
    }
    //endregion

    //region [Tracker]

    /**
     * Tracks all state for a game.
     */
    @Getter
    @Setter
    public static class Tracker {

        GameState state;
        Board board;
        Player player[] = new Player[2];
        GameNotification notification;

        public Tracker() {
        }

        public Tracker(Tracker other) {
            this.state = other.state;
            this.board = new Board(other.board);
            this.player = Arrays.copyOf(other.player, other.player.length);
            this.notification = other.notification;
        }

        @Override
        public String toString() {
            return "Tracker{" +
                    "move=" + board.getNumPieces() +
                    ", state=" + state +
                    ", board=\n" + board +
                    '}';
        }

        public boolean isConsistent() {
            return (board != null) &&
                    board.isConsistent() &&
                    (player[0] != null) &&
                    (player[1] != null);
        }

        /**
         * Gets the player that will make the next move.
         */
        public Player getNextPlayer() {
            switch (state) {
                case TURN_PLAYER_0:
                    return player[0];
                case TURN_PLAYER_1:
                    return player[1];
                default:
                    return null;
            }
        }

        /**
         * Get the value of player at specified index
         *
         * @param index
         * @return the value of player at specified index
         */
        public Player getPlayer(int index) {
            return this.player[index];
        }


        /**
         * Set the value of player at specified index.
         *
         * @param index
         * @param newPlayer new value of player at specified index
         */
        public void setPlayer(int index, Player newPlayer) {
            if (this.player[index] != null)
                this.player[index].interruptMove();

            this.player[index] = newPlayer;
        }
    }
    //endregion

    private Thread gameThread;
    private volatile boolean stopGameThread;
    private Deque<Tracker> history = new ConcurrentLinkedDeque<>();

    public void finalize() {
        endGame();
    }

    /**
     * Executes on game on a separate thread.
     * At most one game can be active at a time.
     */
    public synchronized void executeOneGame(
            Handler uiThreadHandler,
            Function<BoardValue, SharedPreferences> prefs,
            Optional<Tracker> tracker) {

        Log.i(TAG, "executeOneGame: " + tracker.toString());

        endGame();

        // TODO this limits us to one undo level
        history.clear();

        gameThread = new Thread(() -> runOneGame(uiThreadHandler, prefs, tracker));
        gameThread.setName("game");
        gameThread.start();
    }

    private void endGame() {
        Log.i(TAG, "endGame");
        if (gameThread != null) {
            stopGameThread = true;

            history
                    .stream()
                    .limit(1)
                    .forEach(tracker -> Arrays.stream(tracker.getPlayer())
                            .forEach(player -> player.interruptMove()));
            try {
                gameThread.join(5000);
            } catch (InterruptedException e) {
            }
            stopGameThread = false;
        }
    }

    public Optional<Tracker> getGameState() {
        return history.stream().findFirst();
    }

    public Optional<Tracker> getUndoGameState() {
        return history.stream().skip(1).limit(1).findFirst();
    }

    private void runOneGame(
            Handler uiThreadHandler,
            Function<BoardValue, SharedPreferences> prefs,
            Optional<Tracker> inTracker) {

        Tracker tracker = inTracker.orElse(newGame());

        while (tracker.getState() != GameState.GAME_OVER && !stopGameThread) {
            applyPreferences(prefs, tracker);

            if (!tracker.getNextPlayer().isComputer()) {
                history.push(tracker);
                Log.i(TAG, "push: " + tracker.toString());
            }

            tracker = Optional.ofNullable(
                    Flux
                            .just(new Tracker(tracker))
                            .doOnNext(trkr -> sendRedrawRequest(uiThreadHandler, trkr))
                            .flatMap(trkr -> nextTurn(trkr))
                            .doOnNext(trkr -> Mono
                                    .just(trkr)
                                    .filter(trkr2 -> trkr2.getNotification() instanceof MoveNotification)
                                    .doOnEach(trkr2 -> Log.d(TAG, "game: " + trkr2.toString())))
                            // FIXME Reactor Core doesn't have a way to get a UI thread Scheduler.
                            //       Would really like to avoid the Handler.
                            .doOnNext(trkr -> sendRedrawRequest(uiThreadHandler, trkr))
                            .doOnError(error -> sendRedrawRequest(uiThreadHandler, null)) // FIXME
                            .blockLast())
                    .orElse(tracker); // keep old state on error
        }
    }

    private Tracker newGame() {
        Log.i(TAG, "newGame");

        Tracker tracker = new Tracker();
        tracker.state = GameState.TURN_PLAYER_0;
        tracker.board = new Board();

        return tracker;
    }


    /**
     * Sends redraw request to UI thread.
     */
    private void sendRedrawRequest(Handler handler, Tracker tracker) {
        Message msg = handler.obtainMessage(MainActivity.MSG_NOTIFICATION, tracker);
        msg.sendToTarget();
    }

    private boolean applyPreferences(Function<BoardValue, SharedPreferences> prefs, Tracker tracker) {
        boolean rv0 = applyPreferences(prefs, tracker, BoardValue.BLACK, 0);
        boolean rv1 = applyPreferences(prefs, tracker, BoardValue.WHITE, 1);

        return rv0 || rv1;
    }

    private boolean applyPreferences(Function<BoardValue, SharedPreferences> prefsFn, GameExecutor.Tracker tracker, BoardValue color, int index) {
        try {
            SharedPreferences prefs = prefsFn.apply(color);

            Player oldPlayer = tracker.getPlayer(index);

            if (prefs.getBoolean(PlayerSettingsFragment.KEY_ISCOMPUTER, false)) {
                ComputerPlayer cplayer = new ComputerPlayer(color);

                cplayer.setMaxDepth(Integer.parseInt(prefs.getString(PlayerSettingsFragment.KEY_LOOKAHEAD, "4")));
                cplayer.setStrategy(StrategyFactory.getObject(prefs.getString(PlayerSettingsFragment.KEY_STRATEGY, "")));

                tracker.setPlayer(index, cplayer);

                return (oldPlayer == null) || !oldPlayer.isComputer();
            } else {
                tracker.setPlayer(index, new HumanPlayer(color));

                return (oldPlayer == null) || oldPlayer.isComputer();
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot apply preferences", e);
        }

        return false;
    }

    private Flux<Tracker> nextTurn(Tracker inTracker) {

        return Flux
                .just(inTracker)
                .flatMap(tracker -> {

                    switch (tracker.state) {
                        case TURN_PLAYER_0:
                        case TURN_PLAYER_1: {
                            Player me = (tracker.state == GameState.TURN_PLAYER_0) ? tracker.player[0] : tracker.player[1];
                            return me.makeMove(tracker.board)
                                    .flatMap(notification -> {
                                        Tracker copy = new Tracker(tracker);
                                        copy.setNotification(notification);
                                        return Flux.just(copy);
                                    });
                        }

                        default:
                            Log.w(TAG, "executeOneTurn: unexpectedly called while in state=" + tracker.state);
                            return Flux.error(new IllegalStateException("game state"));
                    }
                })
                .doOnNext(tracker -> {
                    if (tracker.getNotification() instanceof MoveNotification) {
                        tracker.board.makeMove(((MoveNotification) tracker.getNotification()).getMove());

                        Player me = (tracker.state == GameState.TURN_PLAYER_0) ? tracker.player[0] : tracker.player[1];
                        if (tracker.board.hasValidMove(me.getColor().otherPlayer())) {
                            tracker.state = tracker.state == GameState.TURN_PLAYER_0
                                    ? GameState.TURN_PLAYER_1 : GameState.TURN_PLAYER_0;
                        } else if (!tracker.board.hasValidMove(me.getColor())) {
                            tracker.state = GameState.GAME_OVER;
                        }
                    }
                });
    }
}
