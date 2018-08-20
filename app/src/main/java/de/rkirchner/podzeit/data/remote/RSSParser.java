package de.rkirchner.podzeit.data.remote;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.Series;

public class RSSParser extends DefaultHandler {

    private final String ITEM_KEY = "item";
    private final String TITLE_KEY = "title";
    private final String URL_KEY = "link";
    private final String LAST_BUILD_DATE_KEY = "lastBuildDate";
    private final String EPISODE_NO_KEY = "episode";
    private final String PUB_DATE_KEY = "pubDate";
    private final String SUBTITLE_KEY = "subtitle";
    private final String AUTHOR_KEY = "author";
    private final String CATEGORY_KEY = "category";
    private final String CATEGORY_ATTRIBUTE_KEY = "text";
    private final String THUMBNAIL_URL_KEY = "image";
    private final String SUMMARY_KEY = "summary";
    private final String EPISODE_URL_ATTRIBUTE_KEY = "url";
    private final String EPISODE_SIZE_ATTRIBUTE_KEY = "length";
    private final String EPISODE_URL_KEY = "enclosure";
    private final String DURATION_KEY = "duration";
    private final String THUMBNAIL_URL_ATTRIBUTE_KEY = "href";
    private final List<Episode> episodes = new ArrayList<>();
    private boolean isReadingEpisodes = false;
    private boolean isTitle = false;
    private boolean isDuration = false;
    private boolean isUrl = false;
    private boolean isLastBuildDate = false;
    private boolean isPubDate = false;
    private boolean isSubtitle = false;
    private boolean isAuthor = false;
    private boolean isSummary = false;
    private boolean isEpisodeNo = false;
    private Episode currentEpisode;

    private Series series;

    @Override
    public void endDocument() {
        series.setEpisodeCount(episodes.size());
        series.setEpisodes(episodes);
    }

    @Override
    public void startDocument() {
        series = new Series();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (localName) {
            case TITLE_KEY:
                isTitle = true;
                break;
            case AUTHOR_KEY:
                isAuthor = true;
                break;
            case CATEGORY_KEY:
                if (!isReadingEpisodes)
                    series.setCategory(attributes.getValue(CATEGORY_ATTRIBUTE_KEY));
                break;
            case THUMBNAIL_URL_KEY:
                if (!isReadingEpisodes)
                    series.setThumbnailUrl(attributes.getValue(THUMBNAIL_URL_ATTRIBUTE_KEY));
                break;
            case SUMMARY_KEY:
                isSummary = true;
                break;
            case URL_KEY:
                isUrl = true;
                break;
            case LAST_BUILD_DATE_KEY:
                isLastBuildDate = true;
                break;
            case PUB_DATE_KEY:
                isPubDate = true;
                break;
            case SUBTITLE_KEY:
                isSubtitle = true;
                break;
            case DURATION_KEY:
                isDuration = true;
                break;
            case EPISODE_NO_KEY:
                isEpisodeNo = true;
                break;
            case ITEM_KEY:
                isReadingEpisodes = true;
                currentEpisode = new Episode();
                break;
            case EPISODE_URL_KEY:
                currentEpisode.setUrl(attributes.getValue(EPISODE_URL_ATTRIBUTE_KEY));
                currentEpisode.setSize(attributes.getValue(EPISODE_SIZE_ATTRIBUTE_KEY));
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        if (localName.equals(ITEM_KEY)) {
            episodes.add(currentEpisode);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        String currentString = new String(ch, start, length);
        if (!isReadingEpisodes) {
            if (isTitle) {
                series.setTitle(currentString);
                isTitle = false;
            } else if (isSummary) {
                series.setSummary(currentString);
                isSummary = false;
            } else if (isLastBuildDate) {
                series.setLastBuildDate(currentString);
                isLastBuildDate = false;
            } else if (isPubDate) {
                series.setPubDate(currentString);
                isPubDate = false;
            } else if (isUrl) {
                series.setWebsiteUrl(currentString);
                isUrl = false;
            } else if (isSubtitle) {
                series.setSubtitle(currentString);
                isSubtitle = false;
            } else if (isAuthor) {
                series.setAuthor(currentString);
                isAuthor = false;
            }
        } else {
            if (isTitle) {
                currentEpisode.setTitle(currentString);
                isTitle = false;
            } else if (isDuration) {
                currentEpisode.setDuration(currentString);
                isDuration = false;
            } else if (isSubtitle) {
                currentEpisode.setSubtitle(currentString);
                isSubtitle = false;
            } else if (isSummary) {
                currentEpisode.setSummary(currentString);
                isSummary = false;
            } else if (isUrl) {
                currentEpisode.setLink(currentString);
                isUrl = false;
            } else if (isPubDate) {
                currentEpisode.setPubDate(currentString);
                isPubDate = false;
            } else if (isEpisodeNo) {
                currentEpisode.setEpisodeNo(Integer.parseInt(currentString));
                isEpisodeNo = false;
            } else if (isAuthor) {
                currentEpisode.setAuthor(currentString);
                isAuthor = false;
            }
        }
    }

    public Series getSeries() {
        return series;
    }
}
