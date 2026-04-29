package com.chowking.smartdtr.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.HashUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.concurrent.Executors;

public class AdminAddUserFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_add_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText etId       = view.findViewById(R.id.etNewId);
        TextInputEditText etName     = view.findViewById(R.id.etNewName);
        TextInputEditText etEmail    = view.findViewById(R.id.etNewEmail);
        TextInputEditText etPass     = view.findViewById(R.id.etNewPass);
        TextInputEditText etPosition = view.findViewById(R.id.etNewPosition);
        TextInputEditText etRate     = view.findViewById(R.id.etNewRate);
        AutoCompleteTextView acRole  = view.findViewById(R.id.acNewRole);
        TextView tvResult            = view.findViewById(R.id.tvAddResult);
        MaterialButton btnAdd        = view.findViewById(R.id.btnAddUser);

        // ── Role dropdown fix ──────────────────────────────────────────────
        String[] roles = {"CREW", "MANAGER", "ADMIN"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                roles);
        acRole.setAdapter(roleAdapter);
        acRole.setText("CREW", false);

        // Force dropdown to open on click/focus — fixes non-clickable dropdown
        acRole.setOnClickListener(v -> acRole.showDropDown());
        acRole.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) acRole.showDropDown();
        });

        btnAdd.setOnClickListener(v -> {
            String id       = getText(etId);
            String name     = getText(etName);
            String email    = getText(etEmail);
            String pass     = getText(etPass);
            String position = getText(etPosition);
            String role     = acRole.getText().toString().trim();
            String rateStr  = getText(etRate);

            if (id.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                tvResult.setText("ID, name and password are required.");
                tvResult.setTextColor(requireContext().getColor(R.color.color_absent));
                tvResult.setVisibility(View.VISIBLE);
                return;
            }

            float rate = 76.25f;
            try { if (!rateStr.isEmpty()) rate = Float.parseFloat(rateStr); }
            catch (NumberFormatException ignored) {}
            final float finalRate = rate;

            Executors.newSingleThreadExecutor().execute(() -> {
                User existing = AppDatabase.getInstance(requireContext())
                        .userDao().getUserByEmployeeId(id);
                if (existing != null) {
                    requireActivity().runOnUiThread(() -> {
                        tvResult.setText("Employee ID already exists.");
                        tvResult.setTextColor(requireContext().getColor(R.color.color_absent));
                        tvResult.setVisibility(View.VISIBLE);
                    });
                    return;
                }
                User user         = new User();
                user.employeeId   = id;
                user.fullName     = name;
                user.email        = email;
                user.role         = role.isEmpty() ? "CREW" : role;
                user.passwordHash = HashUtils.hashPassword(pass);
                user.position     = position.isEmpty() ? "Crew Member" : position;
                user.hourlyRate   = finalRate;
                user.isActive     = 1;
                AppDatabase.getInstance(requireContext()).userDao().insertUser(user);

                // Sync to Cloud
                FirebaseFirestore.getInstance().collection("users")
                        .document(user.employeeId)
                        .set(user, SetOptions.merge());

                requireActivity().runOnUiThread(() -> {
                    tvResult.setText("✓ " + name + " added successfully.");
                    tvResult.setTextColor(requireContext().getColor(R.color.color_present));
                    tvResult.setVisibility(View.VISIBLE);
                    etId.setText(""); etName.setText(""); etEmail.setText(""); etPass.setText("");
                    etPosition.setText(""); etRate.setText("");
                    acRole.setText("CREW", false);
                });
            });
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}