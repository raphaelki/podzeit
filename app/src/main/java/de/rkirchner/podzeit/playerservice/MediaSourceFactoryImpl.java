package de.rkirchner.podzeit.playerservice;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;

import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class MediaSourceFactoryImpl implements TimelineQueueEditor.MediaSourceFactory {

    private DefaultHttpDataSourceFactory dataSourceFactory;

    public MediaSourceFactoryImpl(Context context) {
        String userAgent = Util.getUserAgent(context, "zeit");
        dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
    }

    @Nullable
    @Override
    public MediaSource createMediaSource(MediaDescriptionCompat description) {
        return new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(description.getMediaUri());
    }
}
