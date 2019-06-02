package ca.provenpath.othello.game;

import android.util.Log;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * A cache for evaluated boards
 */
public class TranspositionTable {
    public final static String TAG = TranspositionTable.class.getSimpleName();

    public enum Flag {
        EXACT,
        LOWERBOUND,
        UPPERBOUND
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Entry {
        int value;
        int depth;
        Flag flag;
        Board board;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Entry)) return false;

            Entry entry = (Entry) o;

            if (value != entry.value) return false;
            if (depth != entry.depth) return false;
            if (flag != entry.flag) return false;
            return board != null ? board.equals(entry.board) : entry.board == null;
        }

        @Override
        public int hashCode() {
            return board.hashCode();
        }
    }

    private Map<Integer, Entry> table;

    public TranspositionTable() {
        table = new HashMap<>(1000000);
    }

    public void put(Entry entry) {
        table.put(entry.hashCode(), entry);
    }

    public Entry get(Board board) {
        Entry entry = table.get(board.hashCode());
        if (entry != null) {
            if (entry.getBoard().equals(board)) {
                //Log.d(TAG, entry.getFlag().toString() + " Cache hit at depth " + entry.getDepth() );
                return entry;
            } else {
                Log.d(TAG, "Cache collision");
            }
        }
        return null;
    }

}
