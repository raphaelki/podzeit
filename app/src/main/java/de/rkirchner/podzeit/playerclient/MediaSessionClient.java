package de.rkirchner.podzeit.playerclient;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.rkirchner.podzeit.playerservice.MediaPlaybackService;
import timber.log.Timber;

@Singleton
public class MediaSessionClient {

    private MediaBrowserCompat mediaBrowser;
    private Context context;
    private MediaBrowserConnectionCallbacks connectionCallbacks;
    private MutableLiveData<Boolean> isServiceConnected = new MutableLiveData<>();
    private MediaControllerCompat mediaController;
    private MediaControllerCompat.Callback mediaControllerCallback;
    private MutableLiveData<PlaybackStateCompat> playbackState = new MutableLiveData<>();
    private PlaybackStateCompat.Builder playbackStateBuilder = new PlaybackStateCompat.Builder();
    private MediaControllerCompat.TransportControls transportControls;
    private MutableLiveData<List<MediaSessionCompat.QueueItem>> queueItems = new MutableLiveData<>();
    private MutableLiveData<MediaMetadataCompat> mediaMetadata = new MutableLiveData<>();

    @Inject
    public MediaSessionClient(Context context) {
        this.context = context;
        isServiceConnected.postValue(false);
        playbackState.postValue(buildPlaybackStateCompat(PlaybackStateCompat.STATE_NONE));
        createMediaBrowser();
        mediaBrowser.connect();
    }

    private PlaybackStateCompat buildPlaybackStateCompat(int playbackState) {
        return playbackStateBuilder.setState(playbackState, 0, 0).build();
    }

    private void createMediaBrowser() {
        connectionCallbacks = new MediaBrowserConnectionCallbacks();
        mediaControllerCallback = new MediaControllerCallback();
        ComponentName componentName = new ComponentName(context, MediaPlaybackService.class);
        mediaBrowser = new MediaBrowserCompat(context, componentName, connectionCallbacks, null);
    }

    public MutableLiveData<Boolean> getIsServiceConnected() {
        return isServiceConnected;
    }

    public MutableLiveData<PlaybackStateCompat> getPlaybackState() {
        return playbackState;
    }

    public MediaControllerCompat.TransportControls getTransportControls() {
        return transportControls;
    }

    public MediaControllerCompat getMediaController() {
        return mediaController;
    }

    public LiveData<List<MediaSessionCompat.QueueItem>> getQueueItems() {
        return queueItems;
    }

    public LiveData<MediaMetadataCompat> getMediaMetadata() {
        return mediaMetadata;
    }

    private class MediaBrowserConnectionCallbacks extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            try {
                mediaController = new MediaControllerCompat(context, mediaBrowser.getSessionToken());
                mediaController.registerCallback(mediaControllerCallback);
                transportControls = mediaController.getTransportControls();
            } catch (RemoteException e) {
                Timber.e("Could not construct MediaControllerCompat: %s", e.getMessage());
            }

            isServiceConnected.postValue(true);
        }

        @Override
        public void onConnectionSuspended() {
            isServiceConnected.postValue(false);
        }

        @Override
        public void onConnectionFailed() {
            isServiceConnected.postValue(false);
        }
    }

    private class MediaControllerCallback extends MediaControllerCompat.Callback {

        @Override
        public void onSessionReady() {
            super.onSessionReady();
        }

        @Override
        public void onSessionEvent(String event, Bundle extras) {
            super.onSessionEvent(event, extras);
        }

        @Override
        public void onQueueTitleChanged(CharSequence title) {
            super.onQueueTitleChanged(title);
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
        }

        @Override
        public void onAudioInfoChanged(MediaControllerCompat.PlaybackInfo info) {
            super.onAudioInfoChanged(info);
        }

        @Override
        public void onCaptioningEnabledChanged(boolean enabled) {
            super.onCaptioningEnabledChanged(enabled);
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }

        @Override
        public void onShuffleModeChanged(int shuffleMode) {
            super.onShuffleModeChanged(shuffleMode);
        }

        @Override
        public void binderDied() {
            super.binderDied();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.postValue(state);
            Timber.d("Playback state changed: %s", state.getState());
        }

        @Override
        public void onSessionDestroyed() {
            connectionCallbacks.onConnectionSuspended();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            mediaMetadata.postValue(metadata);
        }

        @Override
        public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
            queueItems.postValue(queue);
        }
    }
}
