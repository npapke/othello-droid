package ca.provenpath.othello.game;

import org.junit.Test;

import static org.junit.Assert.*;

public class StrategyTest {

    @Test
    public void normalizeScore() {

        assertEquals(0, Strategy.normalizeScore(0));
        assertEquals(1, Strategy.normalizeScore(1));
        assertEquals(-1, Strategy.normalizeScore(-1));
        assertEquals(0, Strategy.normalizeScore(Strategy.WIN_BASE));
        assertEquals(2, Strategy.normalizeScore(Strategy.WIN_BASE + 2));
        assertEquals(Board.BOARD_LSIZE, Strategy.normalizeScore(Strategy.WIN_BASE + Board.BOARD_LSIZE));
        assertEquals(0, Strategy.normalizeScore(Strategy.LOSS_BASE));
        assertEquals(-1, Strategy.normalizeScore(Strategy.LOSS_BASE + 1));
        assertEquals(-Board.BOARD_LSIZE, Strategy.normalizeScore(Strategy.LOSS_BASE + Board.BOARD_LSIZE));
    }
}