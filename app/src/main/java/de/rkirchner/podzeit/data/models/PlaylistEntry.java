package de.rkirchner.podzeit.data.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "playlist")
public class PlaylistEntry {

    @PrimaryKey
    private int episodeId;
    private int playlistPosition;

    public PlaylistEntry(int episodeId, int playlistPosition) {
        this.episodeId = episodeId;
        this.playlistPosition = playlistPosition;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public int getPlaylistPosition() {
        return playlistPosition;
    }

    public void setPlaylistPosition(int playlistPosition) {
        this.playlistPosition = playlistPosition;
    }

}
