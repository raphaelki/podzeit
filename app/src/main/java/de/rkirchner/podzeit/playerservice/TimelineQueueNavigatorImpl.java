package de.rkirchner.podzeit.playerservice;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator;

import java.util.List;

public class TimelineQueueNavigatorImpl extends TimelineQueueNavigator {

    private List<MediaDescriptionCompat> queueItems;

    public TimelineQueueNavigatorImpl(MediaSessionCompat mediaSession, int maxQueueSize, List<MediaDescriptionCompat> queue) {
        super(mediaSession, maxQueueSize);
        this.queueItems = queue;
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
}
