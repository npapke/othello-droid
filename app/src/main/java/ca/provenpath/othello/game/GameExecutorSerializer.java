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
        try
        {
            Gson deserializer = new GameExecutorSerializer().makeGson();
            Log.i( TAG, "serialized=" + serial );

            GameExecutor ge = deserializer.fromJson( serial, GameExecutor.class );

            // Rehydrated observers are stale
            ge.deleteObservers();

            return ge;
        }
        catch (Exception e)
        {
            Log.w( TAG, "deserialize", e );
            return null;
        }
    }

    public static String serialize( GameExecutor src )
    {
        try
        {
            Gson serializer = new GameExecutorSerializer().makeGson();

            // FIXME Thread-safety
            String data = serializer.toJson( src );

            Log.i( TAG, "Serialized=" + data );

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
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter( Player.class, new PlayerDeserializer() );
        gson.registerTypeAdapter( Player.class, new PlayerSerializer() );
        Gson serializer = gson.serializeNulls().create();

        return serializer;
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
                Log.i( TAG, "PlayerSerializer deserialize " + serial );

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

            Log.i( TAG, "PlayerSerializer serialize " + out.toString() );

            return new JsonPrimitive( out.toString() );
        }
    }

}
