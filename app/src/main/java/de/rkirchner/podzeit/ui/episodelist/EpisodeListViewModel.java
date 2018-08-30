package de.rkirchner.podzeit.ui.episodelist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.DataState;
import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class EpisodeListViewModel extends ViewModel {

    private final MutableLiveData<String> seriesRssUrl = new MutableLiveData<>();
    private PodcastRepository repository;
    private PlaylistManager playlistManager;
    private Context context;

    @Inject
    public EpisodeListViewModel(PodcastRepository repository, PlaylistManager playlistManager, Context context) {
        this.repository = repository;
        this.context = context;
        this.playlistManager = playlistManager;
    }

    public LiveData<Series> getSeries() {
        return Transformations.switchMap(seriesRssUrl, rssUrl -> repository.getSeries(rssUrl));
    }

    public void setSeries(String rssUrl) {
        seriesRssUrl.setValue(rssUrl);
    }

    public LiveData<List<EpisodesPlaylistJoin>> getEpisodes() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hidePlayed = sharedPreferences.getBoolean(context.getString(R.string.shared_pref_hide_played_key), false);
        if (hidePlayed) {
            return Transformations.switchMap(seriesRssUrl,
                    rssUrl -> repository.getEpisodesPlaylistJoinForSeriesWithoutPlayed(rssUrl));
        }
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
