package de.rkirchner.podzeit.ui.playlist;

import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;

import java.util.Collections;

import javax.inject.Inject;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.PlaylistItemBinding;
import de.rkirchner.podzeit.ui.common.BindingViewHolder;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import de.rkirchner.podzeit.ui.common.RecyclerViewListAdapter;
import timber.log.Timber;

public class PlaylistAdapter extends RecyclerViewListAdapter<PlaylistItemBinding, EpisodePlaylistEntryJoin> implements ItemTouchHelperAdapter {

    private FormatterUtil formatter;
    private OnStartDragListener onStartDragListener;
    private PlaylistManager playlistManager;
    private PlaybackCallback playbackCallback;

    @Inject
    public PlaylistAdapter(FormatterUtil formatter, PlaylistManager playlistManager) {
        this.formatter = formatter;
        this.playlistManager = playlistManager;
    }

    @Override
    public int getLayoutId() {
        return R.layout.playlist_item;
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<PlaylistItemBinding> holder, int position) {
        holder.binding().setEpisode(getList().get(position));
        holder.binding().setFormatter(formatter);
        holder.binding().playlistItemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Timber.d("Playlist item selected");
                playbackCallback.onStartPlayback(getList().get(position));
            }
        });
        if (onStartDragListener != null) {
            holder.binding().playlistItemSwapIcon.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(holder);
                }
                return false;
            });
        }
    }

    @Override
    public void onItemDismiss(int position) {
        playlistManager.removeEpisodeFromPlaylistAtPlaylistPosition(position);
        getList().remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int startPosition, int endPosition) {
        if (startPosition < endPosition) {
            for (int i = startPosition; i < endPosition; i++) {
                Collections.swap(getList(), i, i + 1);
            }
        } else {
            for (int i = startPosition; i > endPosition; i--) {
                Collections.swap(getList(), i, i - 1);
            }
        }
        notifyItemMoved(startPosition, endPosition);
        playlistManager.movePlaylistEntry(startPosition, endPosition);
        return true;
    }

    public void registerOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
    }

    public void registerPlaybackCallback(PlaybackCallback playbackCallback) {
        this.playbackCallback = playbackCallback;
    }
}
