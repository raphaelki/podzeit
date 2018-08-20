package de.rkirchner.podzeit.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.rkirchner.podzeit.data.models.PlaylistEntry;
import de.rkirchner.podzeit.ui.playlist.EpisodePlaylistEntryJoin;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertEntry(PlaylistEntry playlistEntry);

    @Query(value = "SELECT id, title, playlistPosition, duration FROM podcast_episodes INNER JOIN playlist ON playlist.episodeId = id ORDER BY playlistPosition ASC")
    LiveData<List<EpisodePlaylistEntryJoin>> getEpisodesInPlaylist();

    @Query(value = "SELECT * FROM playlist WHERE episodeId = :episodeId")
    PlaylistEntry getPlaylistEntry(int episodeId);

    @Query(value = "SELECT * FROM playlist WHERE playlistPosition = :position")
    PlaylistEntry getPlaylistEntryAtPosition(int position);

    @Query(value = "SELECT * FROM playlist WHERE playlistPosition > :positionToRemove")
    List<PlaylistEntry> getEntriesForReordering(int positionToRemove);

    @Query(value = "SELECT * FROM playlist WHERE playlistPosition >= :startPosition AND playlistPosition <= :endPosition")
    List<PlaylistEntry> getEntriesForReordering(int startPosition, int endPosition);

    @Query(value = "SELECT * FROM playlist")
    LiveData<List<PlaylistEntry>> getPlaylistEntries();

    @Query(value = "SELECT * FROM playlist")
    List<PlaylistEntry> getPlaylistEntriesSync();

    @Update
    void updateEntries(List<PlaylistEntry> entries);

    @Delete
    void removeEpisodeFromPlaylist(PlaylistEntry playlistEntry);
}
