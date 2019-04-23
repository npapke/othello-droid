package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.Move;

public class GameNotification {
    private GameState gameState;
    private Board board;
    private Move consideredMove;

    public GameNotification(GameState gameState, Board board, Move consideredMove) {
        this.gameState = gameState;
        this.board = board;
        this.consideredMove = consideredMove;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public Move getConsideredMove() {
        return consideredMove;
    }

    public void setConsideredMove(Move consideredMove) {
        this.consideredMove = consideredMove;
    }
}
