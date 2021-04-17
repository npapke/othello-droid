package ca.provenpath.othello.game.observer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Getter
public class EngineNotification extends GameNotification {

    int boardsEvaluated;
    long elapsedMs;
    int depth;
}
