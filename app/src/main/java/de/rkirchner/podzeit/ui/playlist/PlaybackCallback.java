package de.rkirchner.podzeit.ui.playlist;

import de.rkirchner.podzeit.data.models.EpisodePlaylistEntryJoin;

public interface PlaybackCallback {

    void onStartPlayback(EpisodePlaylistEntryJoin episode);

    void onStartPlayback(int playlistPosition);
}
