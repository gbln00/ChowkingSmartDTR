package com.chowking.smartdtr.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.admin.AdminHostActivity;
import com.chowking.smartdtr.ui.crew.CrewHostActivity;
import com.chowking.smartdtr.ui.manager.ManagerHostActivity;
import com.chowking.smartdtr.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager session = new SessionManager(this);

        new Handler().postDelayed(() -> {
            if (session.isLoggedIn()) {
                navigateByRole(session.getRole());
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
            finish();
        }, 2000); // 2 second splash delay
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
    }
}