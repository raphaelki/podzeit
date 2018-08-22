package de.rkirchner.podzeit.playerservice;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;
import android.support.v4.media.session.PlaybackStateCompat;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

public class QueueNavigatorImpl implements MediaSessionConnector.QueueNavigator {

    private MediaControllerCompat mediaController;
    private Context context;
    private SimpleExoPlayer exoPlayer;
    private long activeQueueItemId;

    public QueueNavigatorImpl(MediaControllerCompat mediaController, Context context, SimpleExoPlayer exoPlayer) {
        this.mediaController = mediaController;
        this.context = context;
        this.exoPlayer = exoPlayer;
    }

    @Override
    public long getSupportedQueueNavigatorActions(@Nullable Player player) {
        return PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
    }

    @Override
    public void onTimelineChanged(Player player) {

    }

    @Override
    public void onCurrentWindowIndexChanged(Player player) {

    }

    @Override
    public long getActiveQueueItemId(@Nullable Player player) {
        return activeQueueItemId;
    }

    @Override
    public void onSkipToPrevious(Player player) {

    }

    @Override
    public void onSkipToQueueItem(Player player, long id) {
        List<QueueItem> queue = mediaController.getQueue();
        MediaDescriptionCompat description = null;
        for (QueueItem item : queue) {
            if (item.getQueueId() == id) description = item.getDescription();
        }
        if (description != null) {
            String userAgent = Util.getUserAgent(context, "zeit");
            DefaultHttpDataSourceFactory defaultHttpDataSourceFactory =
                    new DefaultHttpDataSourceFactory(userAgent);
            MediaSource mediaSource = new ExtractorMediaSource.Factory(
                    defaultHttpDataSourceFactory)
                    .createMediaSource(description.getMediaUri());
            exoPlayer.prepare(mediaSource);
            player.setPlayWhenReady(true);
            activeQueueItemId = id;
        }
    }

    @Override
    public void onSkipToNext(Player player) {

    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }
}
