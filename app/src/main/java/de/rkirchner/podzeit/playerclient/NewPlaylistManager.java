package de.rkirchner.podzeit.playerclient;

import javax.inject.Singleton;

@Singleton
public class NewPlaylistManager {

    private MediaSessionClient mediaSessionClient;

    public NewPlaylistManager(MediaSessionClient mediaSessionClient) {
        this.mediaSessionClient = mediaSessionClient;
    }


}
