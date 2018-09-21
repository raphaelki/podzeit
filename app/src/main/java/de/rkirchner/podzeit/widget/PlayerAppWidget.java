package de.rkirchner.podzeit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasBroadcastReceiverInjector;
import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.MetadataJoin;
import de.rkirchner.podzeit.playerclient.MediaSessionClient;
import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class PlayerAppWidget extends AppWidgetProvider implements HasBroadcastReceiverInjector {

    @Inject
    PodcastRepository repository;
    @Inject
    MediaSessionClient mediaSessionClient;
    @Inject
    AppExecutors appExecutors;
    @Inject
    DispatchingAndroidInjector<BroadcastReceiver> injector;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {
        appExecutors.diskIO().execute(() -> {
            Intent intent = new Intent(context, MainActivity.class);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.player_app_widget);
            MetadataJoin currentEpisode = repository.getCurrentlySelectedEpisodeSync();
            if (currentEpisode != null) {
                views.setTextViewText(R.id.widget_title, currentEpisode.getEpisodeTitle());
                AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, 144, 144, R.id.widget_thumbnail, views, appWidgetId);
                appExecutors.mainThread().execute(() -> Glide.with(context.getApplicationContext())
                        .asBitmap()
                        .load(currentEpisode.getThumbnailUrl())
                        .into(appWidgetTarget));
                // setup intent to show episode details
                intent.putExtra(Constants.EPISODE_ID_KEY, currentEpisode.getId());
            }
            boolean isPlaying = false;
            try {
                isPlaying = mediaSessionClient.getMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
            } catch (NullPointerException e) {
                Timber.w("Player state not available");
            }
            views.setImageViewResource(R.id.widget_play, isPlaying ? R.drawable.exo_controls_pause : R.drawable.exo_controls_play);

            PendingIntent showEpisodeDetailsPendingIntent = PendingIntent.getActivity(context, 345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_thumbnail, showEpisodeDetailsPendingIntent);
            PendingIntent playIntent =
                    MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                            isPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY);
            views.setOnClickPendingIntent(R.id.widget_play, playIntent);
            views.setOnClickPendingIntent(R.id.widget_previous, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
            views.setOnClickPendingIntent(R.id.widget_next, MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        AndroidInjection.inject(this, context);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public AndroidInjector<BroadcastReceiver> broadcastReceiverInjector() {
        return injector;
    }
}

