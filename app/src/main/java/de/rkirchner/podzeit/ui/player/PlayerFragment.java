package de.rkirchner.podzeit.ui.player;


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
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentPlayerBinding;

public class PlayerFragment extends DaggerFragment {

    private FragmentPlayerBinding binding;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    PlayerVisibilityListener playerVisibilityListener;
    private PlayerViewModel viewModel;
    private boolean isPlaying = false;

    public PlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_player, container, false);
        binding.playerPlay.setOnClickListener(v -> {
            if (isPlaying) viewModel.pausePlayback();
            else viewModel.startPlayback();
        });
        return binding.getRoot();
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(PlayerViewModel.class);
        viewModel.isPlaying().observe(this, isPlaying -> {
            if (isPlaying != null) {
                this.isPlaying = isPlaying;
                binding.playerPlay.setImageResource(isPlaying ? R.drawable.exo_controls_pause : R.drawable.exo_controls_play);
            }
        });
    }
}
