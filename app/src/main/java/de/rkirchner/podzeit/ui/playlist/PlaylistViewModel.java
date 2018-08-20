package de.rkirchner.podzeit.ui.playlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.local.PlaylistDao;

public class PlaylistViewModel extends ViewModel {

    private PlaylistDao playlistDao;

    @Inject
    public PlaylistViewModel(PlaylistDao playlistDao) {
        this.playlistDao = playlistDao;
    }

    public LiveData<List<EpisodePlaylistEntryJoin>> getPlaylistEpisodes() {
        return playlistDao.getEpisodesInPlaylist();
    }
}
