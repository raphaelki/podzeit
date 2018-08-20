package de.rkirchner.podzeit.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.ui.episodelist.EpisodesPlaylistJoin;

@Dao
public interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEpisode(Episode episode);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEpisodeList(List<Episode> episodeList);

    @Query(value = "SELECT * FROM podcast_episodes WHERE series_rss_url = :rssUrl")
    LiveData<List<Episode>> getEpisodesForSeries(String rssUrl);

    @Query(value = "SELECT * FROM podcast_episodes WHERE id = :episodeId")
    LiveData<Episode> getEpisode(int episodeId);

    @Update
    void updateEpisode(Episode episode);

    @Update
    void updateEpisodes(List<Episode> episodes);

    @Query(value =
            "SELECT id, title, summary, duration, pubDate, size, wasPlayed, episodeId " +
                    "FROM podcast_episodes " +
                    "LEFT JOIN playlist ON playlist.episodeId = podcast_episodes.id " +
                    "WHERE series_rss_url = :rssUrl")
    LiveData<List<EpisodesPlaylistJoin>> getEpisodesPlaylistJoinForSeries(String rssUrl);
}
