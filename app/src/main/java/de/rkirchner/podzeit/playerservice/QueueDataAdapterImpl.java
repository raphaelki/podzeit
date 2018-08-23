package de.rkirchner.podzeit.playerservice;

import android.support.v4.media.MediaDescriptionCompat;

import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;

import java.util.Collections;
import java.util.List;

public class QueueDataAdapterImpl implements TimelineQueueEditor.QueueDataAdapter {

    private ConcatenatingMediaSource concatenatingMediaSource;
    private List<MediaDescriptionCompat> queue;

    public QueueDataAdapterImpl(ConcatenatingMediaSource concatenatingMediaSource, List<MediaDescriptionCompat> queue) {
        this.concatenatingMediaSource = concatenatingMediaSource;
        this.queue = queue;
    }

    @Override
    public MediaDescriptionCompat getMediaDescription(int position) {
        return queue.get(position);
    }

    @Override
    public void add(int position, MediaDescriptionCompat description) {
        queue.add(position, description);
    }

    @Override
    public void remove(int position) {
        queue.remove(position);
    }

    @Override
    public void move(int from, int to) {
        if (from < to) {
            for (int i = from; i < to; i++) {
                Collections.swap(queue, i, i + 1);
            }
        } else {
            for (int i = from; i > to; i--) {
                Collections.swap(queue, i, i - 1);
            }
        }
    }
}
