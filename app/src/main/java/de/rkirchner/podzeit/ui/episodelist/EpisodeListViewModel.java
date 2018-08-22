package de.rkirchner.podzeit.ui.episodelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.SeriesDao;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class EpisodeListViewModel extends ViewModel {

    private final MutableLiveData<String> seriesRssUrl = new MutableLiveData<>();
    private EpisodeDao episodeDao;
    private SeriesDao seriesDao;
    private PlaylistManager playlistManager;

    @Inject
    public EpisodeListViewModel(EpisodeDao episodeDao, SeriesDao seriesDao, PlaylistManager playlistManager) {
        this.episodeDao = episodeDao;
        this.seriesDao = seriesDao;
        this.playlistManager = playlistManager;
    }

    public LiveData<Series> getSeries() {
        return Transformations.switchMap(seriesRssUrl, rssUrl -> seriesDao.getSeries(rssUrl));
    }

    public void setSeries(String rssUrl) {
        seriesRssUrl.setValue(rssUrl);
    }

    public LiveData<List<EpisodesPlaylistJoin>> getEpisodes() {
        return Transformations.switchMap(seriesRssUrl, rssUrl -> episodeDao.getEpisodesPlaylistJoinForSeries(rssUrl));
    }

    public void addEpisodeToPlaylist(int episodeId) {
        playlistManager.addEpisodeToPlaylist(episodeId);
    }

    public void removeEpisodeFromPlaylist(int episodeId) {
        playlistManager.removeEpisodeFromPlaylist(episodeId);
    }
}
