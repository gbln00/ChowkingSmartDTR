package com.chowking.smartdtr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.*;
import androidx.lifecycle.ViewModelProvider;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.admin.AdminHostActivity;
import com.chowking.smartdtr.ui.crew.CrewHostActivity;
import com.chowking.smartdtr.ui.manager.ManagerHostActivity;
import com.chowking.smartdtr.utils.GoogleAuthHelper;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.LoginViewModel;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private SessionManager session;
    private CredentialManager credentialManager;

    // Used during Google sign-in to link accounts
    private String pendingGoogleId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        session           = new SessionManager(this);
        viewModel         = new ViewModelProvider(this).get(LoginViewModel.class);
        credentialManager = CredentialManager.create(this);

        if (session.isLoggedIn()) {
            navigateByRole(session.getRole());
            return;
        }

        EditText etId      = findViewById(R.id.etEmployeeId);
        EditText etPass    = findViewById(R.id.etPassword);
        TextView tvError   = findViewById(R.id.tvError);
        Button   btnLogin  = findViewById(R.id.btnLogin);
        MaterialButton btnGoogle = findViewById(R.id.btnGoogleSignIn);

        // ── Password login ──────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> {
            String id   = etId.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            if (id.isEmpty() || pass.isEmpty()) {
                showError(tvError, "Please fill in all fields.");
                return;
            }
            tvError.setVisibility(View.GONE);
            viewModel.login(id, pass).observe(this, user -> {
                if (user != null) {
                    // If there's a pending Google ID to link, do it now
                    if (pendingGoogleId != null) {
                        viewModel.linkGoogle(user.id, pendingGoogleId);
                        pendingGoogleId = null;
                        showError(tvError,
                                "✓ Google account linked! You can now sign in with Google.");
                        tvError.setTextColor(getColor(R.color.color_present));
                        tvError.setVisibility(View.VISIBLE);
                    }
                    session.saveSession(user);
                    navigateByRole(user.role);
                } else {
                    if (pendingGoogleId != null) {
                        showError(tvError,
                                "Google account not linked yet. Enter your Employee ID " +
                                        "and password once to link it.");
                    } else {
                        showError(tvError, "Invalid Employee ID or password.");
                    }
                }
            });
        });

        // ── Google Sign-In ──────────────────────────────────────────────
        btnGoogle.setOnClickListener(v -> launchGoogleSignIn(tvError));
    }

    private void launchGoogleSignIn(TextView tvError) {
        credentialManager.getCredentialAsync(
                this,
                GoogleAuthHelper.buildRequest(),
                null,
                Runnable::run,
                new CredentialManagerCallback<GetCredentialResponse,
                        GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse response) {
                        handleGoogleCredential(response, tvError);
                    }

                    @Override
                    public void onError(GetCredentialException e) {
                        runOnUiThread(() ->
                                showError(tvError, "Google sign-in failed: " + e.getMessage())
                        );
                    }
                }
        );
    }

    private void handleGoogleCredential(GetCredentialResponse response,
                                        TextView tvError) {
        Credential credential = response.getCredential();
        if (!(credential instanceof CustomCredential)) return;
        if (!credential.getType()
                .equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) return;

        GoogleIdTokenCredential googleCred =
                GoogleIdTokenCredential.createFrom(
                        ((CustomCredential) credential).getData()
                );

        String googleId = googleCred.getId(); // unique Google account ID

        viewModel.loginWithGoogle(googleId).observe(this, user -> {
            if (user != null) {
                // Account already linked → sign in directly
                session.saveSession(user);
                navigateByRole(user.role);
            } else {
                // Not linked yet → ask them to log in with password first
                pendingGoogleId = googleId;
                runOnUiThread(() -> {
                    showError(tvError,
                            "First time? Enter your Employee ID and password " +
                                    "below to link your Google account.");
                    tvError.setTextColor(getColor(R.color.color_still_in));
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void showError(TextView tv, String msg) {
        runOnUiThread(() -> {
            tv.setText(msg);
            tv.setTextColor(getColor(R.color.color_absent));
            tv.setVisibility(View.VISIBLE);
        });
    }

    private void navigateByRole(String role) {
        Intent intent;
        switch (role) {
            case "MANAGER":
                intent = new Intent(this, ManagerHostActivity.class); break;
            case "ADMIN":
                intent = new Intent(this, AdminHostActivity.class);   break;
            default:
                intent = new Intent(this, CrewHostActivity.class);
        }
        startActivity(intent);
        finish();
    }
}