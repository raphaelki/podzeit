package de.rkirchner.podzeit.playerservice;

import android.app.Notification;
import android.app.Service;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final int NOW_PLAYING_NOTIFICATION_ID = 5345345;

    private Service mediaBrowserService;
    private NotificationManagerCompat notificationManager;
    private NotificationBuilder notificationBuilder;
    private boolean isForeground = false;

    public MediaControllerCallback(Service mediaBrowserService, Token mediaSessionToken) {
        this.mediaBrowserService = mediaBrowserService;
        notificationBuilder = new NotificationBuilder(mediaBrowserService, mediaSessionToken);
        notificationManager = NotificationManagerCompat.from(mediaBrowserService);
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        Notification notification = null;
        int newState = state.getState();
        if (newState != PlaybackStateCompat.STATE_NONE) {
            notification = notificationBuilder.build();
        }
        if (newState == PlaybackStateCompat.STATE_PLAYING) {
            mediaBrowserService.startForeground(NOW_PLAYING_NOTIFICATION_ID, notification);
            isForeground = true;
        } else {
            if (isForeground) {
                mediaBrowserService.stopForeground(false);
                if (notification != null) {
                    notificationManager.notify(NOW_PLAYING_NOTIFICATION_ID, notification);
                } else {
                    mediaBrowserService.stopForeground(true);
                }
                isForeground = false;
            }
        }
    }
}
