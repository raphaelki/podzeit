package de.rkirchner.podzeit.data.local;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.rkirchner.podzeit.data.models.Series;

@Dao
public interface SeriesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSeriesList(List<Series> seriesList);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertSeries(Series series);

    @Query("SELECT * FROM podcast_series")
    LiveData<List<Series>> getAllSeries();

    @Query("SELECT * FROM podcast_series")
    List<Series> getAllSeriesSync();

    @Query("SELECT * FROM podcast_series WHERE rss_url = :rssUrl")
    LiveData<Series> getSeries(String rssUrl);

    @Query("SELECT * FROM podcast_series WHERE rss_url = :rssUrls")
    LiveData<List<Series>> getSeriesList(String[] rssUrls);

    @Query("SELECT * FROM podcast_series WHERE rss_url = :rssUrl")
    Series getSeriesSync(String rssUrl);

    @Update
    void updateSeries(Series series);

    @Query("SELECT * FROM podcast_series WHERE rss_url LIKE :uriAuthority")
    LiveData<Series> getSeriesMatchingUriAuthority(String uriAuthority);

}
