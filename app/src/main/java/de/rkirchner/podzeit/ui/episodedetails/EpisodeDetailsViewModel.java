package de.rkirchner.podzeit.ui.episodedetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class EpisodeDetailsViewModel extends ViewModel {

    private EpisodeDao episodeDao;
    private MutableLiveData<Integer> episodeId = new MutableLiveData<>();
    private PlaylistManager playlistManager;

    @Inject
    public EpisodeDetailsViewModel(EpisodeDao episodeDao, PlaylistManager playlistManager) {
        this.episodeDao = episodeDao;
        this.playlistManager = playlistManager;
    }

    public void playEpisode() {
        playlistManager.addEpisodeAndPlayNow(episodeId.getValue());
    }

    public LiveData<EpisodesPlaylistJoin> getEpisode() {
        return Transformations.switchMap(episodeId, id -> episodeDao.getEpisodesPlaylistJoinForEpisode(id));
    }

    public void addToPlaylist() {
        playlistManager.addEpisodeToPlaylist(episodeId.getValue());
    }

    public void removeFromPlaylist() {
        playlistManager.removeEpisodeFromPlaylist(episodeId.getValue());
    }

    public void setEpisodeId(int id) {
        episodeId.setValue(id);
    }
}
