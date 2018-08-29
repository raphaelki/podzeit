package de.rkirchner.podzeit.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.rkirchner.podzeit.data.models.EpisodePlaylistEntryJoin;
import de.rkirchner.podzeit.data.models.PlaylistEntry;

@Dao
public interface PlaylistDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertEntry(PlaylistEntry playlistEntry);

    @Query(value = "SELECT id, isSelected, podcast_episodes.title AS title, playlistPosition, duration, url, podcast_series.title AS seriesTitle FROM podcast_episodes INNER JOIN playlist ON playlist.episodeId = id INNER JOIN podcast_series ON podcast_series.rss_url = series_rss_url ORDER BY playlistPosition ASC")
    LiveData<List<EpisodePlaylistEntryJoin>> getEpisodesInPlaylist();

    @Query(value = "SELECT id, isSelected, podcast_episodes.title AS title, playlistPosition, duration, url, podcast_series.title AS seriesTitle FROM podcast_episodes INNER JOIN playlist ON playlist.episodeId = id INNER JOIN podcast_series ON podcast_series.rss_url = series_rss_url WHERE isSelected = 1")
    EpisodePlaylistEntryJoin getSelectedEpisodeSync();

    @Query(value = "SELECT * FROM playlist WHERE isSelected=1")
    PlaylistEntry getSelectedPlaylistEntry();

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

    @Query(value = "SELECT * FROM playlist ORDER BY playlistPosition ASC")
    List<PlaylistEntry> getPlaylistEntriesSync();

    @Update
    void updateEntries(List<PlaylistEntry> entries);

    @Update
    void updateEntry(PlaylistEntry... entry);

    @Delete
    void removeEpisodeFromPlaylist(PlaylistEntry playlistEntry);
}
