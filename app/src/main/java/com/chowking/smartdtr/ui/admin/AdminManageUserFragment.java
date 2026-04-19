package com.chowking.smartdtr.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class AdminManageUsersFragment extends Fragment {

    private UserAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rvUsers);
        adapter = new UserAdapter(new ArrayList<>(),
                this::onResetPassword, this::onToggleActive);
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
                                android.widget.Toast.makeText(requireContext(),
                                        "Password reset for " + user.fullName,
                                        android.widget.Toast.LENGTH_SHORT).show());
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onToggleActive(User user) {
        String action = user.isActive == 1 ? "deactivate" : "reactivate";
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to " + action + " " + user.fullName + "?")
                .setPositiveButton("Yes", (d, w) -> Executors.newSingleThreadExecutor().execute(() -> {
                    if (user.isActive == 1)
                        AppDatabase.getInstance(requireContext()).userDao().deactivateUser(user.id);
                    else
                        AppDatabase.getInstance(requireContext()).userDao().reactivateUser(user.id);
                    requireActivity().runOnUiThread(this::loadUsers);
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }
}