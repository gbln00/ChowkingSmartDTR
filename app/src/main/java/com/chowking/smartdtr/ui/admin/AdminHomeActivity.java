package com.chowking.smartdtr.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.UserAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.EdgeToEdgeHelper;
import com.chowking.smartdtr.utils.HashUtils;
import com.chowking.smartdtr.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class AdminHomeActivity extends AppCompatActivity {

    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        EdgeToEdgeHelper.applyInsets(findViewById(R.id.rootLayout));

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin — Crew Management");
        }

        SessionManager session = new SessionManager(this);

        // ── Form fields ────────────────────────────────────────────────────
        TextInputEditText etId       = findViewById(R.id.etNewId);
        TextInputEditText etName     = findViewById(R.id.etNewName);
        TextInputEditText etPass     = findViewById(R.id.etNewPass);
        TextInputEditText etPosition = findViewById(R.id.etNewPosition);
        TextInputEditText etRate     = findViewById(R.id.etNewRate);
        AutoCompleteTextView acRole  = findViewById(R.id.acNewRole);
        TextView tvResult            = findViewById(R.id.tvAddResult);

        // Role dropdown
        String[] roles = {"CREW", "MANAGER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, roles
        );
        acRole.setAdapter(roleAdapter);
        acRole.setText("CREW", false); // default

        // ── User list RecyclerView ─────────────────────────────────────────
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        userAdapter = new UserAdapter(new ArrayList<>(), this::onResetPassword, this::onDeactivateUser);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);
        refreshUserList();

        // ── Add user button ────────────────────────────────────────────────
        MaterialButton btnAdd = findViewById(R.id.btnAddUser);
        btnAdd.setOnClickListener(v -> {
            String id       = getText(etId);
            String name     = getText(etName);
            String pass     = getText(etPass);
            String position = getText(etPosition);
            String role     = acRole.getText().toString().trim();
            String rateStr  = getText(etRate);

            if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                tvResult.setText("ID, name, and password are required.");
                tvResult.setTextColor(getColor(R.color.color_absent));
                tvResult.setVisibility(View.VISIBLE);
                return;
            }

            float rate = 76.25f; // Region 10 minimum wage default
            try {
                if (!rateStr.isEmpty()) rate = Float.parseFloat(rateStr);
            } catch (NumberFormatException ignored) {}

            final float finalRate = rate;
            Executors.newSingleThreadExecutor().execute(() -> {
                // Check for duplicate employee ID
                User existing = AppDatabase.getInstance(this)
                        .userDao().getUserByEmployeeId(id);
                if (existing != null) {
                    runOnUiThread(() -> {
                        tvResult.setText("Employee ID already exists.");
                        tvResult.setTextColor(getColor(R.color.color_absent));
                        tvResult.setVisibility(View.VISIBLE);
                    });
                    return;
                }

                User user         = new User();
                user.employeeId   = id;
                user.fullName     = name;
                user.role         = role.isEmpty() ? "CREW" : role;
                user.passwordHash = HashUtils.hashPassword(pass);
                user.position     = position.isEmpty() ? "Crew Member" : position;
                user.hourlyRate   = finalRate;
                user.isActive     = 1;
                AppDatabase.getInstance(this).userDao().insertUser(user);

                runOnUiThread(() -> {
                    tvResult.setText("✓ " + name + " added successfully.");
                    tvResult.setTextColor(getColor(R.color.color_present));
                    tvResult.setVisibility(View.VISIBLE);
                    clearForm(etId, etName, etPass, etPosition, etRate);
                    acRole.setText("CREW", false);
                    refreshUserList();
                });
            });
        });

        // ── Logout ─────────────────────────────────────────────────────────
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void refreshUserList() {
        Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<User> users =
                    AppDatabase.getInstance(this).userDao().getAllUsers();
            runOnUiThread(() -> userAdapter.updateUsers(users));
        });
    }

    private void onResetPassword(User user) {
        // Show a simple dialog to enter a new password
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("New password");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(this)
                .setTitle("Reset password for " + user.fullName)
                .setView(input)
                .setPositiveButton("Reset", (dialog, which) -> {
                    String newPass = input.getText().toString().trim();
                    if (newPass.isEmpty()) return;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        String hash = HashUtils.hashPassword(newPass);
                        AppDatabase.getInstance(this).userDao()
                                .updatePassword(user.id, hash);
                        runOnUiThread(() -> {
                            android.widget.Toast.makeText(this,
                                    "Password reset for " + user.fullName,
                                    android.widget.Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onDeactivateUser(User user) {
        String action = user.isActive == 1 ? "deactivate" : "reactivate";
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to " + action + " " + user.fullName + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        if (user.isActive == 1) {
                            AppDatabase.getInstance(this).userDao().deactivateUser(user.id);
                        } else {
                            AppDatabase.getInstance(this).userDao().reactivateUser(user.id);
                        }
                        runOnUiThread(this::refreshUserList);
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearForm(TextInputEditText... fields) {
        for (TextInputEditText f : fields) f.setText("");
    }
}