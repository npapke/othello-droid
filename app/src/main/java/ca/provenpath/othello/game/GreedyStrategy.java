package ca.provenpath.othello.game;

/**
 * Strategy that maximizes own pieces.
 * Created by npapke on 4/19/15.
 */
public class GreedyStrategy extends Strategy
{
    public int determineBoardValue( BoardValue player, Board board )
    {
        return determineFinalScore( player, board );
    }
}
