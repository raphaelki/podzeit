package de.rkirchner.podzeit.player;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.google.android.exoplayer2.SimpleExoPlayer;

import timber.log.Timber;

public class MediaSessionCallbacks extends MediaSessionCompat.Callback {

    private MediaPlaybackService mediaPlaybackService;
    private final int FOREGROUND_SERVICE_ID = 75384;
    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            focusChange -> Timber.d("Audio focus changed: %s", focusChange);
    private NotificationBuilder notificationBuilder;
    private Context context;
    private MediaSessionCompat mediaSession;
    private SimpleExoPlayer player;

    public MediaSessionCallbacks(MediaPlaybackService mediaPlaybackService, NotificationBuilder notificationBuilder, MediaSessionCompat mediaSession, SimpleExoPlayer player) {
        this.mediaPlaybackService = mediaPlaybackService;
        this.notificationBuilder = notificationBuilder;
        this.mediaSession = mediaSession;
        this.player = player;
        context = mediaPlaybackService.getApplicationContext();
    }

    @Override
    public void onPlay() {
        Log.d("TAG", "onPlay");
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int result = am.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            context.startService(new Intent(context, MediaPlaybackService.class));
            mediaSession.setActive(true);
            player.setPlayWhenReady(true);
            mediaPlaybackService.startForeground(FOREGROUND_SERVICE_ID, notificationBuilder.build());
        }
    }

    @Override
    public void onStop() {
        Timber.d("Stop");
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        am.abandonAudioFocus(audioFocusChangeListener);
        mediaPlaybackService.stopSelf();
        player.stop();
        mediaSession.setActive(false);
        mediaPlaybackService.stopForeground(false);
    }

    @Override
    public void onPause() {
        Timber.d("Pause");
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        player.setPlayWhenReady(false);
        mediaPlaybackService.stopForeground(false);
    }
}
