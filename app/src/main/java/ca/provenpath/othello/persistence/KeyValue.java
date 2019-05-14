package ca.provenpath.othello.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "keyvalues")
public class KeyValue {

    @PrimaryKey
    @NonNull
    private String key;
    private String value;

    // Lombok seems to clash with room


    public KeyValue() {
    }

    public KeyValue(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KeyValue)) return false;

        KeyValue keyValue = (KeyValue) o;

        if (!key.equals(keyValue.key)) return false;
        return value != null ? value.equals(keyValue.value) : keyValue.value == null;

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
