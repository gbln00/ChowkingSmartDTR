package com.chowking.smartdtr.ui.crew;

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
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CrewHistoryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crew_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        RecyclerView rv = view.findViewById(R.id.rvHistory);
        AttendanceAdapter adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        TextView tvSummary    = view.findViewById(R.id.tvSummary);
        View tvEmptyState     = view.findViewById(R.id.tvEmptyState);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AttendanceRecord> records = AppDatabase.getInstance(requireContext())
                    .attendanceDao()
                    .getRecordsByEmployeeSync(session.getEmployeeId());

            requireActivity().runOnUiThread(() -> {
                if (records.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvSummary.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    tvSummary.setVisibility(View.VISIBLE);
                    adapter.updateRecords(records);

                    int done = 0;
                    float total = 0;
                    for (AttendanceRecord r : records) {
                        if (r.timeOut > 0) { done++; total += r.totalHours; }
                    }
                    tvSummary.setText(String.format(Locale.getDefault(),
                            "Shifts: %d  ·  Total hours: %.1f", done, total));
                }
            });
        });
    }
}