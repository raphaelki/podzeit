package de.rkirchner.podzeit.playerservice;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.AudioAttributesCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private final String LOG_TAG = getClass().getSimpleName();

    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private SimpleExoPlayer player;
    private AudioAttributesCompat audioAttributes;
    private MediaControllerCompat controller;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot(EMPTY_MEDIA_ROOT_ID, null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (TextUtils.equals(EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        obtainPlayerInstance();

        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setSessionActivity(sessionActivityPendingIntent);
        mediaSession.setActive(true);

        controller = new MediaControllerCompat(this, mediaSession);
        controller.registerCallback(new MediaControllerCallback(this, mediaSession.getSessionToken()));

        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player, null);

        List<MediaDescriptionCompat> queue = new ArrayList<>();
        TimelineQueueNavigatorImpl timelineQueueNavigator = new TimelineQueueNavigatorImpl(mediaSession, 10000, queue);
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        mediaSessionConnector.setQueueEditor(new TimelineQueueEditor(controller,
                concatenatingMediaSource,
                new QueueDataAdapterImpl(concatenatingMediaSource, queue, controller),
                new MediaSourceFactoryImpl(this)));
        player.prepare(concatenatingMediaSource);

        audioAttributes = new AudioAttributesCompat.Builder()
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                .build();

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
    }

    private void obtainPlayerInstance() {

        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                new DefaultLoadControl());
    }

    @Override
    public void onDestroy() {
        mediaSession.setActive(false);
        mediaSession.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}

