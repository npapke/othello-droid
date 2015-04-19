package ca.provenpath.othello.game;

/**
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

            default:
                return new Strategy();
        }
    }
}
