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
import android.support.annotation.NonNull;
import android.util.Log;
import ca.provenpath.othello.PlayerSettingsFragment;
import ca.provenpath.othello.game.observer.AnalysisNotification;
import ca.provenpath.othello.game.observer.EngineNotification;
import ca.provenpath.othello.game.observer.GameNotification;
import ca.provenpath.othello.game.observer.MoveNotification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.time.temporal.ChronoUnit.MILLIS;
import static java.time.temporal.ChronoUnit.SECONDS;

/**
 * A computer player.  Automatically determines the optimal move for
 * a given board based on a strategy.  The strategy is implemented
 * separately as it is independent from the search algorithm.
 *
 * @author npapke
 */
@Getter
@Setter
public class ComputerPlayer extends Player {
    public final static String TAG = ComputerPlayer.class.getSimpleName();

    int maxDepth = 4;
    Strategy strategy = new AdaptiveStrategy();
    boolean showOverlay = false;
    Duration minTurnTime = Duration.ZERO;
    Duration delayInitialNotification = Duration.of(1, SECONDS);
    transient TranspositionTable transpositionTable;
    static transient Timer timer = new Timer("PlayerTimer");


    public ComputerPlayer(BoardValue color) {
        super(color);
    }

    public ComputerPlayer(BoardValue color, SharedPreferences prefs) {
        this(color);

        setMaxDepth(
                Integer.parseInt(prefs.getString(PlayerSettingsFragment.KEY_LOOKAHEAD, "6")));
        setStrategy(StrategyFactory.getObject(prefs.getString(PlayerSettingsFragment.KEY_STRATEGY, "")));

        setMinTurnTime(Duration.of(
                Integer.parseInt(prefs.getString(PlayerSettingsFragment.KEY_MIN_TIME_MS, "0")),
                MILLIS));
        setDelayInitialNotification(Duration.of(
                Integer.parseInt(prefs.getString(PlayerSettingsFragment.KEY_DELAY_TIME_MS, "1000")),
                MILLIS));

        setShowOverlay(prefs.getBoolean(PlayerSettingsFragment.KEY_ISOVERLAY_ANALYSIS, false));
    }

    public ComputerPlayer(String serial) {
        super(serial);
    }

    public boolean isComputer() {
        return true;
    }

    /**
     * Make a move on the board.
     *
     * @param board the board to move on
     * @return
     */
    @Override
    protected Flux<GameNotification> doMakeMove(Board board) {

        Log.i(TAG, "makeMove");

        return Flux
                .create(sink -> {

                    Log.i(TAG, "makeMove - executing sink");

                    Assert.notNull(color);
                    Assert.notNull(strategy);
                    Assert.isTrue(maxDepth > 0);
                    Assert.isTrue(board.hasValidMove(color));

                    Instant startProcessing = Instant.now();

                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            isInterrupted = true;
                        }
                    };

                    timer.schedule(timerTask, 3000);

                    Stats stats = new Stats();
                    MiniMaxResult result = minimaxAB(board, color, maxDepth, stats,
                            notification -> {
                                //Log.d(TAG, "to sink: " + notification);
                                sink.next(notification);
                            });

                    Assert.notNull(result.getBestPosition());

                    Log.i(TAG, "makeMove: " + result.getBestPosition() + ", value: " + result.getValue());
                    long duration = stats.duration();
                    Log.i(TAG, String.format("%d boards evaluated in %d ms. %d boards/sec",
                            stats.getBoardsEvaluated(),
                            duration,
                            duration > 0 ? (int) ((double) stats.getBoardsEvaluated() * 1000.0 / (double) duration) : 999999));
                    Log.i(TAG, String.format("%d cache hits", stats.getCacheHits()));

                    sink.next(new MoveNotification(new Move(color, result.getBestPosition()), startProcessing));
                    sink.complete();

                })
                .subscribeOn(Schedulers.newElastic("engine"), false)
                .publishOn(Schedulers.newElastic("delivery"))
                .filter(notification -> isShowOverlay() || !(notification instanceof AnalysisNotification))
                .delaySubscription(getDelayInitialNotification())
                .flatMapSequential(notification -> {

                    if (notification instanceof MoveNotification) {

                        // This is the final notification for this move.
                        Duration delay = getMinTurnTime()
                                .minus(Duration.between(((MoveNotification) notification).getGameStart(), Instant.now()));
                        delay = delay.isNegative() ? Duration.ZERO : delay;
                        return Flux.just((GameNotification) notification).delaySubscription(delay);

                    } else if (notification instanceof AnalysisNotification) {
                        // Make the score more human readable
                        ((AnalysisNotification) notification).setValue(
                                Strategy.normalizeScore(((AnalysisNotification) notification).getValue()));
                    }

                    return Flux.just((GameNotification) notification);
                });
    }


    /**
     * Recursively build game tree.  Find the move that leads to the
     * strongest board for the player.  Utilize alpha-beta pruning
     * to narrow search.
     * <p>
     * This is specialized implementation for the first ply.
     *
     * @param board  the board to evaluate
     * @param player the player who's move it is
     * @param depth  the current recursion depth (in half-moves)
     * @param stats  performance statistics
     * @return the value of board
     */
    private MiniMaxResult minimaxAB(
            Board board,
            BoardValue player,
            int depth,
            Stats stats,
            Consumer<GameNotification> notificationSinkFn) {
        final PriorityQueue<MiniMaxResult> candidates = new PriorityQueue<>();
        final PriorityQueue<MiniMaxResult> results = new PriorityQueue<>();

        for (Position pos : board) {
            Move m = new Move(player, pos);

            if (board.isValidMove(m)) {
                candidates.add(new MiniMaxResult(0, pos));
            }
        }

        notificationSinkFn.accept(
                new EngineNotification(stats.boardsEvaluated, stats.duration()));

        /*
         * Build game tree of increasing depths.  Use results of one iteration to
         * hint better candidates to the subsequent iteration.  alpha-beta pruning
         * benefits greatly from discovering the best solution early.
         */

        try {
            for (int curDepth = 1; curDepth <= depth; curDepth++) {
                // Always the maximizing player
                int alpha = Integer.MIN_VALUE;
                int beta = Integer.MAX_VALUE;

                results.clear();
                transpositionTable = new TranspositionTable();

                for (MiniMaxResult candidate : candidates) {
                    Board copyOfBoard = (Board) board.clone();
                    copyOfBoard.makeMove(player, candidate.getBestPosition().getLinear());

                    notificationSinkFn.accept(
                            new AnalysisNotification(0, candidate.getBestPosition(), false));

                    int result = (candidates.size() == 1)
                            ? 0  // Optimization.  There is only one move.
                            : minimaxAB(copyOfBoard, player.otherPlayer(), curDepth - 1, alpha, beta, stats);

                    results.add(new MiniMaxResult(result, candidate.getBestPosition()));

                    results.stream().forEach(r ->
                            notificationSinkFn.accept(
                                    new AnalysisNotification(r.getValue(), r.getBestPosition(),
                                            r.getValue() == results.peek().getValue())));

                    notificationSinkFn.accept(
                            new EngineNotification(stats.boardsEvaluated, stats.duration()));

                    alpha = Math.max(result, alpha);

                    if (isInterrupted)
                        break;
                }

                Log.i(TAG, String.format("Predicted best move: %s, found %s at depth %d",
                        candidates.peek(), results.peek(), curDepth));
                Log.i(TAG, String.format("Predicted best move at position %d of %d",
                        new Callable<Integer>() {
                            @Override
                            public Integer call() {
                                int ordinal = 1;
                                Position target = candidates.peek().getBestPosition();
                                for (MiniMaxResult res : results) {
                                    if (res.getBestPosition().equals(target)) {
                                        return ordinal;
                                    }
                                    ++ordinal;
                                }

                                return -1;
                            }
                        }.call(),
                        results.size()));

                candidates.clear();
                candidates.addAll(results);
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
            return bestResultOf(candidates);
        }

        // We should be assured at least one valid move
        Assert.isTrue(!results.isEmpty());

        return bestResultOf(results);
    }

    /**
     * Applies a little entropy to homogeneous results.
     *
     * @param results candidate results sorted in value order
     * @return "best" result
     */
    private MiniMaxResult bestResultOf(Iterable<MiniMaxResult> results) {
        MiniMaxResult best = null;

        for (MiniMaxResult result : results) {
            if (best == null) {
                best = result;
            } else if (best.value == result.value) {
                // Decide randomly which one to take
                // FIXME better distribution for N > 2 results
                if (Math.random() < 0.5) {
                    Log.i(TAG, "Updated result to " + result);
                    best = result;
                }
            } else {
                break;
            }
        }

        return best;
    }

    /**
     * Recursively build game tree.  Find the move that leads to the
     * strongest board for the player.  Utilize alpha-beta pruning
     * to narrow search.
     *
     * @param board  the board to evaluate
     * @param player the player who's move it is
     * @param depth  the current recursion depth (in half-moves)
     * @param alpha  the alpha maximum
     * @param beta   the beta maximum
     * @param stats  performance statistics
     * @return the value of board
     */
    private int minimaxAB(
            Board board,
            BoardValue player,
            int depth,
            int alpha,
            int beta,
            Stats stats) {
        int origAlpha = alpha;
        int origBeta = beta;

        if (isInterrupted) {
            throw new RuntimeException("out of time");
        }

        TranspositionTable.Entry ttEntry = transpositionTable.get(board);
        if (ttEntry != null) {
            switch (ttEntry.getFlag()) {
                case EXACT:
                    stats.incCacheHit();
                    return ttEntry.getValue();
                case LOWERBOUND:
                    alpha = Math.max(alpha, ttEntry.getValue());
                    break;
                case UPPERBOUND:
                    beta = Math.min(beta, ttEntry.getValue());
                    break;
            }
            if (beta <= alpha) {
                //Log.d(TAG, "Cache prune");
                stats.incCacheHit();
                return ttEntry.getValue();
            }
        }

        if (depth <= 0) {
            stats.incBoard();
            return strategy.determineBoardValue(color, board);
        }

        boolean validMoveSeen = false;
        int value;

        if (player == color) {
            value = Integer.MIN_VALUE;

            for (int pos = 0; Position.isValid(pos); pos++) {
                if (board.isValidMove(player, pos)) {
                    validMoveSeen = true;

                    Board copyOfBoard = (Board) board.clone();
                    copyOfBoard.makeMove(player, pos);

                    value = minimaxAB(copyOfBoard, player.otherPlayer(), depth - 1, alpha, beta, stats);
                    alpha = Math.max(value, alpha);
                    if (beta <= alpha)
                        break;
                }

                if (isInterrupted)
                    break;
            }
        } else {
            value = Integer.MAX_VALUE;

            for (int pos = 0; Position.isValid(pos); pos++) {
                if (board.isValidMove(player, pos)) {
                    validMoveSeen = true;

                    Board copyOfBoard = (Board) board.clone();
                    copyOfBoard.makeMove(player, pos);

                    value = minimaxAB(copyOfBoard, player.otherPlayer(), depth - 1, alpha, beta, stats);
                    beta = Math.min(value, beta);
                    if (beta <= alpha)
                        break;
                }

                if (isInterrupted)
                    break;
            }
        }

        if (!validMoveSeen) {
            // player has to pass ...

            if (board.hasValidMove(player.otherPlayer())) {
                value = minimaxAB(board, player.otherPlayer(), depth, alpha, beta, stats);
            } else {
                // neither player has a valid move.  return the score
                value = strategy.determineFinalScore(color, board);
            }
        }

        TranspositionTable.Flag flag;
        if (value <= origAlpha) {
            flag = TranspositionTable.Flag.UPPERBOUND;
        } else if (value >= origBeta) {
            flag = TranspositionTable.Flag.LOWERBOUND;
        } else {
            flag = TranspositionTable.Flag.EXACT;
        }
        transpositionTable.put(new TranspositionTable.Entry(value, depth, flag, board));

        return value;
    }


    @AllArgsConstructor
    @Data
    private class MiniMaxResult implements Comparable<MiniMaxResult> {

        private int value;
        private Position bestPosition;

        /**
         * Compares this object to the specified object to determine their relative
         * order.
         * <p>
         * Natural sort order is highest value results first.
         *
         * @param another the object to compare to this instance.
         * @return a negative integer if this instance is less than {@code another};
         * a positive integer if this instance is greater than
         * {@code another}; 0 if this instance has the same order as
         * {@code another}.
         * @throws ClassCastException if {@code another} cannot be converted into something
         *                            comparable to {@code this} instance.
         */
        @Override
        public int compareTo(@NonNull MiniMaxResult another) {
            return Integer.compare(another.value, this.value);
        }
    }

    /**
     * Helper for performance statistics.  Thread-safe.
     private class Stats
     {
     private long start = System.currentTimeMillis();
     private AtomicInteger boardsEvaluated = new AtomicInteger( 0 );

     public void incBoard()
     {
     boardsEvaluated.getAndIncrement();
     }

     public int getBoardsEvaluated()
     {
     return boardsEvaluated.get();
     }

     public long duration()
     {
     return System.currentTimeMillis() - start;
     }
     }
     */


    /**
     * Helper for performance statistics.  Not thread-safe.
     */
    private class Stats {
        private long start = System.currentTimeMillis();
        private int boardsEvaluated = 0;
        private int cacheHits = 0;

        public void incBoard() {
            boardsEvaluated++;
        }

        public void incCacheHit() {
            cacheHits++;
        }

        public int getBoardsEvaluated() {
            return boardsEvaluated;
        }

        public int getCacheHits() {
            return cacheHits;
        }

        public long duration() {
            return System.currentTimeMillis() - start;
        }
    }
}
