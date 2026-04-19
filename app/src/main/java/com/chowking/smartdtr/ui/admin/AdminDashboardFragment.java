package com.chowking.smartdtr.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.UserAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AdminDashboardFragment extends Fragment {

    private TextView tvGreeting, tvStatTotal, tvStatActive, tvStatInactive;
    private TextView tvCrewCount, tvManagerCount, tvAdminCount;
    private UserAdapter recentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting      = view.findViewById(R.id.tvAdminGreeting);
        tvStatTotal     = view.findViewById(R.id.tvStatTotal);
        tvStatActive    = view.findViewById(R.id.tvStatActive);
        tvStatInactive  = view.findViewById(R.id.tvStatInactive);
        tvCrewCount     = view.findViewById(R.id.tvCrewCount);
        tvManagerCount  = view.findViewById(R.id.tvManagerCount);
        tvAdminCount    = view.findViewById(R.id.tvAdminCount);

        SessionManager session = new SessionManager(requireContext());
        tvGreeting.setText("Welcome, " + session.getFullName());

        RecyclerView rv = view.findViewById(R.id.rvRecentUsers);
        recentAdapter = new UserAdapter(new ArrayList<>(),
                user -> {}, user -> {});
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(recentAdapter);

        loadStats();
    }

    private void loadStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<User> all = db.userDao().getAllUsers();

            int total = all.size(), active = 0, inactive = 0;
            int crew = 0, managers = 0, admins = 0;

            for (User u : all) {
                if (u.isActive == 1) active++; else inactive++;
                switch (u.role) {
                    case "CREW":    crew++;     break;
                    case "MANAGER": managers++; break;
                    case "ADMIN":   admins++;   break;
                }
            }

            // Last 5 added (highest id = most recent)
            List<User> recent = all.size() > 5
                    ? all.subList(all.size() - 5, all.size()) : all;

            int finalTotal    = total;
            int finalActive   = active;
            int finalInactive = inactive;
            int finalCrew     = crew;
            int finalManagers = managers;
            int finalAdmins   = admins;

            requireActivity().runOnUiThread(() -> {
                tvStatTotal.setText(String.valueOf(finalTotal));
                tvStatActive.setText(String.valueOf(finalActive));
                tvStatInactive.setText(String.valueOf(finalInactive));
                tvCrewCount.setText(String.valueOf(finalCrew));
                tvManagerCount.setText(String.valueOf(finalManagers));
                tvAdminCount.setText(String.valueOf(finalAdmins));
                recentAdapter.updateUsers(recent);
            });
        });
    }
}