package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Move;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MoveNotification extends GameNotification {
    Move move;
}
