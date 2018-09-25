package de.rkirchner.podzeit.playerservice;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private final String TAG = getClass().getSimpleName();
    @Inject
    EpisodeDao episodeDao;
    private SimpleExoPlayer player;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private PlaybackPreparerImpl playbackPreparer;
    private MediaControllerCompat controller;
    private ConcatenatingMediaSource concatenatingMediaSource;
    @Inject
    AppExecutors appExecutors;
    @Inject
    PlaylistDao playlistDao;
    private List<MediaDescriptionCompat> queue = new ArrayList<>();

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("empty_root_id", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if (TextUtils.equals("empty_root_id", parentId)) {
            result.sendResult(null);
        }
    }

    @Override
    public void onCreate() {
        AndroidInjection.inject(this);
        super.onCreate();
        createPlayerInstance();
        createMediaSession();
        createMediaSessionConnector();
        setSessionToken(mediaSession.getSessionToken());
    }

    private void createMediaSessionConnector() {
        concatenatingMediaSource = new ConcatenatingMediaSource();
        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        playbackPreparer = new PlaybackPreparerImpl(player, playlistDao, episodeDao, appExecutors, this, concatenatingMediaSource);
        mediaSessionConnector.setPlayer(player, playbackPreparer);
        mediaSessionConnector.setQueueNavigator(new TimelineQueueNavigator(mediaSession) {
            private Timeline.Window window = new Timeline.Window();

            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return (MediaDescriptionCompat) player.getCurrentTimeline().getWindow(windowIndex, window, true).tag;
            }
        });
        mediaSessionConnector.setErrorMessageProvider(new ErrorMessageProviderImpl());
        playbackPreparer.prepareMediaSourceFromPlaylist();
//        TimelineQueueEditor.QueueDataAdapter queueDataAdapter = new QueueDataAdapterImpl(appExecutors, playlistDao, episodeDao);
//        mediaSessionConnector.setQueueEditor(new TimelineQueueEditor(controller, concatenatingMediaSource, queueDataAdapter, new MediaSourceFactoryImpl(this)));
    }

    private void createPlayerInstance() {
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                new DefaultLoadControl());
    }

    private void createMediaSession() {
        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setSessionActivity(sessionActivityPendingIntent);
        mediaSession.setActive(true);
        controller = new MediaControllerCompat(this, mediaSession);
        MediaControllerCallback callback = new MediaControllerCallback(this, mediaSession.getSessionToken(), playlistDao, appExecutors, episodeDao);
        controller.registerCallback(callback);
    }
}

