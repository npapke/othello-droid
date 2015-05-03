/*
 *  Copyright (c) 2015 Norbert Papke <npapke@acm.org>
 *
 *  This file is part of Othello.
 *
 *  Othello is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Othello is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Othello.  If not, see <http://www.gnu.org/licenses/>.
 */

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