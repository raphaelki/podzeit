package de.rkirchner.podzeit.ui.logindialog;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;
import de.rkirchner.podzeit.Constants;
import de.rkirchner.podzeit.R;
import de.rkirchner.podzeit.databinding.ActivityLoginBinding;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends DaggerAppCompatActivity {

    @Inject
    ViewModelProvider.Factory viewModelFactory;
    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LoginViewModel.class);

        if (getIntent().hasExtra(Constants.URI_AUTHORITY_KEY)) {
            viewModel.setAuthority(getIntent().getStringExtra(Constants.URI_AUTHORITY_KEY));
        }

        viewModel.getSeries().observe(this, series -> {
            if (series != null) {
                binding.setSeries(series);
                viewModel.getMail().observe(this, mail -> {
                    if (mail != null && !mail.isEmpty())
                        binding.email.setText(mail);
                });
                viewModel.getPassword().observe(this, pw -> {
                    if (pw != null && !pw.isEmpty())
                        binding.password.setText(pw);
                });
            }
        });

        binding.password.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        binding.emailSignInButton.setOnClickListener(view -> attemptLogin());
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        binding.email.setError(null);
        binding.password.setError(null);

        // Store values at the time of the login attempt.
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            binding.password.setError(getString(R.string.error_invalid_password));
            focusView = binding.password;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            binding.email.setError(getString(R.string.error_field_required));
            focusView = binding.email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            binding.email.setError(getString(R.string.error_invalid_email));
            focusView = binding.email;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            viewModel.setCredentials(email, password);
            finish();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }
}

