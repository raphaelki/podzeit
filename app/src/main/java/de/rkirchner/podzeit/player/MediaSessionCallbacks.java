package de.rkirchner.podzeit.player;

import android.content.Intent;
import android.support.v4.media.session.MediaSessionCompat;

public class MediaSessionCallbacks extends MediaSessionCompat.Callback {

    private MediaPlaybackService mediaPlaybackService;

    public MediaSessionCallbacks(MediaPlaybackService mediaPlaybackService) {
        this.mediaPlaybackService = mediaPlaybackService;
    }

    @Override
    public void onPlay() {
        super.onPlay();
        mediaPlaybackService.startService(new Intent(mediaPlaybackService, MediaPlaybackService.class));
    }

    @Override
    public void onStop() {
        super.onStop();
        mediaPlaybackService.stopSelf();
    }
}
