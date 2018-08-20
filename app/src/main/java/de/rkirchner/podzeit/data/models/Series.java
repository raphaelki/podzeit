package de.rkirchner.podzeit.data.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.Objects;

@Entity(tableName = "podcast_series")
public class Series {

    private String title;
    private String summary;
    private int episodeCount;
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "rss_url")
    private String rssUrl;
    private String lastBuildDate;
    private String pubDate;
    private String websiteUrl;
    private String subtitle;
    private String author;
    private String category;
    private String thumbnailUrl;

    private String lastSync;
    private boolean hiddenFromUser = false;
    @Ignore
    private List<Episode> episodes;

    public Series() {
    }

    public String getLastSync() {
        return lastSync;
    }

    public void setLastSync(String lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isHiddenFromUser() {
        return hiddenFromUser;
    }

    public void setHiddenFromUser(boolean hiddenFromUser) {
        this.hiddenFromUser = hiddenFromUser;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> episodes) {
        this.episodes = episodes;
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

    public int getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(int episodeCount) {
        this.episodeCount = episodeCount;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public String getLastBuildDate() {
        return lastBuildDate;
    }

    public void setLastBuildDate(String lastBuildDate) {
        this.lastBuildDate = lastBuildDate;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        Series series = (Series) obj;
        return Objects.equals(title, series.title)
                && Objects.equals(summary, series.summary)
                && Objects.equals(episodeCount, series.episodeCount)
                && Objects.equals(rssUrl, series.rssUrl)
                && Objects.equals(lastBuildDate, series.lastBuildDate)
                && Objects.equals(pubDate, series.pubDate)
                && Objects.equals(websiteUrl, series.websiteUrl)
                && Objects.equals(subtitle, series.subtitle)
                && Objects.equals(author, series.author)
                && Objects.equals(category, series.category)
                && Objects.equals(thumbnailUrl, series.thumbnailUrl)
                && Objects.equals(lastSync, series.lastSync)
                && Objects.equals(hiddenFromUser, series.hiddenFromUser);
    }
}
