package de.rkirchner.podzeit.data.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "playlist")
public class PlaylistEntry {

    @PrimaryKey
    private int episodeId;
    private int playlistPosition;
    private boolean isSelected;
    private long playbackPosition;
    private boolean wasPreviousEpisode;

    public PlaylistEntry(int episodeId, int playlistPosition, boolean isSelected, long playbackPosition) {
        this.episodeId = episodeId;
        this.playlistPosition = playlistPosition;
        this.isSelected = isSelected;
        this.playbackPosition = playbackPosition;
    }

    public long getPlaybackPosition() {
        return playbackPosition;
    }

    public void setPlaybackPosition(long playbackPosition) {
        this.playbackPosition = playbackPosition;
    }

    public boolean isWasPreviousEpisode() {
        return wasPreviousEpisode;
    }

    public void setWasPreviousEpisode(boolean wasPreviousEpisode) {
        this.wasPreviousEpisode = wasPreviousEpisode;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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
