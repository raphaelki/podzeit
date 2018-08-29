package de.rkirchner.podzeit.ui.player;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import de.rkirchner.podzeit.playerclient.MediaSessionClient;

public class PlayerViewModel extends ViewModel {

    private MediaSessionClient mediaSessionClient;
    private MutableLiveData<Boolean> playbackState = new MutableLiveData<>();
    private Observer<PlaybackStateCompat> playbackStateObserver = playbackStateCompat -> {
        if (playbackStateCompat.getState() == PlaybackStateCompat.STATE_PLAYING) {
            playbackState.postValue(true);
        } else playbackState.postValue(false);
    };

    public LiveData<MediaMetadataCompat> getMetadata() {
        return mediaSessionClient.getMediaMetadata();
    }

    public LiveData<PlaybackStateCompat> getPlaybackState() {
        return mediaSessionClient.getPlaybackState();
    }

    public void setPlayerToPosition(long position) {
        mediaSessionClient.getMediaController().getTransportControls().seekTo(position);
    }

    public void fastForward() {
        mediaSessionClient.getTransportControls().fastForward();
    }

    public void rewind() {
        mediaSessionClient.getTransportControls().rewind();
    }

    public void skipToNext() {
        mediaSessionClient.getTransportControls().skipToNext();
    }

    public void skipToPrevious() {
        mediaSessionClient.getTransportControls().skipToPrevious();
    }

    @Inject
    public PlayerViewModel(MediaSessionClient mediaSessionClient) {
        this.mediaSessionClient = mediaSessionClient;
        playbackState.postValue(false);
        mediaSessionClient.getPlaybackState().observeForever(playbackStateObserver);
    }

    public LiveData<Boolean> isPlaying() {
        return playbackState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mediaSessionClient.getPlaybackState().removeObserver(playbackStateObserver);
    }

    public void startPlayback() {
        mediaSessionClient.getTransportControls().play();
    }

    public void pausePlayback() {
        mediaSessionClient.getTransportControls().pause();
    }
}
