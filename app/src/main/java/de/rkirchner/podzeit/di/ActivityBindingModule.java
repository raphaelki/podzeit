package de.rkirchner.podzeit.di;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.ui.common.NavigationController;
import de.rkirchner.podzeit.ui.player.PlayerVisibilityListener;

@Module
public abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = FragmentBindingModule.class)
    abstract MainActivity mainActivity();

    @Binds
    abstract PlayerVisibilityListener bindPlayerVisibilityListener(NavigationController navigationController);
}
