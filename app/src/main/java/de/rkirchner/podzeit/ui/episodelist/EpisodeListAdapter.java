package de.rkirchner.podzeit.ui.episodelist;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.models.EpisodesPlaylistJoin;
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
        holder.binding().setEpisode(episode);
        holder.binding().setCallback(callback);
        holder.binding().setFormatter(formatterUtil);
        holder.binding().episodeListItemPlaylistAddIcon.setOnClickListener(
                v -> {
                    if (episode.getEpisodeId() == 0) {
                        playlistListener.onAddToPlaylist(episode.getId());
                    } else {
                        playlistListener.onRemoveFromPlaylist(episode.getId());
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

    @Override
    public long getItemId(int position) {
        return getList().get(position).getId();
    }
}
