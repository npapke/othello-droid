package ca.provenpath.othello.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {KeyValue.class}, version = 1, exportSchema = false)
public abstract class StateDatabase extends RoomDatabase {

    public abstract KeyValueDao getKeyValueDao();
}
