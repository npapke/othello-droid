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


import reactor.core.publisher.Flux;

/**
 * Abstraction for a player of the game.
 *
 * @author npapke
 */
public abstract class Player {
    public class MoveNotification {
        private boolean isFinal;
        private Move move;

        public MoveNotification(boolean isFinal, Move move) {
            this.isFinal = isFinal;
            this.move = move;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        public Move getMove() {
            return move;
        }

        public void setMove(Move move) {
            this.move = move;
        }
    }

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

    /**
     * Gives the player the opportunity to perform a move on
     * the specified game board.
     *
     * @param board the game board to make the move on
     */
    public abstract Flux<MoveNotification> makeMove(Board board);

    public void interruptMove() {
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
