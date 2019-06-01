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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

import static ca.provenpath.othello.game.BoardValue.*;

/**
 * A game board.
 *
 * @author npapke
 */
public class Board implements Cloneable, Iterable<Position> {
    public final static String TAG = Board.class.getName();

    /**
     * Size of the board.  Board is square.
     */
    public static final int BOARD_SIZE = 8;

    /**
     * Linear size of the board
     */
    public static final int BOARD_LSIZE = BOARD_SIZE * BOARD_SIZE;

    /**
     * The board cells.  A linear array is more convenient
     * (and efficient) than a grid.
     */
    private BoardValue[] board;

    /**
     * Linear offset for adjacent cells.
     */
    private static int adjacentOffsetTable[] =
            {
                    1,
                    -1,
                    BOARD_SIZE,
                    -BOARD_SIZE,
                    BOARD_SIZE + 1,
                    BOARD_SIZE - 1,
                    -BOARD_SIZE + 1,
                    -BOARD_SIZE - 1
            };

    /**
     * Cardinal directions.  Like the adjacentOffsetTable, except only half.
     */
    private static int cardinalDirectionTable[] =
            {
                    1,
                    BOARD_SIZE,
                    BOARD_SIZE + 1,
                    BOARD_SIZE - 1
            };

    private final static int SERIALIZATION_VERSION = 1;
    protected int serial = -1;

    /**
     * Construct a board and initialize the cells for a game start.
     */
    public Board() {
        serial = SERIALIZATION_VERSION;
        board = new BoardValue[BOARD_LSIZE];

        Arrays.fill(board, EMPTY);

        board[Position.makeLinear(3, 3)] = WHITE;
        board[Position.makeLinear(4, 4)] = WHITE;
        board[Position.makeLinear(3, 4)] = BLACK;
        board[Position.makeLinear(4, 3)] = BLACK;

        updateValidMoves();
    }

    /**
     * Initialize the board from a string.
     * Each character represents one board position.
     * Valid characters are '.', 'b', 'w'.
     *
     * @param state
     */
    public Board(String state) {
        this();

        int lpos = 0;
        for (char c : state.toCharArray()) {
            switch (c) {
                case '.':
                    board[lpos] = EMPTY;
                    break;
                case 'b':
                    board[lpos] = BLACK;
                    break;
                case 'w':
                    board[lpos] = WHITE;
                    break;
            }

            lpos++;
        }

        updateValidMoves();
    }


    /**
     * Construct a board
     *
     * @param other the board to copy
     */
    public Board(Board other) {
        this.board = Arrays.copyOf(other.board, other.board.length);
        this.lastMovePos = other.lastMovePos;
        this.lastMoveValue = other.lastMoveValue;
        this.serial = other.serial;
        this.numPieces = other.numPieces;
    }

    public boolean isConsistent() {
        return board != null && serial == SERIALIZATION_VERSION;
    }


    /**
     * Create a copy of the board.  Expose the cloneable interface.
     *
     * @return copy of board
     */
    @Override
    public Object clone() {
        return new Board(this);
    }


    /**
     * Equality check.  Boards are equal iff all cells are equal.
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        try {
            Board other = (Board) obj;

            return Arrays.equals(board, other.board);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Hashcode.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return ZobristHash.getInstance().calcHash(this);
    }


    /**
     * Determine if the specified move is valid on the board.
     *
     * @param m the move to check
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(Move m) {
        return isValidMove(m.getValue(), m.getPosition().getLinear());
    }

    /**
     * Determine if the specified move is valid on the board.
     *
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(BoardValue player, int position) {
        if (position < 0 || position >= BOARD_LSIZE) {
            return false;
        }

        BoardValue cell = getLvalue(position);
        switch (cell) {
            case BLACK:
            case WHITE:
            case EMPTY:
                return false;

            case VALID_BOTH:
                return true;

            case VALID_BLACK:
                return player == BoardValue.BLACK;

            case VALID_WHITE:
                return player == BoardValue.WHITE;
        }

        return false;
    }

    /**
     * Mark valid moves for both players on the board.
     * Note: Not sure of there is much benefit in marking the opposing player.
     * Likely only useful for strategy implementations.
     */
    private void updateValidMoves() {
        numPieces = 0;
        for (int lpos = 0; lpos < board.length; lpos++) {
            BoardValue cell = board[lpos];

            if (cell.isPlayer()) {
                ++numPieces;
                continue;
            }

            cell = BoardValue.EMPTY;

            for (int direction : adjacentOffsetTable) {
                BoardValue valid = checkRun(direction, lpos);
                if (valid.isValidMove()) {
                    if (cell.isEmpty()) {
                        cell = valid;
                    } else if (cell != valid) {
                        cell = BoardValue.VALID_BOTH;
                        break;
                    }
                    // else cell == valid
                }
            }

            // reflect any updates
            board[lpos] = cell;
        }
    }

    /**
     * Determine if there is a run of same-colored pieces starting at
     * the specified position.
     *
     * @param direction direction to check
     * @param pos       position to evaluate
     * @return BoardValue.EMPTY when no valid move,
     * BoardValue.VALID_WHITE when valid for white only,
     * BoardValue.VALID_BLACK when valid for black only,
     * BoardValue.VALID_BOTH when valid for either player
     */
    private BoardValue checkRun(int direction, int pos) {
        Assert.isTrue(direction != 0);

        int cur = pos;

        BoardValue target = BoardValue.EMPTY;

        for (; ; ) {
            cur = Position.add(cur, direction);

            if (!Position.isValid(cur)) {
                // past the edge of the board
                return BoardValue.EMPTY;
            }

            BoardValue curBoardValue = getLvalue(cur);

            // Always expect a run of non-empty cells
            if (!curBoardValue.isPlayer()) {
                return BoardValue.EMPTY;
            }

            if (target == BoardValue.EMPTY) {
                // first cell after start
                target = curBoardValue.otherPlayer();
            } else {
                if (curBoardValue == target) {
                    return (target == BoardValue.BLACK) ? BoardValue.VALID_BLACK : BoardValue.VALID_WHITE;
                }
            }
        }
    }


    /**
     * Determines how well a the position on the board is protected,
     * i.e., how difficult it is to flip the piece in the specified
     * position.
     * <p>
     * A score of 0 indicates the piece is vulnerable in all directions.
     * A score of 4 indicates the piece cannot be flipped.
     * </p>
     *
     * @param p position
     * @return [0, 4]
     */
    public int countProtected(int p) {
        BoardValue me = getLvalue(p);

        if (!me.isPlayer()) {
            return 0;
        }

        BoardValue other = me.otherPlayer();
        int score = 0;

        for (int direction : cardinalDirectionTable) {
            // arbitrary direction names
            BoardValue left = findColorChange(-direction, p);
            BoardValue right = findColorChange(direction, p);

            if (me.equals(left) || me.equals(right)) {
                // a run of my color all the way to an edge
                ++score;
            } else if (other.equals(left) && other.equals(right)) {
                // my piece is boxed in
                ++score;
            } else if ((other.equals(left) || other.equals(right)) && (!left.isPlayer() || !right.isPlayer())) {
                // Penalize if trivially flippable
                return 0;
            } else if (!left.isPlayer() || !right.isPlayer()) {
                // An empty cell makes this vulnerable.  Penalize.
                --score;
            }
        }

        return Math.max(score, 0);
    }

    /**
     * Does the specified player have a valid move?
     *
     * @param player player to check
     * @return true iff valid move
     */
    public boolean hasValidMove(BoardValue player) {
        Assert.isTrue(player.isPlayer());

        for (Position pos : this) {
            if (isValidMove(new Move(player, pos))) {
                return true;
            }
        }

        return false;
    }


    /**
     * Applies the specified move to the board
     */
    public Board makeMove(Move m) {
        makeMove(m.getValue(), m.getPosition().getLinear());

        return this;
    }

    /**
     * Applies the specified move to the board
     */
    public void makeMove(BoardValue thisPlayer, int pos) {
        // Log.v( TAG, "makeMove: " + m );

        Assert.isTrue(isValidMove(thisPlayer, pos), "Invalid move");

        setBoardValue(pos, thisPlayer);
        lastMovePos = pos;
        lastMoveValue = thisPlayer;

        for (int direction : adjacentOffsetTable) {
            if (isFlippable(direction, thisPlayer, pos)) {
                int cur = pos;

                boolean done = false;

                while (!done) {
                    cur = Position.add(cur, direction);

                    Assert.isTrue(Position.isValid(cur));

                    BoardValue curBoardValue = getLvalue(cur);

                    if (curBoardValue == thisPlayer) {
                        // finish when reaching own piece
                        done = true;
                    } else {
                        // Internal consistency check.  We are flipping
                        // existing pieces not occupying empty cells.
                        Assert.isTrue(curBoardValue.isPlayer(), "Position not occupied");

                        setBoardValue(cur, thisPlayer);
                    }
                }
            }
        }

        updateValidMoves();
    }


    /**
     * Expose an iterator for accessing cells on the board.
     *
     * @return the iterator
     */
    @Override
    public Iterator<Position> iterator() {
        return new PositionIterator();
    }


    /**
     * Stringifier
     *
     * @return String representation
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        if (board != null) {
            int i = 0;
            for (BoardValue cell : board) {
                buf.append(cell.toString(true));

                if ((++i % BOARD_SIZE) == 0) {
                    buf.append('\n');
                }
            }
        } else {
            buf.append("no board");
        }

        return buf.toString();
    }


    /**
     * De-stringifier
     *
     * @param str string representation
     * @return new board
     * @see #toString()
     */
    public static Board fromString(String str) {
        Reader reader = new StringReader(str);

        Board b = new Board();

        int bp = 0;
        while (true) {
            try {
                int c = reader.read();
                if (c < 0) {
                    break;
                }

                if (!Character.isWhitespace(c)) {
                    b.setBoardValue(new Position(bp), BoardValue.fromChar((char) c));
                    bp++;
                }
            } catch (IOException ex) {
                // highly unexpected
                Log.e(TAG, "Reconstitution failed", ex);
                break;
            }
        }

        return b;
    }


    /**
     * Accessor for a cell on the board
     *
     * @param pos cell to access
     * @return the cell
     */
    public BoardValue getValue(Position pos) {
        return board[pos.getLinear()];
    }


    /**
     * Accessor for a cell on the board
     *
     * @return the linear board
     */
    public BoardValue[] getLvalue() {
        return board;
    }


    /**
     * Accessor for a cell on the board
     *
     * @param linear linear position
     * @return the cell
     */
    public BoardValue getLvalue(int linear) {
        // Log.d( TAG, "getLvalue: [" + linear + "] = " +
        //     ((board[linear] == null) ? "null" : board[linear].toString() ) );

        return board[linear];
    }


    /**
     * Accessor for a cell on the board
     *
     * @param x x-ordinate of cell to access
     * @param y y-ordinate of cell to access
     * @return the cell
     */
    public BoardValue getValue(int x, int y) {
        return board[new Position(x, y).getLinear()];
    }


    /**
     * Set a cell
     *
     * @param p position of cell to set
     * @param c value to set the cell to
     */
    protected void setBoardValue(int p, BoardValue c) {
        board[p] = c;
    }

    /**
     * Set a cell
     *
     * @param p position of cell to set
     * @param c value to set the cell to
     */
    protected void setBoardValue(Position p, BoardValue c) {
        board[p.getLinear()] = c;
    }


    /**
     * Set a cell
     *
     * @param m position and value of cell to set
     */
    protected void setBoardValue(Move m) {
        Position p = m.getPosition();

        board[p.getLinear()] = m.getValue();
    }


    /**
     * How many pieces does the specified player own?
     *
     * @param player player to count pieces for
     * @return the number of pieces
     */
    public int countBoardValues(BoardValue player) {
        int count = 0;

        for (BoardValue cell : board) {
            if (cell == player) {
                count++;
            }
        }

        return count;
    }


    /**
     * return true if the cell is occupied
     * <p>
     * Test explicitly against white or black occupation
     * to correctly handle cells that are marked as valid moves.
     *
     * @param p position on board to check
     * @return true if cell is occupied, false otherwise
     */
    protected boolean isOccupiedBoardValue(Position p) {
        BoardValue c = getValue(p);

        return c.isPlayer();
    }


    /**
     * Test whether cell is empty.  By definition, empty == !occupied.
     *
     * @param p position to check
     * @return true iff empty
     */
    protected boolean isEmptyBoardValue(int p) {
        BoardValue c = getLvalue(p);

        return c.isEmpty();
    }


    /**
     * Test whether cell is empty.  By definition, empty == !occupied.
     *
     * @param p position to check
     * @return true iff empty
     */
    protected boolean isEmptyBoardValue(Position p) {
        return !isOccupiedBoardValue(p);
    }


    /**
     * Determine if there are any flippable pieces for the Move
     * in the specified direction
     */
    private boolean isFlippable(int direction, BoardValue thisPlayer, int pos) {
        Assert.isTrue(direction != 0);

        int cur = pos;
        int otherPieces = 0;

        for (; ; ) {
            cur = Position.add(cur, direction);

            if (!Position.isValid(cur)) {
                // past the edge of the board
                return false;
            }

            BoardValue curBoardValue = getLvalue(cur);

            if (curBoardValue == thisPlayer) {
                if (otherPieces > 0) {
                    // need run of other players pieces followed by own player
                    return true;
                } else {
                    return false;
                }
            } else if (!curBoardValue.isPlayer())     // aka "empty" but allows for "valid move" states
            {
                return false;
            }

            ++otherPieces;
        }
    }

    /**
     * Checks that all pieces are of the same color from the specified position and direction.
     *
     * @param direction linear offset
     * @param pos       starting position
     * @return true iff pieces are of the same color
     */
    private BoardValue findColorChange(int direction, int pos) {
        final BoardValue thisPlayer = getLvalue(pos);
        int cur = pos;

        for (; ; ) {
            cur = Position.add(cur, direction);

            if (!Position.isValid(cur)) {
                // past the edge of the board
                return thisPlayer;
            }

            BoardValue curBoardValue = getLvalue(cur);

            if (curBoardValue != thisPlayer) {
                return curBoardValue;
            }
        }
    }


    public Move getLastMove() {
        return new Move(lastMoveValue, new Position(lastMovePos));
    }

    private int lastMovePos;
    private BoardValue lastMoveValue;

    public int getNumPieces() {
        return numPieces;
    }

    private int numPieces = 0;

    /**
     * Iterator across the entire board
     */
    private class PositionIterator implements java.util.Iterator<Position> {
        private int cur = 0;


        public boolean hasNext() {
            return cur < (Board.BOARD_SIZE * Board.BOARD_SIZE);
        }


        public Position next() {
            return new Position(cur++);
        }


        public void remove() {
            throw new UnsupportedOperationException();
        }


    }

    private static class ZobristHash {

        final static int NUM_COLORS = 2;
        private static ZobristHash instance;

        int[][] table;

        public static ZobristHash getInstance() {

            if (instance == null) {
                synchronized (ZobristHash.class) {
                    if (instance == null) {
                        instance = new ZobristHash();
                    }
                }
            }

            return instance;
        }

        private ZobristHash() {

            table = new int[Board.BOARD_LSIZE][NUM_COLORS];

            PrimitiveIterator.OfInt r = new Random().ints().iterator();

            for (int pos = 0; pos < Board.BOARD_LSIZE; ++pos) {
                for (int color = 0; color < NUM_COLORS; ++color) {
                    table[pos][color] = r.nextInt();
                }
            }
        }

        int calcHash(Board board) {
            int hash = 0;
            for (int pos = 0; pos < Board.BOARD_LSIZE; ++pos) {

                if (board.board[pos] == WHITE) {
                    hash = hash ^ table[pos][0];
                }
                else if (board.board[pos] == BLACK) {
                    hash = hash ^ table[pos][1];
                }

            }

            return hash;
        }
    }

}
