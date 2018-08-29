package de.rkirchner.podzeit.data.models;

import java.util.Objects;

public class EpisodePlaylistEntryJoin {

    private int id;
    private String title;
    private int playlistPosition;
    private String duration;
    private String url;
    private boolean isSelected;
    private String seriesTitle;

    public String getSeriesTitle() {
        return seriesTitle;
    }

    public void setSeriesTitle(String seriesTitle) {
        this.seriesTitle = seriesTitle;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPlaylistPosition() {
        return playlistPosition;
    }

    public void setPlaylistPosition(int playlistPosition) {
        this.playlistPosition = playlistPosition;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        EpisodePlaylistEntryJoin object = (EpisodePlaylistEntryJoin) obj;
        return Objects.equals(id, object.id)
                && Objects.equals(isSelected, object.isSelected)
                && Objects.equals(title, object.title)
                && Objects.equals(duration, object.duration);
    }
}
