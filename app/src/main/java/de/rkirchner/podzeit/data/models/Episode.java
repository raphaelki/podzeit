package de.rkirchner.podzeit.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Objects;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        foreignKeys = @ForeignKey(
                entity = Series.class,
                parentColumns = "rss_url",
                childColumns = "series_rss_url",
                onDelete = CASCADE),
        indices = {@Index(value = "id"), @Index(value = "url", unique = true), @Index(value = "series_rss_url")},
        tableName = "podcast_episodes")
public class Episode {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String subtitle;
    private String summary;
    @NonNull
    private String url;
    private String duration;
    private String pubDate;
    private int episodeNo;
    @ColumnInfo(name = "series_rss_url")
    private String seriesRssUrl;
    private String author;
    private String link;
    private String size;
    private boolean wasPlayed;
    private boolean isNew;

    public Episode() {
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isWasPlayed() {
        return wasPlayed;
    }

    public void setWasPlayed(boolean wasPlayed) {
        this.wasPlayed = wasPlayed;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public int getEpisodeNo() {
        return episodeNo;
    }

    public void setEpisodeNo(int episodeNo) {
        this.episodeNo = episodeNo;
    }

    public String getSeriesRssUrl() {
        return seriesRssUrl;
    }

    public void setSeriesRssUrl(String seriesRssUrl) {
        this.seriesRssUrl = seriesRssUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Episode episode = (Episode) obj;
        return Objects.equals(id, episode.id)
                && Objects.equals(title, episode.title)
                && Objects.equals(summary, episode.summary)
                && Objects.equals(url, episode.url)
                && Objects.equals(duration, episode.duration)
                && Objects.equals(pubDate, episode.pubDate)
                && Objects.equals(episodeNo, episode.episodeNo)
                && Objects.equals(seriesRssUrl, episode.seriesRssUrl)
                && Objects.equals(author, episode.author)
                && Objects.equals(link, episode.link)
                && Objects.equals(size, episode.size);
    }
}
