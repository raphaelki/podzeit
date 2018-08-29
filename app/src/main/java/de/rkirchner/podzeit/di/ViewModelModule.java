package de.rkirchner.podzeit.di;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.rkirchner.podzeit.ui.common.ViewModelFactory;
import de.rkirchner.podzeit.ui.episodedetails.EpisodeDetailsViewModel;
import de.rkirchner.podzeit.ui.episodelist.EpisodeListViewModel;
import de.rkirchner.podzeit.ui.logindialog.LoginViewModel;
import de.rkirchner.podzeit.ui.player.PlayerViewModel;
import de.rkirchner.podzeit.ui.playlist.PlaylistViewModel;
import de.rkirchner.podzeit.ui.seriesgrid.SeriesGridViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory viewModelFactory);

    @Binds
    @IntoMap
    @ViewModelKey(SeriesGridViewModel.class)
    abstract ViewModel bindSeriesGridViewModel(SeriesGridViewModel seriesGridViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EpisodeListViewModel.class)
    abstract ViewModel bindEpisodeListViewModel(EpisodeListViewModel episodeListViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(EpisodeDetailsViewModel.class)
    abstract ViewModel bindEpisodeDetailsViewModel(EpisodeDetailsViewModel episodeDetailsViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlaylistViewModel.class)
    abstract ViewModel bindPlaylistViewModel(PlaylistViewModel playlistViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(PlayerViewModel.class)
    abstract ViewModel bindPlayerViewModel(PlayerViewModel playerViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(LoginViewModel.class)
    abstract ViewModel bindLoginViewModel(LoginViewModel loginViewModel);
}
