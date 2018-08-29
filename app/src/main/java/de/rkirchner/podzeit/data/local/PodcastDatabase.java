package de.rkirchner.podzeit.data.local;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import de.rkirchner.podzeit.data.models.Series;

@Database(entities = {
        Series.class,
        Episode.class,
        PlaylistEntry.class
}, version = 7)
public abstract class PodcastDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "podzeit.db";

    public abstract EpisodeDao episodeDao();

    public abstract SeriesDao seriesDao();

    public abstract PlaylistDao playlistDao();
}
