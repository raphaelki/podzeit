package de.rkirchner.podzeit.di;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import de.rkirchner.podzeit.PodZeit;
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
}
