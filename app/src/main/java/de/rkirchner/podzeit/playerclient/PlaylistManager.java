package de.rkirchner.podzeit.playerclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
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
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.local.EpisodeDao;
import de.rkirchner.podzeit.data.local.PlaylistDao;
import de.rkirchner.podzeit.data.models.Episode;
import de.rkirchner.podzeit.data.models.MetadataJoin;
import de.rkirchner.podzeit.data.models.PlaylistEntry;
import de.rkirchner.podzeit.widget.WidgetHelper;
import timber.log.Timber;

@Singleton
public class PlaylistManager {

    private PlaylistDao playlistDao;
    private EpisodeDao episodeDao;
    private AppExecutors appExecutors;
    private MediaSessionClient mediaSessionClient;
    private List<QueueItem> queue;
    private Context context;
    private boolean skipToLastAddedItem = false;

    @Inject
    public PlaylistManager(PlaylistDao playlistDao, EpisodeDao episodeDao, AppExecutors appExecutors, MediaSessionClient mediaSessionClient, Context context) {
        this.playlistDao = playlistDao;
        this.context = context;
        this.episodeDao = episodeDao;
        this.appExecutors = appExecutors;
        this.mediaSessionClient = mediaSessionClient;
        this.mediaSessionClient.getQueueItems().observeForever(queueItems -> {
            queue = queueItems;
            if (skipToLastAddedItem) {
                mediaSessionClient.getTransportControls().skipToQueueItem(queue.size() - 1);
                mediaSessionClient.getTransportControls().play();
                skipToLastAddedItem = false;
            }
        });
        this.mediaSessionClient.getIsServiceConnected().observeForever(isServiceConnected -> {
            if (isServiceConnected != null && isServiceConnected) {
                initializeMediaQueue();
            }
        });
        this.mediaSessionClient.getMediaMetadata().observeForever(this::onMediaDataChanged);
    }

    private void initializeMediaQueue() {
        appExecutors.diskIO().execute(() -> {
            List<PlaylistEntry> playlistEntries = playlistDao.getPlaylistEntriesSync();
            for (PlaylistEntry entry : playlistEntries) {
                mediaSessionClient.getMediaController().addQueueItem(getMetadata(entry.getEpisodeId()), entry.getPlaylistPosition());
            }
            mediaSessionClient.getTransportControls().skipToQueueItem(0);
        });
    }

    private void onMediaDataChanged(MediaMetadataCompat mediaMetadata) {
        appExecutors.diskIO().execute(() -> {
            String url = mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
            Episode currentEpisode = episodeDao.getEpisodeForUrl(url);
            PlaylistEntry playlistEntry = playlistDao.getPlaylistEntry(currentEpisode.getId());
            if (!playlistEntry.isSelected()) {
                List<PlaylistEntry> playlistEntries = playlistDao.getPlaylistEntriesSync();
                for (PlaylistEntry entry : playlistEntries) {
                    entry.setSelected(false);
                }
                playlistDao.updateEntries(playlistEntries);
                playlistEntry = playlistDao.getPlaylistEntry(currentEpisode.getId());
                playlistEntry.setSelected(true);
                playlistDao.updateEntry(playlistEntry);
                WidgetHelper.triggerWidgetUpdate(context);
            }
        });
    }

    public void playNow(int episodeId) {
        appExecutors.diskIO().execute(() -> {
            addEpisodeToPlaylistInternal(episodeId);
            skipToLastAddedItem = true;
        });
    }


    public void playPlaylistEntry(int playlistPosition) {
        mediaSessionClient.getTransportControls().skipToQueueItem(playlistPosition);
        mediaSessionClient.getTransportControls().play();
    }

    @WorkerThread
    private MediaDescriptionCompat getMetadata(int episodeId) {
        MetadataJoin episode = episodeDao.getEpisodeSync(episodeId);
        if (episode == null) {
            Timber.d("Could not find episode with id %s", episodeId);
            return null;
        }
        RequestOptions requestOptions = new RequestOptions()
                .fallback(R.drawable.ic_music_note_black_144dp)
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
        return new MediaDescriptionCompat.Builder()
                .setTitle(episode.getEpisodeTitle())
                .setMediaId(episode.getUrl())
                .setMediaUri(Uri.parse(episode.getUrl()))
                .setIconUri(Uri.parse(episode.getThumbnailUrl()))
                .setIconBitmap(bitmap)
                .setDescription(episode.getSeriesTitle())
                .build();
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
            int currentEpisodeCount = playlistDao.getPlaylistEntriesSync().size();
            playlistDao.insertEntry(new PlaylistEntry(episodeId, currentEpisodeCount, false));
            mediaSessionClient.getMediaController().addQueueItem(getMetadata(episodeId), currentEpisodeCount);
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
        mediaSessionClient.getMediaController().removeQueueItem(getMetadata(entry.getEpisodeId()));
    }

    public void currentEpisodeFinished(String mediaId) {
        appExecutors.diskIO().execute(() -> {
            for (QueueItem item : queue) {
                if (item.getDescription().getMediaId().equals(mediaId)) {
                    Episode episode = episodeDao.getEpisodeForUrl(mediaId);
                    episode.setWasPlayed(true);
                    episodeDao.updateEpisode(episode);
                    break;
                }
            }
        });
    }
}
