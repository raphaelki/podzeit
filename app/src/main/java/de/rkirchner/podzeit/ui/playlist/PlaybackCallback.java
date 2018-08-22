package de.rkirchner.podzeit.ui.playlist;

public interface PlaybackCallback {

    void onStartPlayback(EpisodePlaylistEntryJoin episode);

    void onStartPlayback(int playlistPosition);
}
