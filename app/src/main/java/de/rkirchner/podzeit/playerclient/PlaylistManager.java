package de.rkirchner.podzeit.playerclient;

import android.net.Uri;
import android.os.Bundle;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

@Singleton
public class PlaylistManager implements IPlaylistManager {

    private PlaylistDao playlistDao;
    private EpisodeDao episodeDao;
    private AppExecutors appExecutors;
    private MediaSessionClient mediaSessionClient;

    @Inject
    public PlaylistManager(PlaylistDao playlistDao, EpisodeDao episodeDao, AppExecutors appExecutors, MediaSessionClient mediaSessionClient) {
        this.playlistDao = playlistDao;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.mediaSessionClient = mediaSessionClient;
    }

    @Override
    public void addEpisodeToPlaylist(int episodeId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.QUEUE_EPISODE_ID_KEY, episodeId);
        mediaSessionClient.getMediaController().sendCommand(Constants.QUEUE_COMMAND_ADD, bundle, null);
    }

    @Override
    public void removeEpisode(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(episodeId);
            List<PlaylistEntry> entriesToUpdate = playlistDao.getEntriesForReordering(playlistEntry.getPlaylistPosition());
            for (PlaylistEntry entry : entriesToUpdate) {
                entry.setPlaylistPosition(entry.getPlaylistPosition() - 1);
            }
            playlistDao.updateEntries(entriesToUpdate);
            playlistDao.removeEpisodeFromPlaylist(playlistEntry);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.QUEUE_PLAYLIST_POSITION_KEY, playlistEntry.getPlaylistPosition());
            mediaSessionClient.getMediaController().sendCommand(Constants.QUEUE_COMMAND_REMOVE, bundle, null);
        });
    }

    @Override
    public void moveEpisode(int startPosition, int endPosition) {
        Timber.d("Episode moved from %s to %s", startPosition, endPosition);
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry itemToMove = playlistDao.getPlaylistEntryAtPosition(startPosition);
            List<PlaylistEntry> itemsToReorder;
            if (startPosition < endPosition) {
                itemsToReorder = playlistDao.getEntriesForReordering(startPosition + 1, endPosition);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() - 1);
                    Timber.d("Item moved from %s to %s", itemToReorder.getPlaylistPosition(), itemToReorder.getPlaylistPosition() - 1);
                }
            } else {
                itemsToReorder = playlistDao.getEntriesForReordering(endPosition, startPosition - 1);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() + 1);
                    Timber.d("Item moved from %s to %s", itemToReorder.getPlaylistPosition(), itemToReorder.getPlaylistPosition() + 1);
                }
            }
            itemToMove.setPlaylistPosition(endPosition);
            itemsToReorder.add(itemToMove);
            playlistDao.updateEntries(itemsToReorder);
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.QUEUE_MOVE_FROM_KEY, startPosition);
            bundle.putInt(Constants.QUEUE_MOVE_TO_KEY, endPosition);
            mediaSessionClient.getMediaController().sendCommand(Constants.QUEUE_COMMAND_MOVE, bundle, null);
        });
    }

    @Override
    public void playNow(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeForId(episodeId);
            mediaSessionClient.getTransportControls().playFromUri(Uri.parse(episode.getUrl()), null);
        });
    }

    @Override
    public void triggerMediaSourceRebuild() {
        mediaSessionClient.getMediaController().sendCommand(Constants.QUEUE_COMMAND_REBUILD_MEDIA_SOURCE, null, null);
    }
}
