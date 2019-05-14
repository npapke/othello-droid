package ca.provenpath.othello.persistence;

import android.arch.persistence.room.*;

@Dao
public interface KeyValueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(KeyValue... items);

    @Delete
    void delete(KeyValue item);

    @Query("SELECT * FROM keyvalues WHERE `key` = :key")
    KeyValue getByKey(String key);
}
