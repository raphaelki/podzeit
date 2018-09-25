package de.rkirchner.podzeit.playerclient;

public interface IPlaylistManager {

    void addEpisodeToPlaylist(int episodeId);

    void removeEpisode(int episodeId);

    void moveEpisode(int startPosition, int endPosition);

    void playNow(int episodeId);
}
