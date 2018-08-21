package de.rkirchner.podzeit.playerservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import de.rkirchner.podzeit.R;

public class NotificationBuilder {

    private final String NOW_PLAYING_CHANNEL = "de.kirchner.podzeit.NOW_PLAYING";
    private final String LOG_TAG = getClass().getSimpleName();
    private Context context;
    private MediaSessionCompat mediaSession;
    private NotificationManager notificationManager;

    public NotificationBuilder(Context context, MediaSessionCompat mediaSession) {
        this.context = context;
        this.mediaSession = mediaSession;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public Notification build() {
        createNotificationChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL);

        MediaControllerCompat mediaController = null;
        try {
            mediaController = new MediaControllerCompat(context, mediaSession.getSessionToken());
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Could not create media controller, thus can not create notification: " + e.getMessage());
            return null;
        }

        NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.exo_controls_play,
                "Play", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY));

        NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.exo_controls_pause,
                "Pause", MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE));

        MediaMetadataCompat mediaMetadata = mediaController.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();
        PlaybackStateCompat playbackState = mediaController.getPlaybackState();

        int playbackStateActions = playbackState.getState();
        if (playbackStateActions == PlaybackStateCompat.STATE_PLAYING) {
            builder.addAction(pauseAction);
        } else if (playbackStateActions == PlaybackStateCompat.STATE_PAUSED) {
            builder.addAction(playAction);
        }

        PendingIntent stopIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                PlaybackStateCompat.ACTION_STOP);

        MediaStyle mediaStyle = new MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(stopIntent);

        builder.setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(mediaController.getSessionActivity())
                .setSmallIcon(R.drawable.ic_play_circle_outline)
                .setDeleteIntent(stopIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setStyle(mediaStyle);

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()) {
            CharSequence name = context.getString(R.string.now_playing_channel_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOW_PLAYING_CHANNEL, name, importance);
            channel.setDescription(context.getString(R.string.now_playing_channel_description));
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean nowPlayingChannelExists() {
        return notificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null;
    }
}
