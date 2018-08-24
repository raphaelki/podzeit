package de.rkirchner.podzeit.playerservice;

import android.app.Notification;
import android.app.Service;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.List;

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

    private boolean isForeground = false;

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

    @Override
    public void onSessionReady() {
        super.onSessionReady();
    }

    @Override
    public void onSessionDestroyed() {
        super.onSessionDestroyed();
    }

    @Override
    public void onSessionEvent(String event, Bundle extras) {
        super.onSessionEvent(event, extras);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        super.onMetadataChanged(metadata);
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
        super.onQueueChanged(queue);
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
}
