package de.rkirchner.podzeit.playerservice;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;

import java.util.List;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

public class TimelineQueueNavigatorImpl extends TimelineQueueNavigator {

    private List<MediaDescriptionCompat> queueItems;
    private AppExecutors appExecutors;
    private PlaylistDao playlistDao;
    private MediaSessionCompat mediaSession;

    public TimelineQueueNavigatorImpl(MediaSessionCompat mediaSession,
                                      int maxQueueSize,
                                      List<MediaDescriptionCompat> queue,
                                      AppExecutors appExecutors,
                                      PlaylistDao playlistDao) {
        super(mediaSession, maxQueueSize);
        this.mediaSession = mediaSession;
        this.queueItems = queue;
        this.playlistDao = playlistDao;
        this.appExecutors = appExecutors;
    }

    @Override
    public MediaDescriptionCompat getMediaDescription(Player player, int windowIndex) {
        return queueItems.get(windowIndex);
    }

    // override method to allow skipping to a playlist item when only one item is in the queue
    @Override
    public long getSupportedQueueNavigatorActions(Player player) {
        if (player == null) return 0;
        if (player.getRepeatMode() != Player.REPEAT_MODE_OFF) {
            return PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    | PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
        }

        int currentWindowIndex = player.getCurrentWindowIndex();
        long actions;
        if (currentWindowIndex == 0) {
            actions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        } else if (currentWindowIndex == player.getCurrentTimeline().getWindowCount() - 1) {
            actions = PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        } else {
            actions = PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
        }
        return actions | PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM;
    }

    @Override
    public void onSkipToQueueItem(Player player, long id) {
        super.onSkipToQueueItem(player, id);
        restorePlaybackPosition(player, id);
    }

    private void restorePlaybackPosition(Player player, long id) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntryAtPosition((int) id);
            if (playlistEntry != null && playlistEntry.getPlaybackPosition() > 0) {
                player.seekTo(playlistEntry.getPlaybackPosition());
                Timber.d("Restoring playback position of %s to %s", playlistEntry.getEpisodeId(), playlistEntry.getPlaybackPosition());
            }
        });
    }

    @Override
    public void onSkipToNext(Player player) {
        super.onSkipToNext(player);
        restorePlaybackPosition(player, mediaSession.getController().getPlaybackState().getActiveQueueItemId());
    }

    @Override
    public void onSkipToPrevious(Player player) {
        super.onSkipToPrevious(player);
        restorePlaybackPosition(player, mediaSession.getController().getPlaybackState().getActiveQueueItemId());
    }
}
