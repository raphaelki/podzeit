package de.rkirchner.podzeit.playerservice;

import android.util.Pair;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.ErrorMessageProvider;

public class ErrorMessageProviderImpl implements ErrorMessageProvider<ExoPlaybackException> {
    @Override
    public Pair<Integer, String> getErrorMessage(ExoPlaybackException throwable) {
        int errorCode = 0;
        String message = throwable.getSourceException().getMessage();
        if (throwable.getSourceException() instanceof HttpDataSource.InvalidResponseCodeException) {
            HttpDataSource.InvalidResponseCodeException exception = ((HttpDataSource.InvalidResponseCodeException) throwable.getSourceException());
            errorCode = exception.responseCode;
            message = exception.getMessage() + " " + exception.dataSpec.uri;
        }
        return new Pair<>(errorCode, message);
    }
}
