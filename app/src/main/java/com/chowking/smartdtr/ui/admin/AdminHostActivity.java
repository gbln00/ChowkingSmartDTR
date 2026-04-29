package com.chowking.smartdtr.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.google.android.material.navigation.NavigationView;

public class AdminHostActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private SessionManager session;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_host);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(0xFFFFFFFF);

        navView.setNavigationItemSelectedListener(this);

        // Sync Nav View and Title on back stack change
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
            if (currentFragment != null) {
                updateUIForFragment(currentFragment);
            }
        });

        View header = navView.getHeaderView(0);
        ((TextView) header.findViewById(R.id.tvDrawerName)).setText(session.getFullName());

        if (savedInstanceState == null) {
            loadFragment(new AdminDashboardFragment(), "Dashboard", false);
            navView.setCheckedItem(R.id.nav_admin_dashboard);
        }
    }

    private void updateUIForFragment(Fragment fragment) {
        if (fragment instanceof AdminDashboardFragment) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Dashboard");
            navView.setCheckedItem(R.id.nav_admin_dashboard);
        } else if (fragment instanceof AdminAddUserFragment) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Add user");
            navView.setCheckedItem(R.id.nav_admin_add_user);
        } else if (fragment instanceof AdminManageUsersFragment) {
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage users");
            navView.setCheckedItem(R.id.nav_admin_manage);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_dashboard) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            loadFragment(new AdminDashboardFragment(), "Dashboard", false);
        } else if (id == R.id.nav_admin_add_user) {
            loadFragment(new AdminAddUserFragment(), "Add user", true);
        } else if (id == R.id.nav_admin_manage) {
            loadFragment(new AdminManageUsersFragment(), "Manage users", true);
        } else if (id == R.id.nav_logout) {
            confirmLogout();
            return true;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadFragment(Fragment fragment, String title) {
        loadFragment(fragment, title, true);
    }

    private void loadFragment(Fragment fragment, String title, boolean addToBackStack) {
        Fragment current = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (current != null && current.getClass().equals(fragment.getClass())) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(title);
        androidx.fragment.app.FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_right,
                        R.anim.fade_out,
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                )
                .replace(R.id.contentFrame, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
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

    private long backPressedTime;
    private Toast backToast;

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.contentFrame);
            if (!(currentFragment instanceof AdminDashboardFragment)) {
                // If not on dashboard, go to dashboard first
                loadFragment(new AdminDashboardFragment(), "Dashboard", false);
                navView.setCheckedItem(R.id.nav_admin_dashboard);
            } else {
                // If on dashboard, double tap to exit
                if (backPressedTime + 2000 > System.currentTimeMillis()) {
                    if (backToast != null) backToast.cancel();
                    super.onBackPressed();
                } else {
                    backToast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT);
                    backToast.show();
                    backPressedTime = System.currentTimeMillis();
                }
            }
        }
    }
}