package de.rkirchner.podzeit.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.MainActivity;

@Module
public abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = FragmentBindingModule.class)
    abstract MainActivity mainActivity();
}
