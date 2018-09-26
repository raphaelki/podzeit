package de.rkirchner.podzeit.playerservice;

import com.google.android.exoplayer2.Player;

import timber.log.Timber;

public class PlayerEventListener extends Player.DefaultEventListener {

    @Override
    public void onPositionDiscontinuity(int reason) {
        Timber.d("Player discontinuity: %s", reason);
        switch (reason) {
            case Player.DISCONTINUITY_REASON_PERIOD_TRANSITION:

                break;
        }
    }
}
