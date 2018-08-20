package de.rkirchner.podzeit;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import de.rkirchner.podzeit.di.DaggerAppComponent;
import timber.log.Timber;

public class PodZeit extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        deleteDatabase(PodcastDatabase.DATABASE_NAME); // for debugging purposes
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerAppComponent.builder().create(this);
    }
}
