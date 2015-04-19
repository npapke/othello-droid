package ca.provenpath.othello.game;

/**
 * A Strategy that simply optimizes the number of own pieces.
 * Created by npapke on 4/19/15.
 */
public class StrategyFactory
{
    public static Strategy getObject( String type )
    {
        switch (type.toLowerCase())
        {
            case "adaptive":
                return new AdaptiveStrategy();

            case "greedy":
                return new GreedyStrategy();

            case "static":
            default:
                return new StaticStrategy();
        }
    }
}
