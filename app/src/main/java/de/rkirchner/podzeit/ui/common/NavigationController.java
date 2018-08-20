package de.rkirchner.podzeit.ui.common;

import android.content.Context;
import android.support.v4.app.FragmentManager;

import javax.inject.Inject;

import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.ui.episodedetails.EpisodeDetailsFragment;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListClickCallback;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListFragment;
import de.rkirchner.podzeit.ui.player.PlayerFragment;
import de.rkirchner.podzeit.ui.playlist.PlaylistFragment;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridClickCallback;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridFragment;

public class NavigationController implements SeriesGridClickCallback, EpisodeListClickCallback {

    private final int PLAYER_FRAGMENT_FRAME_ID = R.id.player_fragment_frame;
    private final int UPPER_FRAGMENT_FRAME_ID = R.id.upper_fragment_frame;
    private Context context;
    private FragmentManager fragmentManager;

    @Inject
    public NavigationController(MainActivity activity, Context context) {
        this.fragmentManager = activity.getSupportFragmentManager();
        this.context = context;
    }

    public void bindPlayer() {
        fragmentManager.beginTransaction()
                .add(PLAYER_FRAGMENT_FRAME_ID, new PlayerFragment())
                .commit();
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
}
