package ca.provenpath.othello.game;

/**
 * Created by npapke on 3/30/15.
 */
public class AdaptiveStrategy extends Strategy
{
    @Override
    public int determineBoardValue( BoardValue player, Board board )
    {
        int score = 0;
        int protectedScore = 0;
        int freedom = 0;
        int numMoves = 0;

        BoardValue otherPlayer = player.otherPlayer();

        for (Position pos : board)
        {
            BoardValue curCell = board.getValue( pos );

            if (curCell == player)
            {
                score++;
                numMoves++;
                if (board.isProtected( pos ))
                    protectedScore++;
            }
            else if (curCell == otherPlayer)
            {
                score--;
                numMoves++;
                if (board.isProtected( pos ))
                    protectedScore--;
            }
            else if (board.isValidMove( new Move( player, pos ) ))
            {
                freedom++;
            }
        }

        int finalScore;

        // TODO proteced should be a ranged value rather than a boolean

        freedom = scale( freedom, 0, 16 );
        score = scale( score, -64, 64 );
        protectedScore = scale( protectedScore, -64, 64 );

        if (numMoves < 12)
        {
            finalScore = (freedom * 30) + (score * 70);
        }
        else if (numMoves < 50)
        {
            finalScore = (freedom * 20) + (protectedScore * 50) + (score * 30);
        }
        else
        {
            finalScore = (protectedScore * 30) + (score * 70);
        }

        return finalScore;
    }

    private int scale( int value, int min, int max )
    {
        int clipped = Math.min( Math.max( value, min ), max );

        return (clipped - min) * 100 / max;
    }
}
