package de.rkirchner.podzeit.ui.playlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.playerclient.MediaSessionClient;

public class PlaylistViewModel extends ViewModel {

    private PlaylistDao playlistDao;
    private MediaSessionClient mediaSessionClient;

    @Inject
    public PlaylistViewModel(PlaylistDao playlistDao, MediaSessionClient mediaSessionClient) {
        this.playlistDao = playlistDao;
        this.mediaSessionClient = mediaSessionClient;

    }

    public LiveData<List<EpisodePlaylistEntryJoin>> getPlaylistEpisodes() {
        return playlistDao.getEpisodesInPlaylist();
    }

    public void startPlayback() {
        mediaSessionClient.getTransportControls().play();
    }
}
