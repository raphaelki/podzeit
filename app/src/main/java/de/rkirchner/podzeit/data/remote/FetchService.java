package de.rkirchner.podzeit.data.remote;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.FetchStatusBroadcaster;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.SeriesDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.Series;
import retrofit2.Retrofit;
import timber.log.Timber;

public class FetchService extends JobIntentService {

    static final int JOB_ID = 1000;
    private static int jobCount = 0;
    public final String CONTENT_UPDATE_CHANNEL_ID = "refresh_content";
    public final int UPDATE_NOTIFICATION_ID = 83458;
    @Inject
    FetchStatusBroadcaster fetchStatusBroadcaster;
    @Inject
    SeriesDao seriesDao;
    @Inject
    EpisodeDao episodeDao;
    @Inject
    Retrofit.Builder retrofitBuilder;
    private int totalNewEpisodeCount = 0;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private int jobDone = 0;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, FetchService.class, JOB_ID, work);
        jobCount++;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent.hasExtra(Constants.RSS_URL_KEY)) {
            String url = intent.getStringExtra(Constants.RSS_URL_KEY);
            boolean needsCredentials = intent.getBooleanExtra(Constants.NEEDS_CREDENTIALS_KEY, false);
            fetchStatusBroadcaster.fireBroadcast(Constants.FETCH_SERVICE_EPISODES_STARTED);
            startFetchForUrl(url, needsCredentials);
            fetchStatusBroadcaster.fireBroadcast(Constants.FETCH_SERVICE_EPISODES_FINISHED);
        }
    }

    @Override
    public void onCreate() {
        Timber.d("Creating FetchService");
        AndroidInjection.inject(this);
        super.onCreate();
        setupAndShowNotification();
    }

    @Override
    public void onDestroy() {
        Timber.d("All work done. Shutting down service.");
        if (totalNewEpisodeCount > 0) {
            Toast.makeText(this, "Fetched " + totalNewEpisodeCount + " new episodes", Toast.LENGTH_LONG).show();
        }
        notificationManager.cancel(UPDATE_NOTIFICATION_ID);
        super.onDestroy();
    }

    @WorkerThread
    private void startFetchForUrl(String url, boolean needsCredentials) {
        SeriesRSSFeedService rssFeedService = buildRSSFeedService(url);
        try {
            Series series = rssFeedService.getMP3Feed().execute().body();
            series.setRssUrl(url);
            series.setNeedsCredentials(needsCredentials);
            for (Episode episode : series.getEpisodes()) {
                episode.setSeriesRssUrl(url);
            }
            seriesDao.insertSeries(series);
            int episodesBeforeInsertion = episodeDao.getEpisodeCount(url);
            episodeDao.insertEpisodeList(series.getEpisodes());
            int episodesAfterInsertion = episodeDao.getEpisodeCount(url);
            int newEpisodeCount = episodesAfterInsertion - episodesBeforeInsertion;
            totalNewEpisodeCount += newEpisodeCount;
            Timber.d("Fetched series %s with %s episodes. Inserted %s new episodes into database.", series.getTitle(), series.getEpisodeCount(), newEpisodeCount);
            notificationBuilder.setProgress(jobCount, jobDone, false);
            notificationManager.notify(UPDATE_NOTIFICATION_ID, notificationBuilder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        jobDone++;
    }

    @WorkerThread
    private SeriesRSSFeedService buildRSSFeedService(String url) {
        return retrofitBuilder.baseUrl(url)
                .addConverterFactory(RSSConverterFactory.create())
                .build()
                .create(SeriesRSSFeedService.class);
    }

    private void setupAndShowNotification() {
        createNotificationChannel();
        notificationManager = NotificationManagerCompat.from(this);
        notificationBuilder =
                new NotificationCompat.Builder(this, CONTENT_UPDATE_CHANNEL_ID);
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle("Updating")
                .setSmallIcon(R.drawable.ic_autorenew)
                .setContentText("Fetching new series content")
                .setProgress(jobCount, jobDone, false);
        notificationManager.notify(UPDATE_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CONTENT_UPDATE_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
