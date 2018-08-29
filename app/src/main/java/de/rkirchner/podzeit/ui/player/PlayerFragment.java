package de.rkirchner.podzeit.ui.player;


import android.animation.ValueAnimator;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.google.android.exoplayer2.ui.TimeBar;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.FragmentPlayerBinding;
import de.rkirchner.podzeit.playerclient.PlaylistManager;
import de.rkirchner.podzeit.ui.common.FormatterUtil;
import timber.log.Timber;

public class PlayerFragment extends DaggerFragment {

    private FragmentPlayerBinding binding;
    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private PlayerViewModel viewModel;
    private boolean isPlaying = false;
    @Inject
    PlaylistManager playlistManager;
    @Inject
    FormatterUtil formatterUtil;
    private boolean summaryExpanded;

    public PlayerFragment() {
        // Required empty public constructor
    }

    private MediaMetadataCompat metadata;
    private PlaybackStateCompat playbackState;
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

    private void setDuration(long duration) {
        if (duration >= 0) {
            this.duration = duration;
        }
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
        viewModel.getMetadata().observe(this, metadata -> {
            if (metadata != null) {
                this.metadata = metadata;
                setDuration(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                binding.playerTitle.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE));
                binding.playerTimeBar.setDuration(duration);
                binding.playerSummary.setText(metadata.getText(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION));
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

    private void setExpandIcon() {
        binding.playerExpandArrow.setBackgroundResource(summaryExpanded ? R.drawable.ic_keyboard_arrow_down : R.drawable.ic_keyboard_arrow_up);
    }

    private void animateScrollViewChange() {
        int startHeight = 1;
        int endHeight = 200;
        if (summaryExpanded) {
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
        summaryExpanded = !summaryExpanded;
        setExpandIcon();
    }

    private void setupMediaBarAnimation(long position, float playbackSpeed) {
        final int timeToEnd = (int) ((duration - position) / playbackSpeed);
        Timber.d("duration: %s, position: %s, playbackSpeed: %s", duration, position, playbackSpeed);
        if (position > duration) {
            onEpisodeFinished();
            return;
        }
        progressAnimator = ValueAnimator.ofInt((int) position, (int) duration).setDuration(timeToEnd);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener((ValueAnimator animation) -> {
            int timeElapsed = (int) animation.getAnimatedValue();
            binding.playerTimeBar.setPosition(timeElapsed);
            binding.playerTimeElapsed.setText(formatterUtil.formatMillisecondsDuration(timeElapsed));
            binding.playerTimeLeft.setText(String.format("-%s", formatterUtil.formatMillisecondsDuration((int) duration - timeElapsed)));
            if (timeElapsed == duration) {
                onEpisodeFinished();
            }
        });
        progressAnimator.start();
    }

    private void onEpisodeFinished() {
        if (metadata != null)
            playlistManager.currentEpisodeFinished(metadata.getDescription().getMediaId());
    }
}
