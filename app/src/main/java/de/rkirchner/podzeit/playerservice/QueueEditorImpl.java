package de.rkirchner.podzeit.playerservice;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.rkirchner.podzeit.Constants;

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
        queue.add(index, new QueueItem(description, index));
    }

    @Override
    public void onRemoveQueueItem(Player player, MediaDescriptionCompat description) {
        QueueItem itemToRemove = null;
        for (QueueItem item : queue) {
            if (item.getDescription().getMediaId().equals(description.getMediaId())) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            for (int index = (int) itemToRemove.getQueueId() + 1; index < queue.size(); index++) {
                queue.set(index, new QueueItem(queue.get(index).getDescription(), index - 1));
            }
            queue.remove(itemToRemove);
            mediaSession.setQueue(queue);
        }

    }

    @Override
    public void onRemoveQueueItemAt(Player player, int index) {

    }

    @Override
    public String[] getCommands() {
        return new String[]{Constants.QUEUE_REORDER_COMMAND_KEY};
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {
        if (command.equals(Constants.QUEUE_REORDER_COMMAND_KEY)) {
            if (extras != null && extras.containsKey(Constants.QUEUE_REORDER_START_POSITION_KEY) && extras.containsKey(Constants.QUEUE_REORDER_END_POSITION_KEY)) {
                reorderItem(extras.getInt(Constants.QUEUE_REORDER_START_POSITION_KEY), extras.getInt(Constants.QUEUE_REORDER_END_POSITION_KEY));
            }
        }
    }

    private void reorderItem(int startPosition, int endPosition) {
        if (startPosition < endPosition) {
            for (int index = startPosition + 1; index <= endPosition; index++) {
                MediaDescriptionCompat description = queue.get(index).getDescription();
                queue.set(index, new QueueItem(description, index - 1));
            }
            MediaDescriptionCompat description = queue.get(startPosition).getDescription();
            queue.set(startPosition, new QueueItem(description, endPosition));
        } else {
            for (int index = endPosition; index < startPosition; index++) {
                MediaDescriptionCompat description = queue.get(index).getDescription();
                queue.set(index, new QueueItem(description, index + 1));
            }
            MediaDescriptionCompat description = queue.get(startPosition).getDescription();
            queue.set(startPosition, new QueueItem(description, endPosition));
        }
        Collections.sort(queue, (o1, o2) -> Long.compare(o1.getQueueId(), o2.getQueueId()));
        mediaSession.setQueue(queue);
    }
}
