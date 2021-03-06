package de.rkirchner.podzeit.di;

import android.support.v4.app.FragmentManager;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.ui.common.NavigationController;
import de.rkirchner.podzeit.ui.episodedetails.EpisodeDetailsFragment;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListClickCallback;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListFragment;
import de.rkirchner.podzeit.ui.player.PlayerFragment;
import de.rkirchner.podzeit.ui.player.PlayerVisibilityListener;
import de.rkirchner.podzeit.ui.playlist.PlaylistFragment;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridClickCallback;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridFragment;

@Module
public abstract class FragmentBindingModule {

    @ContributesAndroidInjector
    abstract SeriesGridFragment seriesGridFragment();

    @ContributesAndroidInjector
    abstract PlayerFragment playerFragment();

    @ContributesAndroidInjector
    abstract PlaylistFragment playlistFragment();

    @ContributesAndroidInjector
    abstract EpisodeListFragment episodeListFragment();

    @ContributesAndroidInjector
    abstract EpisodeDetailsFragment episodeDetailsFragment();

    @Provides
    static FragmentManager provideFragmentManager(MainActivity mainActivity) {
        return mainActivity.getSupportFragmentManager();
    }

    @Binds
    abstract SeriesGridClickCallback bindSeriesGridClickCallback(NavigationController navigationController);

    @Binds
    abstract EpisodeListClickCallback bindEpisodeListClickCallback(NavigationController navigationController);

    @Binds
    abstract PlayerVisibilityListener bindPlayerVisibilityListener(NavigationController navigationController);
}
