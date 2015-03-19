package ca.provenpath.othello.game;

import com.google.gson.Gson;

import junit.framework.TestCase;

public class GameExecutorTest extends TestCase
{
    public void testSerialization() throws Exception
    {
        Gson gson = new Gson();

        GameExecutor ge = new GameExecutor();
        ge.newGame();

        String serialized = gson.toJson( ge );
        assertNotNull( serialized );

        GameExecutor ge2 = gson.fromJson( serialized, GameExecutor.class );

        assertNotNull( ge2 );
        //assertEquals( ge, ge2 );
    }

    public void testNewGame() throws Exception
    {
        GameExecutor ge = new GameExecutor();
        assertNotNull( ge );
    }

    public void testEndGame() throws Exception
    {

    }

    public void testExecuteOneTurn() throws Exception
    {

    }
}