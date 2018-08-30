package de.rkirchner.podzeit.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.local.SeriesDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
import de.rkirchner.podzeit.data.models.MetadataJoin;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.data.remote.FetchService;
import timber.log.Timber;

public class PodcastRepository {

    private final String FIREBASE_URL_KEY = "url";
    private final String FIREBASE_NEEDS_CREDENTIALS_KEY = "needsCredentials";

    private DatabaseReference firebaseReference;
    private SeriesDao seriesDao;
    private EpisodeDao episodeDao;
    private PlaylistDao playlistDao;
    private AppExecutors appExecutors;
    private Context context;
    private MutableLiveData<DataState> refreshDataState = new MutableLiveData<>();
    private FetchingStateReceiver fetchingStateReceiver;

    @Inject
    public PodcastRepository(DatabaseReference firebaseReference, SeriesDao seriesDao, EpisodeDao episodeDao, AppExecutors appExecutors, Context context, PlaylistDao playlistDao) {
        this.firebaseReference = firebaseReference;
        this.seriesDao = seriesDao;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.playlistDao = playlistDao;
        this.context = context;
        fetchingStateReceiver = new FetchingStateReceiver();
        IntentFilter fetchServiceIntentFilter = new IntentFilter(Constants.FETCH_SERVICE_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(fetchingStateReceiver, fetchServiceIntentFilter);
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
                boolean needsCredentials = getNeedsCredentialsFromSnapshot(dataSnapshot);
                compareToRoom(url, needsCredentials);
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

    private boolean getNeedsCredentialsFromSnapshot(DataSnapshot dataSnapshot) {
        return dataSnapshot.child(FIREBASE_NEEDS_CREDENTIALS_KEY).getValue(Boolean.class);
    }

    private void compareToRoom(String url, boolean needsCredentials) {
        appExecutors.diskIO().execute(() -> {
            Series series = seriesDao.getSeriesSync(url);
            if (series == null) {
                Timber.d("Series with URL %s does not exist in local database. Starting fetch.", url);
                Intent intent = new Intent();
                intent.putExtra(Constants.RSS_URL_KEY, url);
                intent.putExtra(Constants.NEEDS_CREDENTIALS_KEY, needsCredentials);
                FetchService.enqueueWork(context, intent);
            } else {
                Timber.d("Series with URL %s already exists in local database", url);
            }
        });
    }

    public LiveData<List<EpisodesPlaylistJoin>> getEpisodesPlaylistJoinForSeries(String rssUrl) {
        return episodeDao.getEpisodesPlaylistJoinForSeries(rssUrl);
    }

    public LiveData<List<EpisodesPlaylistJoin>> getEpisodesPlaylistJoinForSeriesWithoutPlayed(String rssUrl) {
        return episodeDao.getEpisodesPlaylistJoinForSeriesWithoutPlayed(rssUrl);
    }

    public void triggerRefreshForRssUrl(String rssUrl) {
        Intent intent = new Intent();
        intent.putExtra(Constants.RSS_URL_KEY, rssUrl);
        FetchService.enqueueWork(context, intent);
    }

    public void triggerCompleteRefresh() {
        appExecutors.diskIO().execute(() -> {
            List<Series> allSeries = seriesDao.getAllSeriesSync();
            for (Series series : allSeries) {
                Intent intent = new Intent();
                intent.putExtra(Constants.RSS_URL_KEY, series.getRssUrl());
                FetchService.enqueueWork(context, intent);
            }
        });
    }

    public MetadataJoin getCurrentlySelectedEpisodeSync() {
        return episodeDao.getCurrentlySelectedEpisodeSync();
    }

    public LiveData<DataState> getRefreshDataState() {
        return refreshDataState;
    }

    private class FetchingStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.FETCH_SERVICE_BROADCAST_MESSAGE);
            switch (message) {
                case Constants.FETCH_SERVICE_EPISODES_STARTED:
                    refreshDataState.postValue(DataState.REFRESHING);
                    break;
                case Constants.FETCH_SERVICE_EPISODES_FINISHED:
                    refreshDataState.postValue(DataState.SUCCESS);
                    break;
            }
        }
    }

    public LiveData<Series> getSeriesMatchtingUriAuthority(String uriAuthority) {
        return seriesDao.getSeriesMatchingUriAuthority(addWildcards(uriAuthority));
    }

    private String addWildcards(String string) {
        return "%" + string + "%";
    }

    public void updateSeries(Series series) {
        appExecutors.diskIO().execute(() -> seriesDao.updateSeries(series));
    }
}
