package de.rkirchner.podzeit;

import android.content.ComponentName;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.design.widget.NavigationView;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.player.MediaPlaybackService;
import de.rkirchner.podzeit.ui.common.NavigationController;

public class MainActivity extends DaggerAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Inject
    PodcastRepository podcastRepository;

    @Inject
    NavigationController navigationController;

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                }
            };
    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            navigationController.bindPlayer();
            navigationController.navigateToPlaylist();
            podcastRepository.startFetch();
        }

        createMediaBrowserService();
    }

    private void createMediaBrowserService() {
//        MediaBrowserConnectionCallbacks callbacks = new MediaBrowserConnectionCallbacks(this);
        MediaBrowserCompat.ConnectionCallback callbacks = new MediaBrowserCompat.ConnectionCallback() {

            @Override
            public void onConnected() {
                try {
                    mediaController = new MediaControllerCompat(MainActivity.this, mediaBrowser.getSessionToken());
                    MediaControllerCompat.setMediaController(MainActivity.this, mediaController);
                    mediaController.registerCallback(controllerCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };
        ComponentName componentName = new ComponentName(this, MediaPlaybackService.class);
        mediaBrowser = new MediaBrowserCompat(this, componentName, callbacks, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStop() {
        super.onStop();


        if (MediaControllerCompat.getMediaController(MainActivity.this) != null) {
            MediaControllerCompat.getMediaController(MainActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();

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
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
