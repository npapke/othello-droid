package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Position;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AnalysisNotification extends GameNotification {

    int value;
    Position position;
}
