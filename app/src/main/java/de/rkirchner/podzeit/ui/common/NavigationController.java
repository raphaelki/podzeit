package de.rkirchner.podzeit.ui.common;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import javax.inject.Inject;

import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.ui.episodedetails.EpisodeDetailsFragment;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListClickCallback;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListFragment;
import de.rkirchner.podzeit.ui.player.PlayerFragment;
import de.rkirchner.podzeit.ui.player.PlayerVisibilityListener;
import de.rkirchner.podzeit.ui.playlist.PlaylistFragment;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridClickCallback;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridFragment;

public class NavigationController implements SeriesGridClickCallback, EpisodeListClickCallback, PlayerVisibilityListener {

    private final int PLAYER_FRAGMENT_FRAME_ID = R.id.player_fragment_frame;
    private final int UPPER_FRAGMENT_FRAME_ID = R.id.upper_fragment_frame;
    private final String PLAYER_FRAGMENT_TAG = "player_fragment";
    private Context context;
    private FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity activity, Context context) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
    }

    private void showPlayer() {
        Fragment playerFragment = fragmentManager.findFragmentByTag(PLAYER_FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (playerFragment == null) {
            fragmentTransaction.add(PLAYER_FRAGMENT_FRAME_ID, new PlayerFragment(), PLAYER_FRAGMENT_TAG);
        } else {
            fragmentTransaction.show(playerFragment);
        }
        fragmentTransaction.commit();
    }

    private void hidePlayer() {
        Fragment playerFragment = fragmentManager.findFragmentByTag(PLAYER_FRAGMENT_TAG);
        if (playerFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(playerFragment)
                    .commit();
        }
    }

    public void navigateToPlaylist() {
        fragmentManager.beginTransaction()
                .replace(UPPER_FRAGMENT_FRAME_ID, new PlaylistFragment())
                .commit();
    }

    public void navigateToSeriesGrid() {
        fragmentManager.beginTransaction()
                .replace(UPPER_FRAGMENT_FRAME_ID, new SeriesGridFragment())
                .addToBackStack(null)
                .commit();
    }

    private void navigateToEpisodeList(String rssUrl) {
        fragmentManager.beginTransaction()
                .replace(UPPER_FRAGMENT_FRAME_ID, EpisodeListFragment.create(rssUrl))
                .addToBackStack(null)
                .commit();
    }

    private void navigateToEpisodeDetails(int episodeId) {
        fragmentManager.beginTransaction()
                .replace(UPPER_FRAGMENT_FRAME_ID, EpisodeDetailsFragment.create(episodeId))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onSeriesSelected(String rssUrl) {
        navigateToEpisodeList(rssUrl);
    }

    @Override
    public void onEpisodeSelected(int episodeId) {
        navigateToEpisodeDetails(episodeId);
    }

    @Override
    public void onToggleVisibility(boolean showPlayer) {
        if (showPlayer) showPlayer();
        else hidePlayer();
    }
}
