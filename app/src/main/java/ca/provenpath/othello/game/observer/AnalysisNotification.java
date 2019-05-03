package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Position;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
public class AnalysisNotification extends GameNotification {

    int value;
    Position position;
    boolean important;
}
