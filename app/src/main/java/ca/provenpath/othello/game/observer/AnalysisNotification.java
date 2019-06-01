package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Position;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
public class AnalysisNotification extends GameNotification {

    @Setter
    int value;

    Position position;
    boolean important;
}
