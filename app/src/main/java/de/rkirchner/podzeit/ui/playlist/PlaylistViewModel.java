package de.rkirchner.podzeit.ui.playlist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.EpisodePlaylistEntryJoin;
import de.rkirchner.podzeit.playerclient.MediaSessionClient;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class PlaylistViewModel extends ViewModel {

    private PlaylistDao playlistDao;
    private PlaylistManager playlistManager;
    private MediaSessionClient mediaSessionClient;
    private boolean playLastQueuedItem = false;

    private Observer<List<QueueItem>> observer = new Observer<List<QueueItem>>() {
        @Override
        public void onChanged(@Nullable List<QueueItem> queueItems) {
            if (playLastQueuedItem) {
                QueueItem lastItem = queueItems.get(queueItems.size() - 1);
                mediaSessionClient.getTransportControls().skipToQueueItem(lastItem.getQueueId());
                playLastQueuedItem = false;
            }
        }
    };

    @Inject
    public PlaylistViewModel(PlaylistDao playlistDao, MediaSessionClient mediaSessionClient, PlaylistManager playlistManager) {
        this.playlistDao = playlistDao;
        this.mediaSessionClient = mediaSessionClient;
        this.mediaSessionClient.getQueueItems().observeForever(observer);
        this.playlistManager = playlistManager;
    }

    public LiveData<List<EpisodePlaylistEntryJoin>> getPlaylistEpisodes() {
        return playlistDao.getEpisodesInPlaylist();
    }

    public void startPlayback(EpisodePlaylistEntryJoin episode) {
        MediaDescriptionCompat mediaDescription = new MediaDescriptionCompat.Builder()
                .setTitle(episode.getTitle())
                .setMediaId(episode.getUrl())
                .setMediaUri(Uri.parse(episode.getUrl()))
                .build();
        mediaSessionClient.getMediaController().addQueueItem(mediaDescription);
        playLastQueuedItem = true;
    }

    public void startPlayback(int playlistPosition) {
        playlistManager.playPlaylistEntry(playlistPosition);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mediaSessionClient.getQueueItems().removeObserver(observer);
    }
}
