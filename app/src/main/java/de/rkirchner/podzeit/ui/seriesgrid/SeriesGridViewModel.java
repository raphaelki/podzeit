package de.rkirchner.podzeit.ui.seriesgrid;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.Series;

public class SeriesGridViewModel extends ViewModel {

    private PodcastRepository repository;

    @Inject
    public SeriesGridViewModel(PodcastRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Series>> getSeriesList() {
        return repository.getSeriesList();
    }
}
