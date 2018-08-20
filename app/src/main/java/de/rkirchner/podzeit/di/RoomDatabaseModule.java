package de.rkirchner.podzeit.di;

import android.arch.persistence.room.Room;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.local.PodcastDatabase;
import de.rkirchner.podzeit.data.local.SeriesDao;

@Module
public class RoomDatabaseModule {

    @Singleton
    @Provides
    static PodcastDatabase providePodcastDatabase(Context context) {
        return Room.databaseBuilder(context, PodcastDatabase.class, PodcastDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton
    @Provides
    static SeriesDao provideSeriesDao(PodcastDatabase podcastDatabase) {
        return podcastDatabase.seriesDao();
    }

    @Singleton
    @Provides
    static EpisodeDao provideEpisodeDao(PodcastDatabase podcastDatabase) {
        return podcastDatabase.episodeDao();
    }

    @Singleton
    @Provides
    static PlaylistDao providePlaylistDao(PodcastDatabase podcastDatabase) {
        return podcastDatabase.playlistDao();
    }
}
