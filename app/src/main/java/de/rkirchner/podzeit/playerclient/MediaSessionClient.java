package de.rkirchner.podzeit.playerclient;

import android.arch.lifecycle.MutableLiveData;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;

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
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            playbackState.postValue(state);
        }

        @Override
        public void onSessionDestroyed() {
            connectionCallbacks.onConnectionSuspended();
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {

        }
    }
}
