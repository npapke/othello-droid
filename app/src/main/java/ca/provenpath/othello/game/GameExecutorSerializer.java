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

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serializes and deserializes GameExcutors.
 *
 * Note: The reason this exists is because Players
 * are polymorphic and Gson (really Json) cannot handle it
 * without help.
 *
 * Created by npapke on 3/19/15.
 */
public class GameExecutorSerializer
{
    public final static String TAG = GameExecutorSerializer.class.getName();

    public static GameExecutor deserialize( String serial )
    {
        GameExecutor executor = null;

        try
        {
            Gson deserializer = new GameExecutorSerializer().makeGson();
            Log.d( TAG, "serialized=" + serial );

            executor = deserializer.fromJson( serial, GameExecutor.class );
            if (executor != null && !executor.isConsistent())
            {
                executor = null;
            }
        }
        catch (Exception e)
        {
            Log.w( TAG, "deserialize", e );
        }

        if (executor == null)
        {
            executor = new GameExecutor();
            executor.newGame();
        }

        return executor;
    }

    public static String serialize( GameExecutor src )
    {
        try
        {
            Gson serializer = new GameExecutorSerializer().makeGson();

            // FIXME Thread-safety
            String data = serializer.toJson( src );

            Log.d( TAG, "Serialized=" + data );

            return data;
        }
        catch (Exception e)
        {
            Log.w( TAG, "Serialization failed", e );
            return null;
        }
    }

    private Gson makeGson()
    {
        GsonBuilder gson = new GsonBuilder()
            .registerTypeAdapter( Player.class, new PlayerDeserializer() )
            .registerTypeAdapter( Player.class, new PlayerSerializer() )
            .serializeNulls();

        return gson.create();
    }

    /*
     * ==============================================================
     *
     * Serialize Player subclasses as, e.g.,
     * ca.provenpath.othello.game.ComputerPlayer;WHITE
     *
     */

    private class PlayerDeserializer implements JsonDeserializer<Player>
    {
        public Player deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context )
                throws JsonParseException
        {
            try
            {
                String serial = json.getAsJsonPrimitive().getAsString();
                Log.d( TAG, "PlayerSerializer deserialize " + serial );

                String[] fields = serial.split( ";", 2 );
                String playerClassName = fields[0];
                String playerSerial = fields[1];

                Player player = (Player)
                        Class.forName( playerClassName ).getConstructor( String.class ).newInstance( playerSerial );

                return player;
            }
            catch (Exception e)
            {
                Log.w( TAG, "Serialization failed", e );
                return null;
            }
        }
    }

    private class PlayerSerializer implements JsonSerializer<Player>
    {
        public JsonElement serialize( Player src, Type typeOfSrc, JsonSerializationContext context )
        {
            StringBuilder out = new StringBuilder();
            out.append( src.getClass().getCanonicalName() );
            out.append( ';' );
            out.append( src.toString() );

            Log.d( TAG, "PlayerSerializer serialize " + out.toString() );

            return new JsonPrimitive( out.toString() );
        }
    }

}
