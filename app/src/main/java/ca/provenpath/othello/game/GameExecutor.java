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
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
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
        transient GameNotification notification;

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

    private Executor gameExecutor = Executors.newSingleThreadExecutor();
    private FutureTask<Void> gameTask;
    private volatile boolean stopGameThread = false;
    private Deque<Tracker> history = new ConcurrentLinkedDeque<>();
    private Tracker currentState;

    public void close() {
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

        gameTask = new FutureTask<>(() -> runOneGame(uiThreadHandler, prefs, tracker));
        gameExecutor.execute(gameTask);
    }

    private synchronized void endGame() {
        Log.i(TAG, "endGame");
        if (gameTask != null) {

            stopGameThread = true;
            if (currentState != null) {
                Arrays.stream(currentState.getPlayer())
                        .forEach(player -> player.interruptMove());
            }

            try {
                gameTask.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.i(TAG, "endgame", e);
            } finally {
                stopGameThread = false;
            }

            gameTask = null;
        }
    }

    public synchronized Optional<Tracker> getGameState() {
        return Optional.ofNullable(currentState);
    }

    public synchronized Optional<Tracker> popUndoGameState() {
        return Optional.ofNullable(history.isEmpty() ? null : history.pop());
    }

    public synchronized Optional<Tracker> peekUndoGameState() {
        return history.stream().findFirst();
    }

    public synchronized Tracker[] getHistory() {
        return history.toArray(new Tracker[0]);
    }

    public synchronized void setHistory(Tracker[] trackers) {
        history.clear();
        history.addAll(Arrays.asList(trackers));
    }

    private Void runOneGame(
            @NonNull Handler uiThreadHandler,
            @NonNull Function<BoardValue, SharedPreferences> prefs,
            @NonNull Optional<Tracker> inTracker) {

        Log.i(TAG, "runOneGame");

        if (!inTracker.isPresent()) {
            inTracker = Optional.of(newGame());  // beware side effects
        }

        Tracker tracker = inTracker.get();

        while (tracker.getState() != GameState.GAME_OVER && !stopGameThread) {
            applyPreferences(prefs, tracker);

            Log.i(TAG, "runOneGame: updating current state");
            currentState = tracker;

            tracker = Optional.ofNullable(
                    Flux
                            .just(new Tracker(tracker))
                            .doOnNext(trkr -> sendRedrawRequest(uiThreadHandler, trkr))
                            .flatMap(trkr -> nextTurn(trkr))
                            .doOnNext(trkr -> {
                                if (trkr.getNotification() instanceof MoveNotification) {
                                    Log.d(TAG, "game: " + trkr.toString());
                                }
                            })
                            // FIXME Reactor Core doesn't have a way to get a UI thread Scheduler.
                            //       Would really like to avoid the Handler.
                            .doOnNext(trkr -> sendRedrawRequest(uiThreadHandler, trkr))
                            .doOnError(error -> {
                                Log.i(TAG, "runOneGame", error);
                                sendRedrawRequest(uiThreadHandler, null);
                            }) // FIXME
                            .blockLast())
                    .orElse(new Tracker(tracker)); // keep old state on error

        }

        Log.i(TAG, "runOneGame: game complete");
        currentState = tracker;

        return null;
    }

    private Tracker newGame() {
        Log.i(TAG, "newGame");

        // Beware the side effects
        history.clear();
        currentState = null;

        Tracker tracker = new Tracker();
        tracker.state = GameState.TURN_PLAYER_0;
        tracker.board = new Board();

        return tracker;
    }


    /**
     * Sends redraw request to UI thread.
     */
    private void sendRedrawRequest(@NonNull Handler handler, Tracker tracker) {
        Message msg = handler.obtainMessage(MainActivity.MSG_NOTIFICATION, tracker);
        msg.sendToTarget();
    }

    private void applyPreferences(Function<BoardValue, SharedPreferences> prefs, Tracker tracker) {
        applyPreferences(prefs, tracker, BoardValue.BLACK, 0);
        applyPreferences(prefs, tracker, BoardValue.WHITE, 1);
    }

    private void applyPreferences(Function<BoardValue, SharedPreferences> prefsFn, GameExecutor.Tracker
            tracker, BoardValue color, int index) {
        try {
            SharedPreferences prefs = prefsFn.apply(color);

            if (prefs.getBoolean(PlayerSettingsFragment.KEY_ISCOMPUTER, false)) {
                ComputerPlayer cplayer = new ComputerPlayer(color, prefs);
                tracker.setPlayer(index, cplayer);
            } else {
                tracker.setPlayer(index, new HumanPlayer(color));
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot apply preferences", e);
        }
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
                                    .flatMapSequential(notification -> {
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
                        if (!tracker.getNextPlayer().isComputer()) {
                            // this move is undo-able
                            history.push(new Tracker(tracker));
                        }
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
