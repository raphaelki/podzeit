package de.rkirchner.podzeit.ui.episodelist;


import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentEpisodeListBinding;
import de.rkirchner.podzeit.ui.common.FormatterUtil;

public class EpisodeListFragment extends DaggerFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    FormatterUtil formatterUtil;
    @Inject
    EpisodeListClickCallback callback;
    @Inject
    EpisodeListAdapter adapter;
    private FragmentEpisodeListBinding binding;
    private EpisodeListViewModel viewModel;
    private PlaylistListener playlistListener = new PlaylistListener() {
        @Override
        public void onAddToPlaylist(int episodeId) {
            viewModel.addEpisodeToPlaylist(episodeId);
        }

        @Override
        public void onRemoveFromPlaylist(int episodeId) {
            viewModel.removeEpisodeFromPlaylist(episodeId);
        }
    };

    public EpisodeListFragment() {
        // Required empty public constructor
    }

    public static EpisodeListFragment create(String rssUrl) {
        EpisodeListFragment episodeListFragment = new EpisodeListFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.RSS_URL_KEY, rssUrl);
        episodeListFragment.setArguments(arguments);
        return episodeListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setPlaylistListener(playlistListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_episode_list, container, false);
        binding.episodeListRv.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(EpisodeListViewModel.class);
        if (getArguments() != null && getArguments().containsKey(Constants.RSS_URL_KEY)) {
            String seriesRssUrl = getArguments().getString(Constants.RSS_URL_KEY);
            viewModel.setSeries(seriesRssUrl);
        }
        viewModel.getSeries().observe(this, binding::setSeries);
        viewModel.getEpisodes().observe(this, adapter::swapList);
    }
}
