package com.chowking.smartdtr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.*;
import androidx.credentials.exceptions.GetCredentialException;
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
        ProgressBar pbGoogle = findViewById(R.id.pbGoogle);

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
        btnGoogle.setOnClickListener(v -> launchGoogleSignIn(btnGoogle, pbGoogle, tvError));
    }

    private void launchGoogleSignIn(MaterialButton btn, ProgressBar pb, TextView tvError) {
        btn.setEnabled(false);
        btn.setAlpha(0.5f);
        pb.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);
        
        Toast.makeText(this, "Connecting to Google...", Toast.LENGTH_SHORT).show();

        // Safety timeout: reset UI if Google doesn't respond in 12 seconds
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (pb.getVisibility() == View.VISIBLE) {
                resetGoogleButton(btn, pb);
                showError(tvError, "Connection timed out. Please check your internet and try again.");
            }
        }, 12000);

        try {
            GetCredentialRequest request = GoogleAuthHelper.buildRequest();
            credentialManager.getCredentialAsync(
                    this,
                    request,
                    null,
                    ContextCompat.getMainExecutor(this),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse response) {
                            handleGoogleCredential(response, btn, pb, tvError);
                        }

                        @Override
                        public void onError(GetCredentialException e) {
                            runOnUiThread(() -> {
                                resetGoogleButton(btn, pb);
                                showError(tvError, "Google Error: " + e.getMessage());
                            });
                        }
                    }
            );
        } catch (Exception e) {
            resetGoogleButton(btn, pb);
            showError(tvError, "Critical: " + e.getMessage());
        }
    }

    private void handleGoogleCredential(GetCredentialResponse response,
                                        MaterialButton btn,
                                        ProgressBar pb,
                                        TextView tvError) {
        Credential credential = response.getCredential();
        
        if (!(credential instanceof CustomCredential) || 
            !credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            resetGoogleButton(btn, pb);
            showError(tvError, "Invalid account type selected.");
            return;
        }

        try {
            GoogleIdTokenCredential googleCred = GoogleIdTokenCredential.createFrom(credential.getData());
            String googleId = googleCred.getId(); // This is often the email or unique sub
            String email = googleCred.getId();    // Use the ID as the identifier

            viewModel.loginWithGoogle(googleId, email).observe(this, user -> {
                resetGoogleButton(btn, pb);
                if (user != null) {
                    session.saveSession(user);
                    navigateByRole(user.role);
                } else {
                    pendingGoogleId = googleId;
                    showError(tvError, "Account not found. Please log in with Employee ID first to link your Google account.");
                    tvError.setTextColor(getColor(R.color.color_still_in));
                }
            });
        } catch (Exception e) {
            resetGoogleButton(btn, pb);
            showError(tvError, "Error reading Google profile.");
        }
    }

    private void resetGoogleButton(MaterialButton btn, ProgressBar pb) {
        runOnUiThread(() -> {
            btn.setEnabled(true);
            btn.setAlpha(1.0f);
            pb.setVisibility(View.GONE);
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