package de.rkirchner.podzeit.playerservice;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

import java.util.List;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;

public class QueueDataAdapterImpl implements TimelineQueueEditor.QueueDataAdapter {

    private ConcatenatingMediaSource concatenatingMediaSource;
    private MediaControllerCompat mediaControllerCompat;
    private MediaSessionCompat mediaSession;
    private AppExecutors appExecutors;
    private PlaylistDao playlistDao;
    private EpisodeDao episodeDao;

    public QueueDataAdapterImpl(AppExecutors appExecutors, PlaylistDao playlistDao, EpisodeDao episodeDao) {
        this.concatenatingMediaSource = concatenatingMediaSource;
        this.mediaControllerCompat = mediaControllerCompat;
        this.appExecutors = appExecutors;
        this.playlistDao = playlistDao;
        this.episodeDao = episodeDao;
    }

    @Override
    public MediaDescriptionCompat getMediaDescription(int position) {

        return null;
    }

    @Override
    public void add(int position, MediaDescriptionCompat description) {
        appExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeForUrl(description.getMediaUri().toString());
            PlaylistEntry playlistEntry = new PlaylistEntry(episode.getId(), position, false, 0);
            playlistDao.insertEntry(playlistEntry);
        });
    }

    @Override
    public void remove(int position) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry entryToRemove = playlistDao.getPlaylistEntryAtPosition(position);
            List<PlaylistEntry> entriesToReorder = playlistDao.getEntriesForReordering(position);
            for (PlaylistEntry entryToReorder : entriesToReorder) {
                entryToReorder.setPlaylistPosition(entryToReorder.getPlaylistPosition() - 1);
            }
            playlistDao.updateEntries(entriesToReorder);
            playlistDao.removeEpisodeFromPlaylist(entryToRemove);
        });
    }

    @Override
    public void move(int startPosition, int endPosition) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry itemToMove = playlistDao.getPlaylistEntryAtPosition(startPosition);
            List<PlaylistEntry> itemsToReorder;
            if (startPosition < endPosition) {
                itemsToReorder = playlistDao.getEntriesForReordering(startPosition + 1, endPosition);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() - 1);
                }
            } else {
                itemsToReorder = playlistDao.getEntriesForReordering(endPosition, startPosition - 1);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() + 1);
                }
            }
            itemToMove.setPlaylistPosition(endPosition);
            itemsToReorder.add(itemToMove);
            playlistDao.updateEntries(itemsToReorder);
        });
    }
}
