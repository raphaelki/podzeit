package de.rkirchner.podzeit.playerservice;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.concurrent.ExecutionException;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.MetadataJoin;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

public class PlaybackPreparerImpl implements MediaSessionConnector.PlaybackPreparer {

    private static final String USER_AGENT = "zeit";
    private static final String AUTH_PREFIX = "Basic ";
    private static final String AUTHORIZATION_REQUEST_KEY = "Authorization";
    private SimpleExoPlayer exoPlayer;
    private PlaylistDao playlistDao;
    private EpisodeDao episodeDao;
    private AppExecutors appExecutors;
    private Context context;
    private DefaultHttpDataSourceFactory dataSourceFactory;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private RequestOptions requestOptions = new RequestOptions()
            .fallback(R.drawable.ic_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
    private String userAgent;

    public PlaybackPreparerImpl(SimpleExoPlayer exoPlayer, PlaylistDao playlistDao, EpisodeDao episodeDao, AppExecutors appExecutors, Context context, ConcatenatingMediaSource concatenatingMediaSource) {
        this.exoPlayer = exoPlayer;
        this.playlistDao = playlistDao;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.context = context;
        this.concatenatingMediaSource = concatenatingMediaSource;
        userAgent = Util.getUserAgent(context, USER_AGENT);
        dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
    }

    @Override
    public long getSupportedPrepareActions() {
        return PlaybackStateCompat.ACTION_PREPARE_FROM_URI |
                PlaybackStateCompat.ACTION_PLAY_FROM_URI;
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {

    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {

    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
        // command to play back episode
        // if episode is in playlist -> prepare mediasource -> play -> select in playlist
        // if episode is not in playlist -> add to mediasource -> prepare mediasource -> play -> add to playlist as selected
        appExecutors.diskIO().execute(() -> {
            Episode episode = episodeDao.getEpisodeForUrl(uri.toString());
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(episode.getId());
            if (playlistEntry == null) {
                addAndPlayNewEntry(uri);
            } else {
                exoPlayer.prepare(concatenatingMediaSource);
                exoPlayer.seekTo(playlistEntry.getPlaylistPosition(), playlistEntry.getPlaybackPosition());
            }
        });
    }

    private void addAndPlayNewEntry(Uri uri) {
        MetadataJoin episode = episodeDao.getEpisodeSync(uri.toString());
        MediaSource mediaSource = buildMediaSourceForEpisode(episode);
        concatenatingMediaSource.addMediaSource(mediaSource);
        exoPlayer.prepare(concatenatingMediaSource);
        PlaylistEntry newEntry = new PlaylistEntry(episode.getId(), concatenatingMediaSource.getSize() - 1, false, 0);
        playlistDao.insertEntry(newEntry);
        exoPlayer.seekTo(concatenatingMediaSource.getSize() - 1, 0);
    }

    public void prepareMediaSourceFromDbPlaylist() {
        appExecutors.diskIO().execute(() -> {
            concatenatingMediaSource.clear();
            buildPlaylistMediaSource();
            exoPlayer.prepare(concatenatingMediaSource);
            PlaylistEntry newSelection = playlistDao.getSelectedPlaylistEntry();
            if (newSelection != null)
                exoPlayer.seekTo(newSelection.getPlaylistPosition(), newSelection.getPlaybackPosition());
        });
    }

    private MediaSource buildMediaSourceForEpisode(MetadataJoin episode) {
        ExtractorMediaSource.Factory factory = new ExtractorMediaSource.Factory(dataSourceFactory);
        if (episode.getCredentials() != null && !episode.getCredentials().isEmpty()) {
            HttpDataSource.RequestProperties requestProperties = dataSourceFactory.getDefaultRequestProperties();
            String auth = AUTH_PREFIX + episode.getCredentials();
            requestProperties.set(AUTHORIZATION_REQUEST_KEY, auth);
        }
        factory.setTag(getMediaDescription(episode));
        return factory.createMediaSource(Uri.parse(episode.getUrl()));
    }

    private void buildPlaylistMediaSource() {
        List<PlaylistEntry> playlistEntries = playlistDao.getPlaylistEntriesSync();
        for (PlaylistEntry entry : playlistEntries) {
            MetadataJoin episode = episodeDao.getEpisodeSync(entry.getEpisodeId());
            MediaSource mediaSource = buildMediaSourceForEpisode(episode);
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
    }

    private MediaDescriptionCompat getMediaDescription(MetadataJoin episode) {
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        return builder.setTitle(episode.getEpisodeTitle())
                .setDescription(episode.getSummary())
                .setSubtitle(episode.getSeriesTitle())
                .setMediaUri(Uri.parse(episode.getUrl()))
                .setIconBitmap(retrieveThumbnail(episode.getId()))
                .build();
    }

    @Override
    public String[] getCommands() {
        return new String[]{
                Constants.QUEUE_COMMAND_ADD,
                Constants.QUEUE_COMMAND_MOVE,
                Constants.QUEUE_COMMAND_REMOVE,
                Constants.QUEUE_COMMAND_REBUILD_MEDIA_SOURCE};
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {
        Timber.d("Command received: %s", command);
        if (command.equals(Constants.QUEUE_COMMAND_MOVE)) {
            int startPosition = extras.getInt(Constants.QUEUE_MOVE_FROM_KEY);
            int endPosition = extras.getInt(Constants.QUEUE_MOVE_TO_KEY);
            concatenatingMediaSource.moveMediaSource(startPosition, endPosition);
        } else if (command.equals(Constants.QUEUE_COMMAND_ADD)) {
            int episodeId = extras.getInt(Constants.QUEUE_EPISODE_ID_KEY);
            appExecutors.diskIO().execute(() -> {
                PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(episodeId);
                if (playlistEntry != null) return;
                MetadataJoin episode = episodeDao.getEpisodeSync(episodeId);
                MediaSource mediaSource = buildMediaSourceForEpisode(episode);
                concatenatingMediaSource.addMediaSource(mediaSource);
                playlistEntry = new PlaylistEntry(episodeId, concatenatingMediaSource.getSize() - 1, false, 0);
                playlistDao.insertEntry(playlistEntry);
            });
        } else if (command.equals(Constants.QUEUE_COMMAND_REMOVE)) {
            int position = extras.getInt(Constants.QUEUE_PLAYLIST_POSITION_KEY);
            concatenatingMediaSource.removeMediaSource(position);
        } else if (command.equals(Constants.QUEUE_COMMAND_REBUILD_MEDIA_SOURCE)) {
            prepareMediaSourceFromDbPlaylist();
        }
    }

    private Bitmap retrieveThumbnail(int episodeId) {
        MetadataJoin episode = episodeDao.getEpisodeSync(episodeId);
        Bitmap bitmap = null;
        try {
            bitmap = Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .asBitmap()
                    .load(episode.getThumbnailUrl())
                    .submit(144, 144)
                    .get();
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            Timber.e("Could not load thumbnail for episodeId: %s", episodeId);
        }
        return bitmap;
    }
}
