package de.rkirchner.podzeit.player;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.AudioAttributesCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private final String LOG_TAG = getClass().getSimpleName();
    private final int NOW_PLAYING_NOTIFICATION_ID = 5345345;

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private MediaSessionConnector mediaSessionConnector;
    private SimpleExoPlayer player;
    private AudioAttributesCompat audioAttributes;
    private MediaControllerCompat controller;
    private NotificationManagerCompat notificationManager;
    private NotificationBuilder notificationBuilder;

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
//        // (Optional) Control the level of access for the specified package name.
//        // You'll need to write your own logic to do this.
        if (allowBrowsing(clientPackageName, clientUid)) {
//            // Returns a root ID that clients can use with onLoadChildren() to retrieve
//            // the content hierarchy.
            return new BrowserRoot(MEDIA_ROOT_ID, null);
        } else {
//            // Clients can connect, but this BrowserRoot is an empty hierachy
//            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    private boolean allowBrowsing(String clientPackageName, int clientUid) {
        return true;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        //  Browsing not allowed
        if (TextUtils.equals(EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        obtainPlayerInstance();

        mediaSession = new MediaSessionCompat(this, LOG_TAG);

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

//        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
//        stateBuilder = new PlaybackStateCompat.Builder()
//                .setActions(
//                        PlaybackStateCompat.ACTION_PLAY |
//                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
//        mediaSession.setPlaybackState(stateBuilder.build());
//
//        // MySessionCallback() has methods that handle callbacks from a media controller
        notificationBuilder = new NotificationBuilder(this, mediaSession);
        notificationManager = NotificationManagerCompat.from(this);
//        MediaSessionCallbacks mediaSessionCallbacks = new MediaSessionCallbacks(this, notificationBuilder, mediaSession, player);
//        mediaSession.setCallback(mediaSessionCallbacks);
        controller = new MediaControllerCompat(this, mediaSession);
        controller.registerCallback(new MediaControllerCallback());

        //
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player, new PlaybackPreparerImpl());

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

        String userAgent = Util.getUserAgent(this, "zeit");
        DefaultHttpDataSourceFactory defaultHttpDataSourceFactory =
                new DefaultHttpDataSourceFactory(userAgent);
        Uri uri = Uri.parse("https://cdn.podigee.com/media/podcast_1652_was_jetzt_episode_236_einwanderung_in_spanien_noch_kein_thema.mp3?v=1534734347&source=feed");
        MediaSource mediaSource = new ExtractorMediaSource.Factory(
                defaultHttpDataSourceFactory)
                .createMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);

            if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                startForeground(NOW_PLAYING_NOTIFICATION_ID, notificationBuilder.build());
            } else {
                stopForeground(false);
            }

        }
    }
}

