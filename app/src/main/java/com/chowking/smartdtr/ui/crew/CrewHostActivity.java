package com.chowking.smartdtr.ui.crew;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CrewHostActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_host);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // Default fragment
        if (savedInstanceState == null) {
            loadFragment(new CrewDashboardFragment(), "Home");
            bottomNav.setSelectedItemId(R.id.nav_crew_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_crew_home) {
                loadFragment(new CrewDashboardFragment(), "Home");
                return true;
            } else if (id == R.id.nav_crew_scan) {
                loadFragment(new CrewScanFragment(), "Scan QR");
                return true;
            } else if (id == R.id.nav_crew_history) {
                loadFragment(new CrewHistoryFragment(), "My history");
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();
    }

    public void logout() {
        session.logout();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}