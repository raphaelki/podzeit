package de.rkirchner.podzeit.ui.player;


import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.google.android.exoplayer2.ui.TimeBar;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentPlayerBinding;
import de.rkirchner.podzeit.playerclient.PlaylistManager;
import de.rkirchner.podzeit.ui.common.FormatterUtil;

public class PlayerFragment extends DaggerFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    @Inject
    PlaylistManager playlistManager;
    @Inject
    FormatterUtil formatterUtil;
    private FragmentPlayerBinding binding;
    private PlayerViewModel viewModel;

    private boolean isSummaryExpanded;
    private float playbackSpeed;
    private long playbackPosition;
    private long duration;
    private ValueAnimator progressAnimator;
    private boolean isPlaying;
    private boolean isPaused;
    private String mediaId;

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
                updateMediaBarAnimation();
            }
        });
        binding.playerFfwd.setOnClickListener(v -> {
            viewModel.fastForward();
        });
        binding.playerRew.setOnClickListener(v -> {
            viewModel.rewind();
        });
        binding.playerNext.setOnClickListener(v -> {
            viewModel.skipToNext();
        });
        binding.playerPrev.setOnClickListener(v -> {
            viewModel.skipToPrevious();
        });

        binding.playerTitle.setOnClickListener(v -> {
            animateScrollViewChange();
        });
        binding.playerSummary.setOnClickListener(v -> {
            animateScrollViewChange();
        });
        binding.playerExpandArrow.setOnClickListener(v -> {
            animateScrollViewChange();
        });
        viewModel.getCurrentMediaId().observe(this, mediaId -> {
            if (mediaId != null) this.mediaId = mediaId;
        });
        viewModel.getTitle().observe(this, binding.playerTitle::setText);
        viewModel.getSummary().observe(this, binding.playerSummary::setText);
        viewModel.isPaused().observe(this, isPaused -> {
            if (isPaused != null) this.isPaused = isPaused;
        });
        viewModel.getEpisodeDuration().observe(this, duration -> {
            if (duration != null) {
                this.duration = duration;
                updateMediaBarAnimation();
            }
        });
        viewModel.getPlaybackSpeed().observe(this, playbackSpeed -> {
            if (playbackSpeed != null) {
                this.playbackSpeed = playbackSpeed;
                updateMediaBarAnimation();
            }
        });
        viewModel.getPlayPosition().observe(this, position -> {
            if (position != null) {
                    playbackPosition = position;
                updateMediaBarAnimation();
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

    private void setExpandIcon() {
        binding.playerExpandArrow.setBackgroundResource(isSummaryExpanded ? R.drawable.ic_keyboard_arrow_down : R.drawable.ic_keyboard_arrow_up);
    }

    private void animateScrollViewChange() {
        int startHeight = 1;
        int endHeight = 200;
        if (isSummaryExpanded) {
            startHeight = 200;
            endHeight = 1;
        }
        ValueAnimator valueAnimator = ValueAnimator.ofInt(startHeight, endHeight);
        valueAnimator.setInterpolator(new FastOutSlowInInterpolator());
        valueAnimator.setDuration(400);
        valueAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams layoutParams = binding.playerSummaryScrollView.getLayoutParams();
            layoutParams.height = (int) animation.getAnimatedValue();
            binding.playerSummaryScrollView.setLayoutParams(layoutParams);
        });
        valueAnimator.start();
        isSummaryExpanded = !isSummaryExpanded;
        setExpandIcon();
    }

    private boolean mediaBarAnimationValuesAreValid() {
        return duration >= 0 &&
                playbackPosition >= 0 &&
                playbackSpeed > 0.0f
                && playbackPosition < duration;
    }

    private void updateMediaBarAnimation() {
        if (progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if (isPlaying) {
            setupMediaBarAnimation();
        } else if (isPaused) {
            binding.playerTimeBar.setDuration(duration);
            binding.playerTimeBar.setPosition(playbackPosition);
            binding.playerTimeElapsed.setText(formatterUtil.formatMillisecondsDuration((int) playbackPosition));
            binding.playerTimeLeft.setText(String.format("-%s", formatterUtil.formatMillisecondsDuration((int) duration - (int) playbackPosition)));
        }
    }

    private void setupMediaBarAnimation() {
        if (!mediaBarAnimationValuesAreValid()) return;
        final int timeToEnd = (int) ((duration - playbackPosition) / playbackSpeed);
        binding.playerTimeBar.setDuration(duration);
        progressAnimator = ValueAnimator.ofInt((int) playbackPosition, (int) duration).setDuration(timeToEnd);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener((ValueAnimator animation) -> {
            int timeElapsed = (int) animation.getAnimatedValue();
            playbackPosition = timeElapsed;
            binding.playerTimeBar.setPosition(timeElapsed);
            binding.playerTimeElapsed.setText(formatterUtil.formatMillisecondsDuration(timeElapsed));
            binding.playerTimeLeft.setText(String.format("-%s", formatterUtil.formatMillisecondsDuration((int) duration - timeElapsed)));
        });
        progressAnimator.start();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Constants.PLAYER_EXPANDED_STATE_KEY, isSummaryExpanded);
        outState.putLong(Constants.PLAYER_PLAYBACK_POSITION_KEY, playbackPosition);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            restoreSummaryExpandedState(savedInstanceState.getBoolean(Constants.PLAYER_EXPANDED_STATE_KEY));
            playbackPosition = savedInstanceState.getLong(Constants.PLAYER_PLAYBACK_POSITION_KEY);
        }
    }

    private void restoreSummaryExpandedState(boolean isSummaryExpanded) {
        this.isSummaryExpanded = isSummaryExpanded;
        if (isSummaryExpanded) {
            ViewGroup.LayoutParams layoutParams = binding.playerSummaryScrollView.getLayoutParams();
            layoutParams.height = 200;
            binding.playerSummaryScrollView.setLayoutParams(layoutParams);
        }
    }
}
