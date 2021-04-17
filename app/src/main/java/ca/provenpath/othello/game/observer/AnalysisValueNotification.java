package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Position;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class AnalysisValueNotification extends AnalysisNotification {

    public AnalysisValueNotification(int value, Position position, boolean important) {
        super(value, position, important);
    }
}
