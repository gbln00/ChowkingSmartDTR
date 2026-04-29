package com.chowking.smartdtr.ui.crew;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class CrewHostActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_host);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(0xFFFFFFFF);

        navView.setNavigationItemSelectedListener(this);

        // Populate drawer header with session data
        View header = navView.getHeaderView(0);
        TextView tvName = header.findViewById(R.id.tvDrawerName);
        TextView tvRole = header.findViewById(R.id.tvDrawerRole);
        tvName.setText(session.getFullName());
        tvRole.setText("Service Crew");

        // Default screen
        if (savedInstanceState == null) {
            loadFragment(new CrewDashboardFragment(), "Home");
            navView.setCheckedItem(R.id.nav_crew_home);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_crew_home) {
            loadFragment(new CrewDashboardFragment(), "Home");
        } else if (id == R.id.nav_crew_history) {
            loadFragment(new CrewHistoryFragment(), "My History");
        } else if (id == R.id.nav_crew_payslip) {
            loadFragment(new CrewPayslipFragment(), "My Payslip");
        } else if (id == R.id.nav_logout) {
            confirmLogout();
            return true;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log out", (d, w) -> {
                    session.logout();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}