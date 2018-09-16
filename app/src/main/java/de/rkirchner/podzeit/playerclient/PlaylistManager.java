package de.rkirchner.podzeit.playerclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat.QueueItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueEditor;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.rkirchner.podzeit.AppExecutors;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.MetadataJoin;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import timber.log.Timber;

@Singleton
public class PlaylistManager {

    private PlaylistDao playlistDao;
    private EpisodeDao episodeDao;
    private AppExecutors appExecutors;
    private MediaSessionClient mediaSessionClient;
    private List<QueueItem> queue;
    private Context context;
    private boolean shouldPlayNow = false;

    @Inject
    public PlaylistManager(PlaylistDao playlistDao, EpisodeDao episodeDao, AppExecutors appExecutors, MediaSessionClient mediaSessionClient, Context context) {
        this.playlistDao = playlistDao;
        this.context = context;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.mediaSessionClient = mediaSessionClient;
        this.mediaSessionClient.getQueueItems().observeForever(queueItems -> {
            queue = queueItems;
            Timber.d("Queue changed size: %s", queue.size());
            Timber.d("skip: %s", shouldPlayNow);
            if (shouldPlayNow) {
                mediaSessionClient.getTransportControls().skipToQueueItem(queue.size() - 1);
                mediaSessionClient.getTransportControls().play();
                shouldPlayNow = false;
            }
        });
        this.mediaSessionClient.getIsServiceConnected().observeForever(isServiceConnected -> {
            Timber.d("Player service connected: %s", isServiceConnected);
            if (isServiceConnected != null && isServiceConnected) {
                initializeMediaQueue();
            }
        });
    }

    public void initializeMediaQueue() {
        appExecutors.diskIO().execute(() -> {
            Timber.d("Initializing queue");
            List<PlaylistEntry> playlistEntries = playlistDao.getPlaylistEntriesSync();
            for (PlaylistEntry entry : playlistEntries) {
                MediaDescriptionCompat mediaDescription = buildMediaDescription(entry.getEpisodeId());
                // remove item if it already exists for reinitialization
                mediaSessionClient.getMediaController().removeQueueItem(mediaDescription);
                mediaSessionClient.getMediaController().addQueueItem(mediaDescription, entry.getPlaylistPosition());
            }
            PlaylistEntry episode = playlistDao.getSelectedPlaylistEntry();
            if (episode != null) {
                mediaSessionClient.getTransportControls().skipToQueueItem(episode.getPlaylistPosition());
            }
        });
    }

    public void addEpisodeAndPlayNow(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            int position = addEpisodeToPlaylistInternal(episodeId);
            if (queue == null || position == queue.size()) {
                shouldPlayNow = true;
            } else {
                mediaSessionClient.getTransportControls().skipToQueueItem(position);
                mediaSessionClient.getTransportControls().play();
            }
        });
    }

    public void playPlaylistEntry(int playlistPosition) {
        mediaSessionClient.getTransportControls().skipToQueueItem(playlistPosition);
        mediaSessionClient.getTransportControls().play();
    }

    @WorkerThread
    private MediaDescriptionCompat buildMediaDescription(int episodeId) {
        MetadataJoin episode = episodeDao.getEpisodeSync(episodeId);
        if (episode == null) {
            Timber.d("Could not find episode with id %s", episodeId);
            return null;
        }
        RequestOptions requestOptions = new RequestOptions()
                .fallback(R.drawable.ic_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE);
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
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder()
                .setTitle(episode.getEpisodeTitle())
                .setMediaId(episode.getUrl())
                .setMediaUri(Uri.parse(episode.getUrl()))
                .setIconUri(Uri.parse(episode.getThumbnailUrl()))
                .setIconBitmap(bitmap)
                .setDescription(episode.getSummary())
                .setSubtitle(episode.getSeriesTitle());
        if (episode.getCredentials() != null) {
            Bundle credentials = new Bundle();
            credentials.putString(Constants.CREDENTIALS_KEY, episode.getCredentials());
            builder.setExtras(credentials);
        }
        return builder.build();
    }

    public void addEpisodeToPlaylist(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            addEpisodeToPlaylistInternal(episodeId);
        });
    }

    @WorkerThread
    private int addEpisodeToPlaylistInternal(int episodeId) {
        // if already in the list just play episode at current position
        PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(episodeId);
        if (playlistEntry == null) {
            int currentEpisodeCount = playlistDao.getPlaylistEntryCount();
            playlistDao.insertEntry(new PlaylistEntry(episodeId, currentEpisodeCount, false, 0));
            mediaSessionClient.getMediaController().addQueueItem(buildMediaDescription(episodeId), currentEpisodeCount);
            return currentEpisodeCount;
        } else return playlistEntry.getPlaylistPosition();
    }

    public void removeEpisodeFromPlaylistAtPlaylistPosition(int playlistPosition) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry entry = playlistDao.getPlaylistEntryAtPosition(playlistPosition);
            removePlaylistEntry(entry);
        });
    }

    public void removeEpisodeFromPlaylist(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry entry = playlistDao.getPlaylistEntry(episodeId);
            removePlaylistEntry(entry);
        });
    }

    public void movePlaylistEntry(int startPosition, int endPosition) {
        Timber.d("Item moved from %s to %s", startPosition, endPosition);
        appExecutors.diskIO().execute(() -> {
            PlaylistEntry itemToMove = playlistDao.getPlaylistEntryAtPosition(startPosition);
            List<PlaylistEntry> itemsToReorder;
            if (startPosition < endPosition) {
                itemsToReorder = playlistDao.getEntriesForReordering(startPosition + 1, endPosition);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() - 1);
                }
            } else {
                itemsToReorder = playlistDao.getEntriesForReordering(endPosition, startPosition - 1);
                for (PlaylistEntry itemToReorder : itemsToReorder) {
                    itemToReorder.setPlaylistPosition(itemToReorder.getPlaylistPosition() + 1);
                }
            }
            itemToMove.setPlaylistPosition(endPosition);
            itemsToReorder.add(itemToMove);
            playlistDao.updateEntries(itemsToReorder);
            reorderMediaQueue(startPosition, endPosition);
        });
    }

    private void reorderMediaQueue(int startPosition, int endPosition) {
        Bundle bundle = new Bundle();
        bundle.putInt(TimelineQueueEditor.EXTRA_FROM_INDEX, startPosition);
        bundle.putInt(TimelineQueueEditor.EXTRA_TO_INDEX, endPosition);
        mediaSessionClient.getMediaController().sendCommand(TimelineQueueEditor.COMMAND_MOVE_QUEUE_ITEM, bundle, null);
    }

    @WorkerThread
    private void removePlaylistEntry(PlaylistEntry entry) {
        List<PlaylistEntry> entriesToReorder = playlistDao.getEntriesForReordering(entry.getPlaylistPosition());
        for (PlaylistEntry entryToReorder : entriesToReorder) {
            entryToReorder.setPlaylistPosition(entryToReorder.getPlaylistPosition() - 1);
        }
        playlistDao.updateEntries(entriesToReorder);
        playlistDao.removeEpisodeFromPlaylist(entry);
        mediaSessionClient.getMediaController().removeQueueItem(buildMediaDescription(entry.getEpisodeId()));
    }
}
