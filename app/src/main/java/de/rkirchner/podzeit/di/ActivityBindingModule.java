package de.rkirchner.podzeit.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import de.rkirchner.podzeit.MainActivity;
import de.rkirchner.podzeit.playerservice.MediaPlaybackService;
import de.rkirchner.podzeit.ui.logindialog.LoginActivity;

@Module
public abstract class ActivityBindingModule {


    @ContributesAndroidInjector(modules = FragmentBindingModule.class)
    abstract MainActivity mainActivity();

    @ContributesAndroidInjector
    abstract LoginActivity loginActivity();

    @ContributesAndroidInjector
    abstract MediaPlaybackService mediaPlaybackService();
}
