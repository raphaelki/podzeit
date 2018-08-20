package de.rkirchner.podzeit.di;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import de.rkirchner.podzeit.PodZeit;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ApplicationModule.class,
        ActivityBindingModule.class,
        ServiceBindingModule.class})
public interface AppComponent extends AndroidInjector<PodZeit> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<PodZeit> {
    }
}
