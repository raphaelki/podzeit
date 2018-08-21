package de.rkirchner.podzeit.playerclient;

import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;

public class MediaBrowserConnectionCallbacks extends MediaBrowserCompat.ConnectionCallback {

    private Context context;

    public MediaBrowserConnectionCallbacks(Context context) {
        this.context = context;
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onConnectionFailed() {

    }

    @Override
    public void onConnectionSuspended() {

    }
}
