package ca.provenpath.othello.game.observer;

import ca.provenpath.othello.game.Move;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
public class MoveNotification extends GameNotification {
    Move move;
    Instant gameStart;
}
