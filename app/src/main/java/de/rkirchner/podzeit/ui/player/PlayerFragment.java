package de.rkirchner.podzeit.ui.player;


import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.google.android.exoplayer2.ui.TimeBar;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentPlayerBinding;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import timber.log.Timber;

public class PlayerFragment extends DaggerFragment {

    private FragmentPlayerBinding binding;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    PlayerVisibilityListener playerVisibilityListener;
    private PlayerViewModel viewModel;
    private boolean isPlaying = false;
    @Inject
    FormatterUtil formatterUtil;

    public PlayerFragment() {
        // Required empty public constructor
    }

    private Handler bufferingHandler = new Handler();
    private PlaybackStateCompat playbackState;
    private long bufferUpdateTime = 500;
    private long duration = 0;
    private ValueAnimator progressAnimator;

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
        viewModel.getMetadata().observe(this, metadata -> {
            if (metadata != null) {
                duration = metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                Timber.d("duration: %s", duration);
                binding.playerTimeBar.setDuration(duration);
            }
        });
        viewModel.getPlaybackState().observe(this, state -> {
            playbackState = state;
            if (state != null) {
                long position = state.getPosition();
                float playbackSpeed = state.getPlaybackSpeed();
                if (progressAnimator != null) {
                    progressAnimator.cancel();
                    progressAnimator = null;
                }
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    setupMediaBarAnimation(position, playbackSpeed);
                }
            }
        });

        binding.playerTimeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                viewModel.setPlayerToPosition(position);
            }
        });
    }

    private void setupMediaBarAnimation(long position, float playbackSpeed) {
        final int timeToEnd = (int) ((duration - position) / playbackSpeed);
        progressAnimator = ValueAnimator.ofInt((int) position, (int) duration).setDuration(timeToEnd);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener((ValueAnimator animation) -> {
            int timeElapsed = (int) animation.getAnimatedValue();
            binding.playerTimeBar.setPosition(timeElapsed);
            binding.playerTimeElapsed.setText(formatterUtil.formatMillisecondsDuration(timeElapsed));
            binding.playerTimeLeft.setText(String.format("-%s", formatterUtil.formatMillisecondsDuration((int) duration - timeElapsed)));
        });
        progressAnimator.start();
    }
}
