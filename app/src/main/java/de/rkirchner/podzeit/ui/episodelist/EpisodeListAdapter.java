package de.rkirchner.podzeit.ui.episodelist;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.EpisodeListItemBinding;
import de.rkirchner.podzeit.ui.common.BindingViewHolder;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import de.rkirchner.podzeit.ui.common.RecyclerViewListAdapter;

public class EpisodeListAdapter extends RecyclerViewListAdapter<EpisodeListItemBinding, EpisodesPlaylistJoin> {

    private FormatterUtil formatterUtil;
    private EpisodeListClickCallback callback;
    private PlaylistListener playlistListener;

    @Inject
    public EpisodeListAdapter(FormatterUtil formatterUtil, EpisodeListClickCallback callback) {
        this.formatterUtil = formatterUtil;
        this.callback = callback;
    }

    @Override
    public void onBindViewHolder(@NonNull BindingViewHolder<EpisodeListItemBinding> holder, int position) {
        EpisodesPlaylistJoin episode = getList().get(position);
        setPlaylistAddIcon(episode.getEpisodeId() > 0, holder); // if episode is in playlist the episodeId is greater 0
        holder.binding().setEpisode(episode);
        holder.binding().setCallback(callback);
        holder.binding().setFormatter(formatterUtil);
        holder.binding().episodeListItemPlaylistAddIcon.setOnClickListener(
                v -> {
                    if (episode.getEpisodeId() == 0) {
                        episode.setEpisodeId(episode.getId());
                        playlistListener.onAddToPlaylist(episode.getId());
                        setPlaylistAddIcon(true, holder);
                    } else {
                        playlistListener.onRemoveFromPlaylist(episode.getId());
                        episode.setEpisodeId(0);
                        setPlaylistAddIcon(false, holder);
                    }
                });
        holder.binding().episodeListItemPlayIcon.setOnClickListener(v -> {
            playlistListener.onPlayNow(getList().get(position).getId());
        });
        holder.binding().executePendingBindings();
    }

    @Override
    public int getLayoutId() {
        return R.layout.episode_list_item;
    }

    public void setPlaylistListener(PlaylistListener playlistListener) {
        this.playlistListener = playlistListener;
    }

    private void setPlaylistAddIcon(boolean isAdded, BindingViewHolder<EpisodeListItemBinding> holder) {
        holder.binding().episodeListItemPlaylistAddIcon.setImageResource(isAdded ? R.drawable.ic_playlist_add_check : R.drawable.ic_playlist_add);
    }
}
