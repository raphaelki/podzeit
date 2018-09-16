package de.rkirchner.podzeit.playerservice;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource.InvalidResponseCodeException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";
    private final String LOG_TAG = getClass().getSimpleName();
    @Inject
    EpisodeDao episodeDao;
    @Inject
    AppExecutors appExecutors;
    @Inject
    PlaylistDao playlistDao;
    private MediaSessionCompat mediaSession;
    private MediaSessionConnector mediaSessionConnector;
    private MediaControllerCallback controllerCallback;
    private SimpleExoPlayer player;
    private MediaControllerCompat controller;
    private QueueDataAdapterImpl queueDataAdapter;
    private MediaDescriptionCompat previousTrack;
    private MediaDescriptionCompat currentTrack;
    private Handler handler = new Handler();
    private PlaybackStateCompat lastPlaybackState;
    private long currentPlaybackPosition;
    private long currentTrackDuration = -1;
    private Runnable positionTracker = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                currentPlaybackPosition = player.getCurrentPosition();
                Timber.d("Buffered: %s %s", player.getBufferedPercentage(), player.getBufferedPosition());
            }
            handler.postDelayed(this, 1000);
        }
    };
    private long previousPlaybackPosition;

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
        AndroidInjection.inject(this);
        super.onCreate();
        obtainPlayerInstance();

        Intent sessionIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        PendingIntent sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0);
        mediaSession = new MediaSessionCompat(this, LOG_TAG);
        mediaSession.setSessionActivity(sessionActivityPendingIntent);
        mediaSession.setActive(true);

        controller = new MediaControllerCompat(this, mediaSession);
        controllerCallback = new MediaControllerCallback(this,
                mediaSession.getSessionToken());
        controller.registerCallback(controllerCallback);
        controller.registerCallback(new MediaControllerCallback(this, mediaSession.getSessionToken()) {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                if (state.getState() == PlaybackStateCompat.STATE_PAUSED ||
                        state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                    if (currentTrackDuration != -1 &&
                            state.getActiveQueueItemId() != -1 &&
                            state.getPosition() >= currentTrackDuration
                            ) {
                        onEpisodeCompleted(getActiveQueueItemDescription());
                    } else saveCurrentPlaybackPosition();
                }
            }

            @Override
            public void onMetadataChanged(MediaMetadataCompat metadata) {
                currentTrackDuration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
            }
        });

        mediaSessionConnector = new MediaSessionConnector(mediaSession);
        mediaSessionConnector.setPlayer(player, null);

        List<MediaDescriptionCompat> queue = new ArrayList<>();
        TimelineQueueNavigatorImpl timelineQueueNavigator =
                new TimelineQueueNavigatorImpl(mediaSession,
                        10000,
                        queue,
                        appExecutors,
                        playlistDao);
        mediaSessionConnector.setQueueNavigator(timelineQueueNavigator);
        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        queueDataAdapter = new QueueDataAdapterImpl(concatenatingMediaSource, queue, controller);
        mediaSessionConnector.setQueueEditor(new TimelineQueueEditor(controller,
                concatenatingMediaSource,
                queueDataAdapter,
                new MediaSourceFactoryImpl(this)));
        player.prepare(concatenatingMediaSource, false, false);
        mediaSessionConnector.setErrorMessageProvider(throwable -> {
            int errorCode = 0;
            String message = throwable.getSourceException().getMessage();
            if (throwable.getSourceException() instanceof InvalidResponseCodeException) {
                InvalidResponseCodeException exception = ((InvalidResponseCodeException) throwable.getSourceException());
                errorCode = exception.responseCode;
                message = exception.dataSpec.uri.getAuthority();
            }
            return new Pair<>(errorCode, message);
        });
        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
        handler.post(positionTracker);
    }

    private void obtainPlayerInstance() {
        DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();
        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                new DefaultLoadControl());
        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPositionDiscontinuity(int reason) {
                MediaDescriptionCompat queuedTrack = getActiveQueueItemDescription();
                if (currentTrack == null || queuedTrack != currentTrack) {
                    previousPlaybackPosition = currentPlaybackPosition;
                    previousTrack = currentTrack;
                    currentTrack = queuedTrack;
                    if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT ||
                            reason == Player.DISCONTINUITY_REASON_SEEK) {
                        Timber.d("Manual skip");
                        savePlaybackPositionAndSelectEpisode();
                    }
                }
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    Timber.d("Current track ended");
                    onEpisodeCompleted(getActiveQueueItemDescription());
                    savePlaybackPositionAndSelectEpisode();
                }
                Timber.d("onPositionDiscontinuity reason: %s, activeQueueItem: %s", reason, controller.getPlaybackState().getActiveQueueItemId());
            }
        });
    }

    private MediaDescriptionCompat getActiveQueueItemDescription() {
        return queueDataAdapter.getMediaDescription((int) controller.getPlaybackState().getActiveQueueItemId());
    }

    private void savePlaybackPositionAndSelectEpisode() {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry previousSelection = playlistDao.getSelectedPlaylistEntry();
            if (previousSelection != null) {
                if (previousPlaybackPosition != 0) {
                    Timber.d("Saving playback position of %s: %s", previousSelection.getEpisodeId(), previousPlaybackPosition);
                    previousSelection.setPlaybackPosition(previousPlaybackPosition);
                }
                previousSelection.setSelected(false);
                playlistDao.updateEntry(previousSelection);
            }
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntryAtPosition((int) controller.getPlaybackState().getActiveQueueItemId());
            if (playlistEntry != null) {
                playlistEntry.setSelected(true);
                playlistDao.updateEntry(playlistEntry);
                Timber.d("New selected item: %s", playlistEntry.getPlaylistPosition());
            }
        });
    }

    private void saveCurrentPlaybackPosition() {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry playlistEntry = playlistDao.getSelectedPlaylistEntry();
            if (playlistEntry != null && currentPlaybackPosition != 0) {
                Timber.d("Saving current playback position of %s: %s", playlistEntry.getEpisodeId(), player.getCurrentPosition());
                playlistEntry.setPlaybackPosition(currentPlaybackPosition);
                playlistDao.updateEntry(playlistEntry);
            }
        });

    }

    public void onEpisodeCompleted(MediaDescriptionCompat completedEpisode) {
        appExecutors.diskIO().execute(() -> {
            Episode previousEpisode = episodeDao.getEpisodeForUrl(completedEpisode.getMediaId());
            if (previousEpisode != null) {
                PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(previousEpisode.getId());
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                if (sharedPreferences != null) {
                    boolean removeEpisode = sharedPreferences.getBoolean(getString(R.string.shared_pref_remove_after_playback_key), false);
                    if (removeEpisode) playlistDao.removeEpisodeFromPlaylist(playlistEntry);
                    else {
                        playlistEntry.setPlaybackPosition(0);
                        playlistDao.updateEntry(playlistEntry);
                    }
                }
                previousEpisode.setWasPlayed(true);
                episodeDao.updateEpisode(previousEpisode);
                Timber.d("Episode set to was played %s", completedEpisode.getTitle());
            }
        });
    }

    @Override
    public void onDestroy() {
        mediaSession.setActive(false);
        mediaSession.release();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}

