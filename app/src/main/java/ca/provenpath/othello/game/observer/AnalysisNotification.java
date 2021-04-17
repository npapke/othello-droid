package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Position;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(callSuper = true)
@Getter
public class AnalysisNotification extends GameNotification {

    public AnalysisNotification(int value, Position position, boolean important) {
        this.value = value;
        this.position = position;
        this.important = important;
    }

    @Setter
    int value;

    Position position;
    boolean important;
}
