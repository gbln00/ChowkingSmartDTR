package com.chowking.smartdtr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.admin.AdminHostActivity;
import com.chowking.smartdtr.ui.crew.CrewHostActivity;
import com.chowking.smartdtr.ui.manager.ManagerHostActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        session   = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Already logged in? Skip straight to home
        if (session.isLoggedIn()) {
            navigateByRole(session.getRole());
            return;
        }

        EditText etId    = findViewById(R.id.etEmployeeId);
        EditText etPass  = findViewById(R.id.etPassword);
        TextView tvError = findViewById(R.id.tvError);
        Button   btnLogin= findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            String id   = etId.getText().toString().trim();
            String pass = etPass.getText().toString().trim();

            if (id.isEmpty() || pass.isEmpty()) {
                tvError.setText("Please fill in all fields.");
                tvError.setVisibility(View.VISIBLE);
                return;
            }

            tvError.setVisibility(View.GONE);

            viewModel.login(id, pass).observe(this, user -> {
                if (user != null) {
                    session.saveSession(user);
                    navigateByRole(user.role);
                } else {
                    tvError.setText("Invalid Employee ID or password.");
                    tvError.setVisibility(View.VISIBLE);
                }
            });
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
        finish(); // remove LoginActivity from back stack
    }
}