package de.rkirchner.podzeit.ui.episodelist;

import java.util.Objects;

public class EpisodesPlaylistJoin {

    private boolean wasPlayed;
    private int id;
    private String title;
    private String duration;
    private String summary;
    private String size;
    // will be 0 if not in playlist
    private int episodeId;
    private String pubDate;

    public boolean isWasPlayed() {
        return wasPlayed;
    }

    public void setWasPlayed(boolean wasPlayed) {
        this.wasPlayed = wasPlayed;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        EpisodesPlaylistJoin object = (EpisodesPlaylistJoin) obj;
        return Objects.equals(id, object.id)
                && Objects.equals(title, object.title)
                && Objects.equals(summary, object.summary)
                && Objects.equals(size, object.size)
                && Objects.equals(pubDate, object.pubDate)
                && Objects.equals(wasPlayed, object.wasPlayed)
                && Objects.equals(duration, object.duration)
                && Objects.equals(episodeId, object.episodeId);
    }
}
