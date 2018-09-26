package de.rkirchner.podzeit.playerservice;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController;

import de.rkirchner.podzeit.R;
import timber.log.Timber;

public class PlaybackControllerImpl extends DefaultPlaybackController {

    private Context context;
    private int rewFfwSeconds = 15;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceListener =
            (sharedPreferences, key) -> {
                if (key.equals(context.getString(R.string.shared_pref_seconds_to_skip_key))) {
                    setRewFfwSeconds(sharedPreferences);
                }
            };

    public PlaybackControllerImpl(Context context) {
        this.context = context;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        setRewFfwSeconds(sharedPreferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceListener);
    }

    private void setRewFfwSeconds(SharedPreferences sharedPreferences) {
        String seconds = sharedPreferences.getString(context.getString(R.string.shared_pref_seconds_to_skip_key), "15");
        rewFfwSeconds = Integer.parseInt(seconds);
        Timber.d("rew ffw seconds set to %s", rewFfwSeconds);
    }

    @Override
    public void onFastForward(Player player) {
        Timber.d("Ffw %s seconds", rewFfwSeconds);
        if (rewFfwSeconds <= 0) {
            return;
        }
        onSeekTo(player, player.getCurrentPosition() + rewFfwSeconds * 1000);
    }

    @Override
    public void onRewind(Player player) {
        Timber.d("Rew %s seconds", rewFfwSeconds);
        if (rewFfwSeconds <= 0) {
            return;
        }
        onSeekTo(player, player.getCurrentPosition() - rewFfwSeconds * 1000);
    }
}
