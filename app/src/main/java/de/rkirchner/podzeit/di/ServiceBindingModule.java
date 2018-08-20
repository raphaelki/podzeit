package de.rkirchner.podzeit.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.data.remote.FetchService;

@Module
public abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract FetchService fetchService();
}
