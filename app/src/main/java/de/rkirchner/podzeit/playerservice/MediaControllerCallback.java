package de.rkirchner.podzeit.playerservice;

import android.app.Service;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private final int NOW_PLAYING_NOTIFICATION_ID = 5345345;

    private Service mediaBrowserService;
    private NotificationManagerCompat notificationManager;
    private NotificationBuilder notificationBuilder;

    public MediaControllerCallback(Service mediaBrowserService, Token mediaSessionToken) {
        this.mediaBrowserService = mediaBrowserService;
        notificationBuilder = new NotificationBuilder(mediaBrowserService, mediaSessionToken);
        notificationManager = NotificationManagerCompat.from(mediaBrowserService);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            mediaBrowserService.startForeground(NOW_PLAYING_NOTIFICATION_ID, notificationBuilder.build());
        } else if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
            mediaBrowserService.stopForeground(false);
            notificationManager.notify(NOW_PLAYING_NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
