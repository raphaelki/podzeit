package de.rkirchner.podzeit.ui.common;

import android.content.Context;
import android.content.Intent;
import android.support.transition.Fade;
import android.support.transition.Slide;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;

import javax.inject.Inject;

import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.SettingsFragment;
import de.rkirchner.podzeit.ui.episodedetails.EpisodeDetailsFragment;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListClickCallback;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListFragment;
import de.rkirchner.podzeit.ui.logindialog.LoginActivity;
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
    public NavigationController(FragmentManager fragmentManager, Context context) {
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    private void showPlayer() {
        Fragment playerFragment = fragmentManager.findFragmentByTag(PLAYER_FRAGMENT_TAG);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (playerFragment == null) {
            Fragment fragment = new PlayerFragment();
            fragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            fragmentTransaction.add(PLAYER_FRAGMENT_FRAME_ID, fragment, PLAYER_FRAGMENT_TAG);
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
        PlaylistFragment fragment = new PlaylistFragment();
        setTransitions(fragment);
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(UPPER_FRAGMENT_FRAME_ID, fragment)
                .commit();
    }

    public void navigateToSeriesGrid() {
        SeriesGridFragment fragment = new SeriesGridFragment();
        setTransitionAndNavigateToFragment(fragment);
    }

    private void navigateToEpisodeList(String rssUrl) {
        Fragment fragment = EpisodeListFragment.create(rssUrl);
        setTransitionAndNavigateToFragment(fragment);
    }

    private void navigateToEpisodeDetails(int episodeId) {
        Fragment fragment = EpisodeDetailsFragment.create(episodeId);
        setTransitionAndNavigateToFragment(fragment);
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

    private void setTransitions(Fragment fragment) {
        fragment.setExitTransition(new Fade());
        fragment.setEnterTransition(new Slide(Gravity.RIGHT));
    }

    public void navigateToSettings() {
        SettingsFragment fragment = new SettingsFragment();
        setTransitionAndNavigateToFragment(fragment);
        hidePlayer();
    }

    private void setTransitionAndNavigateToFragment(Fragment fragment) {
        setTransitions(fragment);
        fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .replace(UPPER_FRAGMENT_FRAME_ID, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void showLoginDialog(String uriAuthority) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Constants.URI_AUTHORITY_KEY, uriAuthority);
        context.startActivity(intent);
    }
}
