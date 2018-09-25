package de.rkirchner.podzeit.ui.logindialog;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.util.Base64;

import javax.inject.Inject;

import de.rkirchner.podzeit.data.PodcastRepository;
import de.rkirchner.podzeit.data.models.Series;
import de.rkirchner.podzeit.playerclient.PlaylistManager;

public class LoginViewModel extends ViewModel {

    private PodcastRepository repository;
    private MutableLiveData<String> uriAuthority = new MutableLiveData<>();
    private LiveData<Series> series;
    private PlaylistManager playlistManager;

    @Inject
    public LoginViewModel(PodcastRepository repository, PlaylistManager playlistManager) {
        this.repository = repository;
        this.playlistManager = playlistManager;
    }

    public void setCredentials(String username, String password) {
        Series seriesToUpdate = series.getValue();
        seriesToUpdate.setCredentials(encodeCredentials(username, password));
        repository.updateSeries(seriesToUpdate);
    }

    public void setAuthority(String uriAuthority) {
        this.uriAuthority.postValue(uriAuthority);
    }

    public LiveData<Series> getSeries() {
        return Transformations.switchMap(uriAuthority,
                authority -> {
                    LiveData<Series> series = repository.getSeriesMatchtingUriAuthority(authority);
                    this.series = series;
                    return series;
                });
    }

    private String encodeCredentials(String username, String password) {
        byte[] credentials = (username + ":" + password).getBytes();
        return Base64.encodeToString(credentials, Base64.URL_SAFE | Base64.NO_WRAP);
    }

    private String decodeMail(String credentials) {
        if (credentials == null) return "";
        byte[] decoded = Base64.decode(credentials, Base64.URL_SAFE | Base64.NO_WRAP);
        String decodedStr = new String(decoded);
        String[] credentialsArray = decodedStr.split(":");
        return credentialsArray[0];
    }

    private String decodePassword(String credentials) {
        if (credentials == null) return "";
        byte[] decoded = Base64.decode(credentials, Base64.URL_SAFE | Base64.NO_WRAP);
        String decodedStr = new String(decoded);
        String[] credentialsArray = decodedStr.split(":");
        return credentialsArray[1];
    }

    public LiveData<String> getMail() {
        return Transformations.map(series, series -> decodeMail(series.getCredentials()));
    }

    public LiveData<String> getPassword() {
        return Transformations.map(series, series -> decodePassword(series.getCredentials()));
    }
}
