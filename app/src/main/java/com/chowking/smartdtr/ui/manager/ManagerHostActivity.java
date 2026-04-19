package com.chowking.smartdtr.ui.manager;

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

public class ManagerHostActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_host);

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

        // Populate drawer header
        View header = navView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tvDrawerName)).setText(session.getFullName());

        // Default screen
        if (savedInstanceState == null) {
            loadFragment(new ManagerDashboardFragment(), "Dashboard");
            navView.setCheckedItem(R.id.nav_manager_dashboard);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_manager_dashboard) {
            loadFragment(new ManagerDashboardFragment(), "Dashboard");
        } else if (id == R.id.nav_manager_scan) {
            loadFragment(new ManagerScanFragment(), "Scan Attendance");
        } else if (id == R.id.nav_manager_attendance) {
            loadFragment(new ManagerAttendanceFragment(), "Attendance Log");
        } else if (id == R.id.nav_manager_report) {
            loadFragment(new ManagerReportFragment(), "Report");
        } else if (id == R.id.nav_manager_salary) {
            loadFragment(new ManagerSalaryFragment(), "Salary");
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