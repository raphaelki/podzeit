package de.rkirchner.podzeit.playerclient;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.ui.logindialog.LoginActivity;

public class ErrorHandler {

    private final int UNAUTHORIZED_RESPONSE_CODE = 401;
    private Context context;

    @Inject
    public ErrorHandler(Context context) {
        this.context = context;
    }

    public void handleError(int errorCode, String uriAuthority) {
        if (errorCode == UNAUTHORIZED_RESPONSE_CODE) {
            showLoginDialog(uriAuthority);
        }
    }

    private void showLoginDialog(String uriAuthority) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra(Constants.URI_AUTHORITY_KEY, uriAuthority);
        context.startActivity(intent);
    }
}
