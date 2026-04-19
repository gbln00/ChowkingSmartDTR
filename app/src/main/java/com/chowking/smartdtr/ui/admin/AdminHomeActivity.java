package com.chowking.smartdtr.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        // ── Role dropdown fix ──────────────────────────────────────────────
        String[] roles = {"CREW", "MANAGER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, roles);
        acRole.setAdapter(roleAdapter);
        acRole.setText("CREW", false);
        acRole.setOnClickListener(v -> acRole.showDropDown());
        acRole.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) acRole.showDropDown();
        });

        // ── User list RecyclerView ─────────────────────────────────────────
        RecyclerView rvUsers = findViewById(R.id.rvUsers);
        userAdapter = new UserAdapter(
                new ArrayList<>(),
                this::onResetPassword,
                this::onDeactivateUser,
                this::onEditUser           // ← edit support
        );
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

            float rate = 76.25f;
            try {
                if (!rateStr.isEmpty()) rate = Float.parseFloat(rateStr);
            } catch (NumberFormatException ignored) {}

            final float finalRate = rate;
            Executors.newSingleThreadExecutor().execute(() -> {
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

    // ── Edit User ──────────────────────────────────────────────────────────

    private void onEditUser(User user) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        android.widget.EditText etName = makeEditText("Full Name", user.fullName,
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        android.widget.EditText etPos  = makeEditText("Position",
                user.position != null ? user.position : "",
                android.text.InputType.TYPE_CLASS_TEXT);
        android.widget.EditText etRate = makeEditText("Hourly Rate (₱/hr)",
                String.valueOf(user.hourlyRate),
                android.text.InputType.TYPE_CLASS_NUMBER
                        | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);

        AutoCompleteTextView acRole = new AutoCompleteTextView(this);
        acRole.setInputType(android.text.InputType.TYPE_NULL);
        String[] roles = {"CREW", "MANAGER", "ADMIN"};
        acRole.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, roles));
        acRole.setText(user.role, false);
        acRole.setOnClickListener(v -> acRole.showDropDown());
        acRole.setHint("Role");
        acRole.setPadding(0, 16, 0, 16);

        layout.addView(etName);
        layout.addView(etPos);
        layout.addView(etRate);
        layout.addView(acRole);

        new AlertDialog.Builder(this)
                .setTitle("Edit — " + user.fullName)
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = etName.getText().toString().trim();
                    String newPos  = etPos.getText().toString().trim();
                    String newRole = acRole.getText().toString().trim();
                    String rateStr = etRate.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    float newRate = user.hourlyRate;
                    try { if (!rateStr.isEmpty()) newRate = Float.parseFloat(rateStr); }
                    catch (NumberFormatException ignored) {}

                    user.fullName   = newName;
                    user.position   = newPos.isEmpty() ? user.position : newPos;
                    user.role       = newRole.isEmpty() ? user.role : newRole;
                    user.hourlyRate = newRate;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(this).userDao().updateUser(user);
                        runOnUiThread(() -> {
                            Toast.makeText(this,
                                    "✓ " + user.fullName + " updated.",
                                    Toast.LENGTH_SHORT).show();
                            refreshUserList();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Reset Password ─────────────────────────────────────────────────────

    private void onResetPassword(User user) {
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
                        runOnUiThread(() ->
                                Toast.makeText(this,
                                        "Password reset for " + user.fullName,
                                        Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Deactivate / Reactivate ────────────────────────────────────────────

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

    // ── Helpers ────────────────────────────────────────────────────────────

    private android.widget.EditText makeEditText(String hint, String value, int inputType) {
        android.widget.EditText et = new android.widget.EditText(this);
        et.setHint(hint);
        et.setText(value);
        et.setInputType(inputType);
        et.setPadding(0, 8, 0, 24);
        return et;
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void clearForm(TextInputEditText... fields) {
        for (TextInputEditText f : fields) f.setText("");
    }
}