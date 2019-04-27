package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.Move;
import ca.provenpath.othello.game.Player;

public class GameNotification {
    private GameState gameState;
    private Board board;
    private Move consideredMove;
    private Player[] player;

    public GameNotification(GameState gameState, Board board, Move consideredMove) {
        this.gameState = gameState;
        this.board = board;
        this.consideredMove = consideredMove;
        this.player = new Player[2];
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

    public Player[] getPlayer() {
        return player;
    }

    public void setPlayer(Player[] player) {
        this.player = player;
    }
}
