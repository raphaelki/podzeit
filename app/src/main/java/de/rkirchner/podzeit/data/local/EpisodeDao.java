package de.rkirchner.podzeit.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
import de.rkirchner.podzeit.data.models.MetadataJoin;

@Dao
public interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEpisode(Episode episode);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEpisodeList(List<Episode> episodeList);

    @Query(value = "SELECT * FROM episodes WHERE series_rss_url = :rssUrl")
    LiveData<List<Episode>> getEpisodesForSeries(String rssUrl);

    @Query(value = "SELECT * FROM episodes WHERE id = :episodeId")
    LiveData<Episode> getEpisode(int episodeId);

    @Query(value = "SELECT id, episodes.title AS episodeTitle, episodes.summary," +
            " thumbnailUrl, url, series.title AS seriesTitle, credentials " +
            "FROM episodes " +
            "LEFT JOIN series ON series.rss_url = series_rss_url " +
            "WHERE id= :episodeId")
    MetadataJoin getEpisodeSync(int episodeId);

    @Query(value = "SELECT id, episodes.title AS episodeTitle, episodes.summary," +
            " thumbnailUrl, url, series.title AS seriesTitle, credentials " +
            "FROM episodes " +
            "LEFT JOIN series ON series.rss_url = series_rss_url " +
            "WHERE url= :url")
    MetadataJoin getEpisodeSync(String url);

    @Query(value = "SELECT id, episodes.title AS episodeTitle, episodes.summary, thumbnailUrl, url, series.title AS seriesTitle, credentials " +
            "FROM episodes " +
            "LEFT JOIN series ON series.rss_url = series_rss_url " +
            "LEFT JOIN playlist ON playlist.episodeId = episodes.id " +
            "WHERE playlist.isSelected = 1")
    MetadataJoin getCurrentlySelectedEpisodeSync();

    @Query(value = "SELECT * FROM episodes WHERE url = :url")
    Episode getEpisodeForUrl(String url);

    @Query(value = "SELECT * FROM episodes WHERE id = :episodeId")
    Episode getEpisodeForId(int episodeId);

    @Update
    void updateEpisode(Episode episode);

    @Update
    void updateEpisodes(List<Episode> episodes);

    @Query(value =
            "SELECT id, title, summary, duration, pubDate, size, wasPlayed, episodeId " +
                    "FROM episodes " +
                    "LEFT JOIN playlist ON playlist.episodeId = episodes.id " +
                    "WHERE series_rss_url = :rssUrl")
    LiveData<List<EpisodesPlaylistJoin>> getEpisodesPlaylistJoinForSeries(String rssUrl);

    @Query(value =
            "SELECT id, title, summary, duration, pubDate, size, wasPlayed, episodeId " +
                    "FROM episodes " +
                    "LEFT JOIN playlist ON playlist.episodeId = episodes.id " +
                    "WHERE series_rss_url = :rssUrl AND wasPlayed = 0")
    LiveData<List<EpisodesPlaylistJoin>> getEpisodesPlaylistJoinForSeriesWithoutPlayed(String rssUrl);

    @Query(value =
            "SELECT id, title, summary, duration, pubDate, size, wasPlayed, episodeId " +
                    "FROM episodes " +
                    "LEFT JOIN playlist ON playlist.episodeId = episodes.id " +
                    "WHERE id = :episodeId")
    LiveData<EpisodesPlaylistJoin> getEpisodesPlaylistJoinForEpisode(int episodeId);

    @Query(value = "SELECT COUNT(*) FROM episodes WHERE series_rss_url = :rssSeriesUrl")
    int getEpisodeCount(String rssSeriesUrl);
}
