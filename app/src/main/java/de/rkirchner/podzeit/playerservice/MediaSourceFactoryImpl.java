package de.rkirchner.podzeit.playerservice;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;

import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import de.rkirchner.podzeit.Constants;

public class MediaSourceFactoryImpl implements TimelineQueueEditor.MediaSourceFactory {

    private final String AUTHORIZATION_REQUEST_KEY = "Authorization";
    private final String AUTH_PREFIX = "Basic ";
    private final String USER_AGENT = "zeit";

    private String userAgent;

    public MediaSourceFactoryImpl(Context context) {
        userAgent = Util.getUserAgent(context, USER_AGENT);
    }

    @Nullable
    @Override
    public MediaSource createMediaSource(MediaDescriptionCompat description) {
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        // add credentials if available
        if (description.getExtras() != null && description.getExtras().containsKey(Constants.CREDENTIALS_KEY)) {
            HttpDataSource.RequestProperties requestProperties = dataSourceFactory.getDefaultRequestProperties();
            String auth = AUTH_PREFIX + description.getExtras().getString(Constants.CREDENTIALS_KEY);
            requestProperties.set(AUTHORIZATION_REQUEST_KEY, auth);
        }
        return new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(description.getMediaUri());
    }
}
