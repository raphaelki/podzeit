package de.rkirchner.podzeit.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.PodZeit;
import de.rkirchner.podzeit.widget.PlayerAppWidget;
import retrofit2.Retrofit;

@Module(includes = {
        FirebaseDatabaseModule.class,
        ViewModelModule.class,
        RoomDatabaseModule.class})
public abstract class ApplicationModule {

    @Singleton
    @Provides
    static Retrofit.Builder provideRetrofitBuilder() {
        return new Retrofit.Builder();
    }

    @Singleton
    @Binds
    abstract Application bindApplication(PodZeit podZeit);

    @Singleton
    @Binds
    abstract Context bindContext(Application application);

    @ContributesAndroidInjector
    abstract PlayerAppWidget playerAppWidget();
}
