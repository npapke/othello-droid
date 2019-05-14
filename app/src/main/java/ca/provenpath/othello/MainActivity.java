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

package ca.provenpath.othello;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import ca.provenpath.othello.game.BoardValue;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.GameExecutorSerializer;
import ca.provenpath.othello.game.Player;
import ca.provenpath.othello.game.observer.AnalysisNotification;
import ca.provenpath.othello.game.observer.EngineNotification;
import ca.provenpath.othello.game.observer.GameNotification;
import ca.provenpath.othello.game.observer.MoveNotification;
import ca.provenpath.othello.persistence.KeyValue;
import ca.provenpath.othello.persistence.StateDatabase;

import java.util.Arrays;
import java.util.Optional;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getName();

    /*
     * ====================================================================
     */
    // region [Life cycle methods]
    /*
     * --------------------------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        database = Room.databaseBuilder(this, StateDatabase.class, "othstate")
                .allowMainThreadQueries()
                .build();

        onRestoreInstanceState(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        mBoardAdaptor = new BoardAdapter(this);
        GridView gridview = findViewById(R.id.gridview);
        gridview.setAdapter(mBoardAdaptor);

        gridview.setOnItemClickListener(
                (parent, v, position, id) -> {
                    // Toast.makeText( MainActivity.this, "" + position, Toast.LENGTH_SHORT ).show();
                    attemptMove(position);
                });

        findViewById(R.id.undo).setOnClickListener(v -> undoGame());
        findViewById(R.id.newgame).setOnClickListener(v -> runGame(Optional.empty()));

        getSupportFragmentManager().findFragmentById(R.id.statistics_fragment);
        getSupportFragmentManager().findFragmentById(R.id.status_fragment);

        /*
         * Process requests on the UI thread.
         */
        mHandler =
                new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case MSG_NOTIFICATION: {
                                GameExecutor.Tracker tracker = (GameExecutor.Tracker) msg.obj;
                                updateDisplay(tracker);
                                break;
                            }

                            default:
                                super.handleMessage(msg);
                                break;
                        }
                    }
                };
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");

        super.onDestroy();

        database.close();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        super.onStart();

        if (!tracker.isPresent()) {
            Log.i(TAG, "restoring from DB");
            KeyValue kv = database.getKeyValueDao().getByKey(KEY_EXECUTOR);
            if (kv != null) {
                tracker = Optional.ofNullable(
                        GameExecutorSerializer.deserialize(kv.getValue()));
            }
        }

        // Do this after everything is initialized
        runGame(tracker);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();

        GameExecutor.instance()
                .getGameState()
                .map(tracker -> {
                    String serial = GameExecutorSerializer.serialize(tracker);
                    database.getKeyValueDao().insert(new KeyValue(KEY_EXECUTOR, serial));
                    return serial;
                });

        GameExecutor.instance().close();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        Log.i(TAG, "onSaveInstanceState");

        super.onSaveInstanceState(bundle);

        GameExecutor.instance()
                .getGameState()
                .map(tracker -> {
                    String serial = GameExecutorSerializer.serialize(tracker);
                    bundle.putString(KEY_EXECUTOR, serial);
                    return serial;
                });

    }

    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        Log.i(TAG, "onRestoreInstanceState");

        if (bundle != null) {
            super.onRestoreInstanceState(bundle);
            tracker = Optional.ofNullable(
                    GameExecutorSerializer.deserialize(bundle.getString(KEY_EXECUTOR)));
        }

    }

    // endregion

    /*
     * ====================================================================
     */
    // region [Option menu methods]
    /*
     * --------------------------------------------------------------------
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_about) {
            new AboutDialog().show(getFragmentManager(), null);
            return true;
        } else if (id == R.id.action_help) {
            // Cheat by sending user off to a web site
            Intent helpIntent = new Intent();
            helpIntent.setAction(Intent.ACTION_VIEW);
            helpIntent.setData(Uri.parse(getResources().getString(R.string.help_url)));

            // Verify that the intent will resolve to an activity
            if (helpIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(helpIntent);
                return true;
            } else {
                Log.w(TAG, "No takers for intent: " + getResources().getText(R.string.help_url));
            }
        } else if (id == R.id.action_exit) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    // endregion

    /*
     * ====================================================================
     *
     * Manage display
     *
     * --------------------------------------------------------------------
     */

    /**
     * (Re)initializes the game state.
     */
    private Optional<GameExecutor.Tracker> runGame(Optional<GameExecutor.Tracker> tracker) {
        GameExecutor.instance()
                .executeOneGame(
                        mHandler,
                        color -> getSharedPreferences(
                                PlayerSettingsFragment.getSharedPreferencesName(color.name()),
                                Context.MODE_PRIVATE),
                        tracker);
        return tracker;
    }

    /**
     * Restores an earlier game state, if possible.
     */
    private void undoGame() {
        GameExecutor.instance()
                .popUndoGameState()
                .flatMap(tracker -> runGame(Optional.of(tracker)));
    }

    private void updateDisplay(GameExecutor.Tracker tracker) {

        GameNotification notification = tracker.getNotification();
        if (notification == null || notification instanceof MoveNotification) {

            GameStatusFragment statusFragment =
                    (GameStatusFragment) getSupportFragmentManager().findFragmentById(R.id.status_fragment);

            if (statusFragment != null) {
                statusFragment.update(tracker);

                if (tracker == null) {
                    mBoardAdaptor.redraw(null, BoardValue.EMPTY);
                } else {
                    showValidMoves(tracker);
                }
            }
        } else if (notification instanceof AnalysisNotification) {
            mBoardAdaptor.draw((AnalysisNotification) notification);
        } else if (notification instanceof EngineNotification) {

            StatisticsFragment statisticsFragment =
                    (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.statistics_fragment);

            if (statisticsFragment != null) {
                statisticsFragment.update(tracker);
            }

        }

        findViewById(R.id.undo).setEnabled(
                GameExecutor.instance().peekUndoGameState().isPresent());
    }

    private void showValidMoves(GameExecutor.Tracker tracker) {
        // Only for human player
        Player player = tracker.getNextPlayer();

        if ((player != null) && !player.isComputer()) {
            BoardValue validMoveFilter =
                    (player.getColor() == BoardValue.BLACK) ? BoardValue.VALID_BLACK : BoardValue.VALID_WHITE;

            mBoardAdaptor.redraw(tracker.getBoard(), validMoveFilter);
        } else {
            mBoardAdaptor.redraw(tracker.getBoard(), BoardValue.EMPTY);
        }
    }

    private void attemptMove(int position) {
        Log.d(TAG, "Attempt move to " + position);
        Optional<GameExecutor.Tracker> oTracker = GameExecutor.instance().getGameState();
        if (oTracker.isPresent()) {
            Arrays.stream(oTracker.get().getPlayer())
                    .forEach(player -> player.offerMove(position));
        }
    }

    public static final int MSG_NOTIFICATION = 342;

    private static final String KEY_EXECUTOR = MainActivity.class.getName() + ".executor-1";
    private static final String KEY_LAST_MOVE = MainActivity.class.getName() + ".lastMove-1";

    private Handler mHandler;
    private BoardAdapter mBoardAdaptor;
    private Optional<GameExecutor.Tracker> tracker = Optional.empty();
    private StateDatabase database;
}
