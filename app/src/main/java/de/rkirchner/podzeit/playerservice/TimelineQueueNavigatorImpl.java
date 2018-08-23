package de.rkirchner.podzeit.playerservice;

import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;

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
}
