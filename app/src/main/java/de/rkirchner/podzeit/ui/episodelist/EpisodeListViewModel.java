package de.rkirchner.podzeit.ui.episodelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.DataState;
import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class EpisodeListViewModel extends ViewModel {

    private final MutableLiveData<String> seriesRssUrl = new MutableLiveData<>();
    private PodcastRepository repository;
    private PlaylistManager playlistManager;

    @Inject
    public EpisodeListViewModel(PodcastRepository repository, PlaylistManager playlistManager) {
        this.repository = repository;
        this.playlistManager = playlistManager;
    }

    public LiveData<Series> getSeries() {
        return Transformations.switchMap(seriesRssUrl, rssUrl -> repository.getSeries(rssUrl));
    }

    public void setSeries(String rssUrl) {
        seriesRssUrl.setValue(rssUrl);
    }

    public LiveData<List<EpisodesPlaylistJoin>> getEpisodes() {
        return Transformations.switchMap(seriesRssUrl, rssUrl -> repository.getEpisodesPlaylistJoinForSeries(rssUrl));
    }

    public void addEpisodeToPlaylist(int episodeId) {
        playlistManager.addEpisodeToPlaylist(episodeId);
    }

    public void removeEpisodeFromPlaylist(int episodeId) {
        playlistManager.removeEpisodeFromPlaylist(episodeId);
    }

    public void playNow(int episodeId) {
        playlistManager.addEpisodeAndPlayNow(episodeId);
    }

    public void triggerRefresh() {
        repository.triggerRefreshForRssUrl(seriesRssUrl.getValue());
    }

    public LiveData<DataState> getRefreshStatus() {
        return repository.getRefreshDataState();
    }
}
