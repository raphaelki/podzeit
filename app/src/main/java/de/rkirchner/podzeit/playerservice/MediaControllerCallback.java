package de.rkirchner.podzeit.playerservice;

import android.app.Notification;
import android.app.Service;
import android.net.Uri;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.Token;
import android.support.v4.media.session.PlaybackStateCompat;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import de.rkirchner.podzeit.widget.WidgetHelper;
import timber.log.Timber;

public class MediaControllerCallback extends MediaControllerCompat.Callback {

    private static final int NOW_PLAYING_NOTIFICATION_ID = 5345345;

    private Service mediaBrowserService;
    private NotificationManagerCompat notificationManager;
    private NotificationBuilder notificationBuilder;
    private boolean isForeground = false;
    private PlaylistDao playlistDao;
    private AppExecutors appExecutors;
    private EpisodeDao episodeDao;
    private Uri previousUri;

    public MediaControllerCallback(Service mediaBrowserService, Token mediaSessionToken, PlaylistDao playlistDao, AppExecutors appExecutors, EpisodeDao episodeDao) {
        this.mediaBrowserService = mediaBrowserService;
        notificationBuilder = new NotificationBuilder(mediaBrowserService, mediaSessionToken);
        notificationManager = NotificationManagerCompat.from(mediaBrowserService);
        this.appExecutors = appExecutors;
        this.playlistDao = playlistDao;
        this.episodeDao = episodeDao;
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
        WidgetHelper.triggerWidgetUpdate(mediaBrowserService);
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
        if (metadata != null && metadata.getDescription() != null && metadata.getDescription().getMediaUri() != null) {
            Uri uri = metadata.getDescription().getMediaUri();
            if (previousUri != uri) {
                appExecutors.diskIO().execute(() -> {
                    PlaylistEntry oldSelection = playlistDao.getSelectedPlaylistEntry();
                    if (oldSelection != null) {
                        oldSelection.setSelected(false);
                        playlistDao.updateEntry(oldSelection);
                    }
                    Episode episode = episodeDao.getEpisodeForUrl(uri.toString());
                    PlaylistEntry newSelection = playlistDao.getPlaylistEntry(episode.getId());
                    newSelection.setSelected(true);
                    playlistDao.updateEntry(newSelection);
                });
                Timber.d("Metadata changed: %s", metadata.getDescription().getTitle());
                previousUri = uri;
            }
        }
    }
}
