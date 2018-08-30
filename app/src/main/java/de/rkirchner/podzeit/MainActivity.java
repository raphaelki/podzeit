package de.rkirchner.podzeit;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.ui.common.NavigationController;

public class MainActivity extends DaggerAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Inject
    PodcastRepository podcastRepository;

    @Inject
    NavigationController navigationController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            navigationController.onToggleVisibility(true);
            navigationController.navigateToPlaylist();
            podcastRepository.startFetch();
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_series:
                navigationController.navigateToSeriesGrid();
                break;
            case R.id.nav_playlist:
                navigationController.navigateToPlaylist();
                break;
            case R.id.nav_settings:
                navigationController.navigateToSettings();
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(Constants.EPISODE_ID_KEY)) {
            navigationController.onEpisodeSelected(intent.getIntExtra(Constants.EPISODE_ID_KEY, 0));
        }
    }
}
