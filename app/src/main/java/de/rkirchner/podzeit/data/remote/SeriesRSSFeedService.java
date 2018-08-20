package de.rkirchner.podzeit.data.remote;

import de.rkirchner.podzeit.data.models.Series;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SeriesRSSFeedService {

    @GET("mp3")
    Call<Series> getMP3Feed();

    @GET("aac")
    Call<Series> getAACFeed();
}
