package ca.provenpath.othello.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class AdaptiveStrategyTest {

    @Test
    public void determineBoardValue01() {
        String state = "" +
                "........" +
                "........" +
                "........" +
                "...wb..." +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board1 = new Board(state);

        state = "" +
                "........" +
                "........" +
                "........" +
                "...ww..." +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board2 = new Board(state);

        AdaptiveStrategy strategy = new AdaptiveStrategy();

        int value1 = strategy.determineBoardValue(BoardValue.WHITE, board1);
        int value2 = strategy.determineBoardValue(BoardValue.WHITE, board2);

        assertTrue(value1 < value2);
    }

    @Test
    public void determineBoardValue02() {
        String state = "" +
                "........" +
                "........" +
                "........" +
                "...wbbbb" +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board1 = new Board(state);

        state = "" +
                "........" +
                "........" +
                "........" +
                "...ww..." +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board2 = new Board(state);

        AdaptiveStrategy strategy = new AdaptiveStrategy();

        int value1 = strategy.determineBoardValue(BoardValue.WHITE, board1);
        int value2 = strategy.determineBoardValue(BoardValue.WHITE, board2);

        assertTrue(value1 < value2);
    }

    @Test
    public void determineBoardValue03() {
        String state = "" +
                "........" +
                "........" +
                "........" +
                "...wbbbb" +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board1 = new Board(state);

        state = "" +
                "........" +
                "........" +
                "........" +
                "...wbbbb" +
                "...bw..." +
                ".....w.." +
                "......w." +
                ".......w";
        Board board2 = new Board(state);

        AdaptiveStrategy strategy = new AdaptiveStrategy();

        int value1 = strategy.determineBoardValue(BoardValue.WHITE, board1);
        int value2 = strategy.determineBoardValue(BoardValue.WHITE, board2);

        assertTrue(value1 < value2);
    }

    @Test
    public void determineBoardValue04() {
        String state = "" +
                "........" +
                "........" +
                "........" +
                "...wbbbb" +
                "...bw..w" +
                "........" +
                "........" +
                "........";
        Board board1 = new Board(state);

        state = "" +
                "........" +
                "........" +
                "........" +
                "...bbbbw" +
                "...bw..." +
                "........" +
                "........" +
                "........";
        Board board2 = new Board(state);

        AdaptiveStrategy strategy = new AdaptiveStrategy();

        int value1 = strategy.determineBoardValue(BoardValue.WHITE, board1);
        int value2 = strategy.determineBoardValue(BoardValue.WHITE, board2);

        assertTrue(value1 < value2);
    }

    @Test
    public void determineBoardValue05() {
        String state = "" +
                "........" +
                "........" +
                ".bbbww.b" +
                "...bbwb." +
                "...wbwww" +
                "..wbbb.." +
                "..b....." +
                "........";
        Board board1 = new Board(state);

        state = "" +
                "........" +
                "........" +
                ".bbb.w.b" +
                "...bbbb." +
                "...wbwww" +
                "..wbbb.." +
                "..w....." +
                "..w.....";
        Board board2 = new Board(state);

        AdaptiveStrategy strategy = new AdaptiveStrategy();

        int value1 = strategy.determineBoardValue(BoardValue.WHITE, board1);
        int value2 = strategy.determineBoardValue(BoardValue.WHITE, board2);

        assertTrue(value1 < value2);
    }
}