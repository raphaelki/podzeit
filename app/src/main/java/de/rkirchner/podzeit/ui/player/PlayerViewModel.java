package de.rkirchner.podzeit.ui.player;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
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
