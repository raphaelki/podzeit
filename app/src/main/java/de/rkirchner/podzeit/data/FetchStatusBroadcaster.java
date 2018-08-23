package de.rkirchner.podzeit.data;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.rkirchner.podzeit.Constants;

@Singleton
public class FetchStatusBroadcaster {

    private final LocalBroadcastManager broadcastManager;

    @Inject
    public FetchStatusBroadcaster(Context context) {
        this.broadcastManager = LocalBroadcastManager.getInstance(context);
    }

    public void fireBroadcast(String message) {
        Intent broadcastIntent = new Intent(Constants.FETCH_SERVICE_BROADCAST_ACTION);
        broadcastIntent.putExtra(Constants.FETCH_SERVICE_BROADCAST_MESSAGE, message);
        broadcastManager.sendBroadcast(broadcastIntent);
    }
}
