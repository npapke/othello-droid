package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Board;
import ca.provenpath.othello.game.Position;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class AnalysisBoardNotification extends AnalysisNotification {

    public AnalysisBoardNotification(int value, Position position, boolean important, Board board) {

        super(value, position, important);
        this.board = board;
    }

    Board board;
}
