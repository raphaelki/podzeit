package de.rkirchner.podzeit.ui.player;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import javax.inject.Inject;

import de.rkirchner.podzeit.playerclient.MediaSessionClient;

public class PlayerViewModel extends ViewModel {

    private MediaSessionClient mediaSessionClient;
    private MutableLiveData<Boolean> playbackState = new MutableLiveData<>();

    @Inject
    public PlayerViewModel(MediaSessionClient mediaSessionClient) {
        this.mediaSessionClient = mediaSessionClient;
        playbackState.postValue(false);
    }

    public LiveData<String> getCurrentMediaId() {
        return Transformations.map(getMetadata(),
                metadata -> metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    }

    public LiveData<Boolean> isPlaying() {
        return Transformations.map(getPlaybackState(),
                state -> state.getState() == PlaybackStateCompat.STATE_PLAYING);
    }

    public LiveData<Boolean> isPaused() {
        return Transformations.map(getPlaybackState(),
                state -> state.getState() == PlaybackStateCompat.STATE_PAUSED);
    }

    public LiveData<Long> getEpisodeDuration() {
        return Transformations.map(getMetadata(),
                metadata -> metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
    }

    public LiveData<String> getTitle() {
        return Transformations.map(getMetadata(),
                metadata -> metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE));
    }

    public LiveData<String> getSummary() {
        return Transformations.map(getMetadata(),
                metadata -> metadata.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION));
    }

    public LiveData<Long> getPlayPosition() {
        return Transformations.map(getPlaybackState(), PlaybackStateCompat::getPosition);
    }

    public LiveData<Float> getPlaybackSpeed() {
        return Transformations.map(getPlaybackState(), PlaybackStateCompat::getPlaybackSpeed);
    }

    private LiveData<MediaMetadataCompat> getMetadata() {
        return mediaSessionClient.getMediaMetadata();
    }

    private LiveData<PlaybackStateCompat> getPlaybackState() {
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

    public void startPlayback() {
        mediaSessionClient.getTransportControls().play();
    }

    public void pausePlayback() {
        mediaSessionClient.getTransportControls().pause();
    }
}
