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

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

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
    private TimelineQueueNavigator timelineQueueNavigator;
    @Inject
    AppExecutors appExecutors;
    @Inject
    PlaylistDao playlistDao;

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
        mediaSessionConnector = new MediaSessionConnector(mediaSession, new PlaybackControllerImpl(this));
        playbackPreparer = new PlaybackPreparerImpl(player, playlistDao, episodeDao, appExecutors, this, concatenatingMediaSource);
        mediaSessionConnector.setPlayer(player, playbackPreparer);
        timelineQueueNavigator = new TimelineQueueNavigator(mediaSession) {
            private Timeline.Window window = new Timeline.Window();

            @Override
            public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
                return (MediaDescriptionCompat) player.getCurrentTimeline().getWindow(windowIndex, window, true).tag;
            }
        };
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator);
        mediaSessionConnector.setErrorMessageProvider(new ErrorMessageProviderImpl());
        playbackPreparer.prepareMediaSourceFromDbPlaylist();
    }

    private void createPlayerInstance() {
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                new DefaultLoadControl());
        player.addListener(new Player.DefaultEventListener() {

            @Override
            public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
                Timber.d("Timeline changed: %s", reason);
            }

            @Override
            public void onPositionDiscontinuity(int reason) {
                switch (reason) {
                    case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:
                        onEpisodeComplete();
                        break;
                }
                Timber.d("On player discontinuity: %s %s", reason, player.getCurrentWindowIndex());
            }
        });
    }

    private void onEpisodeComplete() {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntryAtPosition(player.getCurrentWindowIndex() - 1);
            if (playlistEntry != null) {
                Episode episode = episodeDao.getEpisodeForId(playlistEntry.getEpisodeId());
                episode.setWasPlayed(true);
                episodeDao.updateEpisode(episode);
                Timber.d("Mark episode %s as played", episode.getTitle());
            }
        });
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

