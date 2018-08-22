package de.rkirchner.podzeit.ui.episodelist;

public interface PlaylistListener {

    void onAddToPlaylist(int episodeId);

    void onRemoveFromPlaylist(int playlistPosition);

    void onPlayNow(int episodeId);
}
