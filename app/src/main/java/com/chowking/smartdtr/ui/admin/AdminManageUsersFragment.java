package com.chowking.smartdtr.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.UserAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.HashUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class AdminManageUsersFragment extends Fragment {

    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_manage_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvUsers);
        // Pass all three listeners — edit is now included
        adapter = new UserAdapter(
                new ArrayList<>(),
                this::onResetPassword,
                this::onToggleActive,
                this::onEditUser       // ← NEW
        );
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        Executors.newSingleThreadExecutor().execute(() -> {
            java.util.List<User> users = AppDatabase.getInstance(requireContext())
                    .userDao().getAllUsers();
            requireActivity().runOnUiThread(() -> adapter.updateUsers(users));
        });
    }

    // ── Edit User dialog ───────────────────────────────────────────────────

    private void onEditUser(User user) {
        // Build dialog layout programmatically so we can reuse the fragment layout style
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 8);

        // Full Name
        TextInputLayout tilName = makeInputLayout("Full Name");
        TextInputEditText etName = new TextInputEditText(requireContext());
        etName.setText(user.fullName);
        tilName.addView(etName);
        layout.addView(tilName);

        // Position
        TextInputLayout tilPos = makeInputLayout("Position");
        TextInputEditText etPos = new TextInputEditText(requireContext());
        etPos.setText(user.position);
        tilPos.addView(etPos);
        layout.addView(tilPos);

        // Hourly Rate
        TextInputLayout tilRate = makeInputLayout("Hourly Rate (₱/hr)");
        TextInputEditText etRate = new TextInputEditText(requireContext());
        etRate.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etRate.setText(String.valueOf(user.hourlyRate));
        tilRate.addView(etRate);
        layout.addView(tilRate);

        // Role dropdown
        TextInputLayout tilRole = new TextInputLayout(
                requireContext(), null,
                com.google.android.material.R.attr.textInputOutlinedExposedDropdownMenuStyle);
        tilRole.setHint("Role");
        LinearLayout.LayoutParams lpRole = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpRole.setMargins(0, 16, 0, 16);
        tilRole.setLayoutParams(lpRole);

        AutoCompleteTextView acRole = new AutoCompleteTextView(requireContext());
        acRole.setInputType(android.text.InputType.TYPE_NULL);
        String[] roles = {"CREW", "MANAGER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, roles);
        acRole.setAdapter(roleAdapter);
        acRole.setText(user.role, false);
        acRole.setOnClickListener(v -> acRole.showDropDown());
        acRole.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) acRole.showDropDown();
        });
        tilRole.addView(acRole);
        layout.addView(tilRole);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit — " + user.fullName)
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String newName = etName.getText() != null
                            ? etName.getText().toString().trim() : "";
                    String newPos  = etPos.getText()  != null
                            ? etPos.getText().toString().trim()  : "";
                    String newRole = acRole.getText().toString().trim();
                    String rateStr = etRate.getText() != null
                            ? etRate.getText().toString().trim() : "";

                    if (newName.isEmpty()) {
                        Toast.makeText(requireContext(),
                                "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    float newRate = user.hourlyRate;
                    try {
                        if (!rateStr.isEmpty()) newRate = Float.parseFloat(rateStr);
                    } catch (NumberFormatException ignored) {}

                    final float finalRate = newRate;
                    user.fullName   = newName;
                    user.position   = newPos.isEmpty() ? user.position : newPos;
                    user.role       = newRole.isEmpty() ? user.role : newRole;
                    user.hourlyRate = finalRate;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(requireContext()).userDao().updateUser(user);
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(),
                                    "✓ " + user.fullName + " updated.",
                                    Toast.LENGTH_SHORT).show();
                            loadUsers();
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Reset Password dialog ──────────────────────────────────────────────

    private void onResetPassword(User user) {
        android.widget.EditText input = new android.widget.EditText(requireContext());
        input.setHint("New password");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setPadding(48, 24, 48, 24);

        new AlertDialog.Builder(requireContext())
                .setTitle("Reset password — " + user.fullName)
                .setView(input)
                .setPositiveButton("Reset", (d, w) -> {
                    String p = input.getText().toString().trim();
                    if (p.isEmpty()) return;
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(requireContext())
                                .userDao().updatePassword(user.id, HashUtils.hashPassword(p));
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Password reset for " + user.fullName,
                                        Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Activate / Deactivate ──────────────────────────────────────────────

    private void onToggleActive(User user) {
        String action = user.isActive == 1 ? "deactivate" : "reactivate";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to " + action + " " + user.fullName + "?")
                .setPositiveButton("Yes", (d, w) ->
                        Executors.newSingleThreadExecutor().execute(() -> {
                            if (user.isActive == 1)
                                AppDatabase.getInstance(requireContext())
                                        .userDao().deactivateUser(user.id);
                            else
                                AppDatabase.getInstance(requireContext())
                                        .userDao().reactivateUser(user.id);
                            requireActivity().runOnUiThread(this::loadUsers);
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private TextInputLayout makeInputLayout(String hint) {
        TextInputLayout til = new TextInputLayout(
                requireContext(), null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        til.setHint(hint);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 16);
        til.setLayoutParams(lp);
        return til;
    }
}