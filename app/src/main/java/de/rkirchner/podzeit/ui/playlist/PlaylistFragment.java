package de.rkirchner.podzeit.ui.playlist;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.data.models.EpisodePlaylistEntryJoin;
import de.rkirchner.podzeit.databinding.FragmentPlaylistBinding;
import de.rkirchner.podzeit.ui.common.NavigationController;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends DaggerFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    NavigationController navigationController;
    @Inject
    PlaylistAdapter adapter;
    private FragmentPlaylistBinding binding;
    private PlaylistViewModel viewModel;
    private ItemTouchHelper itemTouchHelper;
    private OnStartDragListener onStartDragListener = new OnStartDragListener() {
        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            itemTouchHelper.startDrag(viewHolder);
        }
    };

    public PlaylistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setHasStableIds(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_playlist, container, false);
        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback(adapter, getContext());
        itemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        itemTouchHelper.attachToRecyclerView(binding.playlistRv);
        adapter.registerOnStartDragListener(onStartDragListener);
        adapter.registerPlaybackCallback(new PlaybackCallback() {
            @Override
            public void onStartPlayback(EpisodePlaylistEntryJoin episode) {
                viewModel.startPlayback(episode);
            }

            @Override
            public void onStartPlayback(int episodeId) {
                viewModel.startPlayback(episodeId);
            }
        });
        binding.playlistRv.setAdapter(adapter);
        binding.playlistFab.setOnClickListener(view -> navigationController.navigateToSeriesGrid());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlaylistViewModel.class);
        viewModel.getPlaylistEpisodes().observe(this, episodes -> {
            adapter.swapList(episodes);
            binding.setAdapter(adapter);
            binding.executePendingBindings();
        });
        DrawerLayout drawer = binding.getRoot().getRootView().findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, binding.playlistToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }
}
