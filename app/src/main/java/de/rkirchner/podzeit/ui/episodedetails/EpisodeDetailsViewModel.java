package de.rkirchner.podzeit.ui.episodedetails;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.Episode;

public class EpisodeDetailsViewModel extends ViewModel {

    private PodcastRepository repository;
    private MutableLiveData<Integer> episodeId = new MutableLiveData<>();

    @Inject
    public EpisodeDetailsViewModel(PodcastRepository repository) {
        this.repository = repository;
    }

    public LiveData<Episode> getEpisode() {
        return Transformations.switchMap(episodeId, id -> repository.getEpisode(id));
    }

    public void setEpisodeId(int id) {
        episodeId.setValue(id);
    }
}
