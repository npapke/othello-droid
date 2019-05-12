package ca.provenpath.othello;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.provenpath.othello.game.ComputerPlayer;
import ca.provenpath.othello.game.GameExecutor;
import ca.provenpath.othello.game.Player;
import ca.provenpath.othello.game.observer.EngineNotification;

import java.util.Locale;


/**
 *
 */
public class StatisticsFragment extends Fragment {

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatisticsFragment.
     */
    public static StatisticsFragment newInstance() {
        StatisticsFragment fragment = new StatisticsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    public void update(GameExecutor.Tracker tracker) {

        StringBuilder playerText = new StringBuilder();
        StringBuilder progressText = new StringBuilder();

        Player player = tracker.getNextPlayer();

        playerText.append(player.getColor().toString());
        if (player.isComputer()) {

            ComputerPlayer computer = (ComputerPlayer) player;

            playerText.append(" - ");
            playerText.append(computer.getStrategy().toString());
            playerText.append(" - ");
            playerText.append(computer.getMaxDepth());
        }

        if (tracker.getNotification() instanceof EngineNotification) {

            EngineNotification engine = (EngineNotification) tracker.getNotification();

            progressText.append(
                    String.format(Locale.US, "%d boards in %d ms",
                            engine.getBoardsEvaluated(), engine.getElapsedMs()));

            if (engine.getElapsedMs() != 0) {
                progressText.append(String.format(Locale.US, " - %.3f boards / sec",
                        (double) engine.getBoardsEvaluated() / (double) engine.getElapsedMs() * 1000.0));
            }
        }

        drawText(R.id.statistics_player, playerText.toString());
        drawText(R.id.statistics_progress, progressText.toString());

    }

    private void drawText(int viewId, String msg) {
        View view = getView();
        if (view != null) {
            view = view.findViewById(viewId);
            if (view instanceof TextView) {
                ((TextView) view).setText(msg);
            }
        }
    }
}
