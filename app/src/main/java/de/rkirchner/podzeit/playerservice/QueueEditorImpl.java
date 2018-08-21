package de.rkirchner.podzeit.playerservice;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

import java.util.ArrayList;
import java.util.List;

public class QueueEditorImpl implements MediaSessionConnector.QueueEditor {

    private List<QueueItem> queue = new ArrayList<>();
    private MediaSessionCompat mediaSession;

    public QueueEditorImpl(MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
        mediaSession.setQueue(queue);
    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description) {
        queue.add(new QueueItem(description, queue.size()));
        mediaSession.setQueue(queue);
    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description, int index) {

    }

    @Override
    public void onRemoveQueueItem(Player player, MediaDescriptionCompat description) {

    }

    @Override
    public void onRemoveQueueItemAt(Player player, int index) {

    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }
}
