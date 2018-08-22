package de.rkirchner.podzeit.ui.episodedetails;


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
import de.rkirchner.podzeit.databinding.FragmentEpisodeDetailsBinding;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import de.rkirchner.podzeit.ui.episodelist.EpisodesPlaylistJoin;

public class EpisodeDetailsFragment extends DaggerFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    FormatterUtil formatterUtil;
    private FragmentEpisodeDetailsBinding binding;
    private EpisodesPlaylistJoin episode;

    public EpisodeDetailsFragment() {
        // Required empty public constructor
    }

    public static EpisodeDetailsFragment create(int episodeId) {
        EpisodeDetailsFragment fragment = new EpisodeDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(Constants.EPISODE_ID_KEY, episodeId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_episode_details, container, false);
        binding.setFormatter(formatterUtil);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EpisodeDetailsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(EpisodeDetailsViewModel.class);
        if (getArguments() != null && getArguments().containsKey(Constants.EPISODE_ID_KEY)) {
            viewModel.setEpisodeId(getArguments().getInt(Constants.EPISODE_ID_KEY));
        }
        viewModel.getEpisode().observe(this, episode -> {
            if (episode != null) {
                this.episode = episode;
                binding.setEpisode(episode);
            }
        });
        binding.episodeDetailsItemPlayIcon.setOnClickListener(v -> {
            viewModel.playEpisode();
        });
        binding.episodeDetailsItemPlaylistAddIcon.setOnClickListener(
                v -> {
                    if (episode == null) return;
                    if (episode.getEpisodeId() == 0) {
                        episode.setEpisodeId(episode.getId());
                        viewModel.addToPlaylist();
//                        setPlaylistAddIcon(true);
                    } else {
                        viewModel.removeFromPlaylist();
                        episode.setEpisodeId(0);
//                        setPlaylistAddIcon(false);
                    }
                });
    }
}
