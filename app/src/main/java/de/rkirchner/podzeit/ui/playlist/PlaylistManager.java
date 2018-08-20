package de.rkirchner.podzeit.ui.playlist;

import android.support.annotation.WorkerThread;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.PlaylistEntry;

@Singleton
public class PlaylistManager {

    private PlaylistDao playlistDao;
    private AppExecutors appExecutors;

    @Inject
    public PlaylistManager(PlaylistDao playlistDao, AppExecutors appExecutors) {
        this.appExecutors = appExecutors;
        this.playlistDao = playlistDao;
    }

    public void addEpisodeToPlaylist(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            int currentEpisodeCount = playlistDao.getPlaylistEntriesSync().size();
            playlistDao.insertEntry(new PlaylistEntry(episodeId, currentEpisodeCount));
        });
    }

    public void removeEpisodeFromPlaylistAtPlaylistPosition(int playlistPosition) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry entry = playlistDao.getPlaylistEntryAtPosition(playlistPosition);
            removePlaylistEntry(entry);
        });
    }

    public void removeEpisodeFromPlaylist(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry entry = playlistDao.getPlaylistEntry(episodeId);
            removePlaylistEntry(entry);
        });
    }

    public void movePlaylistEntry(int startPosition, int endPosition) {
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

    @WorkerThread
    private void removePlaylistEntry(PlaylistEntry entry) {
        List<PlaylistEntry> entriesToReorder = playlistDao.getEntriesForReordering(entry.getPlaylistPosition());
        for (PlaylistEntry entryToReorder : entriesToReorder) {
            entryToReorder.setPlaylistPosition(entryToReorder.getPlaylistPosition() - 1);
        }
        playlistDao.updateEntries(entriesToReorder);
        playlistDao.removeEpisodeFromPlaylist(entry);
    }
}
