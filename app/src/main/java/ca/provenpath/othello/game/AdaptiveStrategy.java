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

/**
 * A strategy that considers multiple board characterizations:
 * <ul>
 * <li>score</li>
 * <li>number of valid moves</li>
 * <li>how difficult to flip pieces are</li>
 * </ul>
 * The weight of these characterizations changes through the course of the game.
 * <p>
 * Created by npapke on 3/30/15.
 */
public class AdaptiveStrategy extends Strategy {
    @Override
    public int determineBoardValue(BoardValue player, Board board) {
        int scoreMe = 0;
        int scoreOther = 0;
        int protectedScore = 0;
        int freedom = 0;
        int threats = 0;
        int numMoves = 0;
        int antiBuffs = 0;

        BoardValue otherPlayer = player.otherPlayer();

        for (int lpos = 0; Position.isValid(lpos); lpos++) {
            BoardValue curCell = board.getLvalue(lpos);

            if (curCell == player) {
                scoreMe++;
                numMoves++;
                protectedScore += calcProtectedScore(board.countProtected(lpos), lpos);
                antiBuffs += calcAntiBuffs(board, lpos);
            } else if (curCell == otherPlayer) {
                scoreOther++;
                numMoves++;
                protectedScore -= calcProtectedScore(board.countProtected(lpos), lpos);
                antiBuffs -= calcAntiBuffs(board, lpos);
            } else if (board.isValidMove(player, lpos)) {
                freedom++;
            } else if (board.isValidMove(otherPlayer, lpos)) {
                threats++;
            }
        }

        // TODO the edge evaluation considers adjacent empty cells and opponents to be equal.
        //      they are not. either need a "flippable" anti-buff or a better protected score
        //      algorithm.
        //      algorithm is not prioritizing corners enough.

        // A guaranteed win/loss
        if (scoreMe == 0) {
            return Integer.MIN_VALUE;
        } else if (scoreOther == 0) {
            return Integer.MAX_VALUE;
        }

        // nobody has a move
        if (freedom == 0 && threats == 0) {
            return determineFinalScore(player, board);
        }


        freedom = scale(freedom, 0, 16);  // a large max is not realistic
        threats = scale(threats, 0, 16);  // a large max is not realistic

        int score = scoreMe;//- scoreOther;

        /*
         * The following is conjecture
         */
        int finalScore = (freedom * 0)
                + (protectedScore * 1)
                + (score * 1)
                - (threats * 00)
                - (antiBuffs * 1);

        return finalScore;
    }

    private int calcAntiBuffs(Board board, int lpos) {

        int score = 0;
        switch (lpos) {
            /*
             * Corners
             */
            case 9:
                score += 2 * buffIfEmpty(board, 0);
                score += buffIfEmpty(board, 1);
                score += buffIfEmpty(board, 8);
                break;
            case 14:
                score += 2 * buffIfEmpty(board, 7);
                score += buffIfEmpty(board, 6);
                score += buffIfEmpty(board, 15);
                break;
            case 49:
                score += 2 * buffIfEmpty(board, 56);
                score += buffIfEmpty(board, 48);
                score += buffIfEmpty(board, 57);
                break;
            case 54:
                score += 2 * buffIfEmpty(board, 63);
                score += buffIfEmpty(board, 55);
                score += buffIfEmpty(board, 62);
                break;

            /*
             * Top Edge
             */
            case 10:
            case 11:
            case 12:
            case 13:
                score += buffIfEmpty(board, lpos - 7);
                score += buffIfEmpty(board, lpos - 8);
                score += buffIfEmpty(board, lpos - 9);
                break;

            /*
             * Left Edge
             */
            case 17:
            case 25:
            case 33:
            case 42:
                score += buffIfEmpty(board, lpos - 9);
                score += buffIfEmpty(board, lpos - 1);
                score += buffIfEmpty(board, lpos + 7);
                break;

            /*
             * Right Edge
             */
            case 22:
            case 30:
            case 38:
            case 46:
                score += buffIfEmpty(board, lpos - 7);
                score += buffIfEmpty(board, lpos + 1);
                score += buffIfEmpty(board, lpos + 9);
                break;

            /*
             * Bottom Edge
             */
            case 50:
            case 51:
            case 52:
            case 53:
                score += buffIfEmpty(board, lpos + 7);
                score += buffIfEmpty(board, lpos + 8);
                score += buffIfEmpty(board, lpos + 9);
                break;
        }
        return score;
    }

    private int buffIfEmpty(Board board, int lpos) {
        return board.isEmptyBoardValue(new Position(lpos)) ? 1 : 0;
    }

    /**
     * Scales input value to [0,100].  input value is considered to be in [min,max].
     *
     * @param value input value
     * @param min   lower bound for value
     * @param max   upper bound for value
     * @return scaled value
     */
    private int scale(int value, int min, int max) {
        int clipped = Math.min(Math.max(value, min), max);

        return (clipped - min) * 100 / (max - min);
    }

    /**
     * Scales input value to [-100,100].  input value is considered to be in [min,max].
     *
     * @param value input value
     * @param min   lower bound for value
     * @param max   upper bound for value
     * @return scaled value
     */
    private int scale2(int value, int min, int max) {
        int clipped = Math.min(Math.max(value, min), max);

        return ((clipped - min) * 200 / (max - min)) - 100;
    }

    /**
     * Scores a protected cell count
     *
     * @param count protected axis count
     * @return
     */
    private int calcProtectedScore(int count, int pos) {
        return (1 << count) - 1;
    }

    @Override
    public String toString() {
        return "AdaptiveStrategy";
    }
}
