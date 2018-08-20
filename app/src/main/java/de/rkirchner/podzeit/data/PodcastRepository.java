package de.rkirchner.podzeit.data;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.SeriesDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.data.remote.FetchService;
import timber.log.Timber;

public class PodcastRepository {

    private final String FIREBASE_URL_KEY = "url";

    private DatabaseReference firebaseReference;
    private SeriesDao seriesDao;
    private EpisodeDao episodeDao;
    private AppExecutors appExecutors;
    private Context context;

    @Inject
    public PodcastRepository(DatabaseReference firebaseReference, SeriesDao seriesDao, EpisodeDao episodeDao, AppExecutors appExecutors, Context context) {
        this.firebaseReference = firebaseReference;
        this.seriesDao = seriesDao;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.context = context;
    }

    public void updateEpisodePlaylistState(Episode episode) {
        appExecutors.diskIO().execute(() -> episodeDao.updateEpisode(episode));
    }

    public void startFetch() {
        subscribeToFirebase();
    }

    public LiveData<List<Series>> getSeriesList() {
        return seriesDao.getAllSeries();
    }

    public LiveData<Series> getSeries(String rssUrl) {
        return seriesDao.getSeries(rssUrl);
    }

    public LiveData<List<Episode>> getEpisodesForSeries(String rssUrl) {
        return episodeDao.getEpisodesForSeries(rssUrl);
    }

    public LiveData<Episode> getEpisode(int episodeId) {
        return episodeDao.getEpisode(episodeId);
    }

    private void subscribeToFirebase() {
        firebaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String url = getUrlFromSnapshot(dataSnapshot);
                compareToRoom(url);
                Timber.d("Firebase child added: %s", url);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String url = getUrlFromSnapshot(dataSnapshot);
                Timber.d("Firebase child changed: %s", url);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String url = getUrlFromSnapshot(dataSnapshot);
                Timber.d("Firebase child removed: %s", url);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private String getUrlFromSnapshot(DataSnapshot dataSnapshot) {
        return dataSnapshot.child(FIREBASE_URL_KEY).getValue(String.class);
    }

    private void compareToRoom(String url) {
        appExecutors.diskIO().execute(() -> {
            Series series = seriesDao.getSeriesSync(url);
            if (series == null) {
                Timber.d("Series with URL %s does not exist in local database. Starting fetch.", url);
                Intent intent = new Intent();
                intent.putExtra(Constants.RSS_URL_KEY, url);
                FetchService.enqueueWork(context, intent);
            } else {
                Timber.d("Series with URL %s already exists in local database", url);
            }
        });
    }
}
