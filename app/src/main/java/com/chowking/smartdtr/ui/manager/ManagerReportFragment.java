package com.chowking.smartdtr.ui.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerReportFragment extends Fragment {

    private AttendanceAdapter adapter;
    private AttendanceViewModel viewModel;
    private TextView tvSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel  = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        tvSummary  = view.findViewById(R.id.tvTotalSummary);

        RecyclerView rv = view.findViewById(R.id.rvReport);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        EditText etDate   = view.findViewById(R.id.etFilterDate);
        Button   btnFilter = view.findViewById(R.id.btnFilter);

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        etDate.setText(today);
        loadReport(today);

        btnFilter.setOnClickListener(v -> {
            String d = etDate.getText().toString().trim();
            if (!d.isEmpty()) loadReport(d);
        });
    }

    private void loadReport(String date) {
        viewModel.getRecordsByDate(date).observe(getViewLifecycleOwner(), records -> {
            adapter.updateRecords(records);
            float total = 0;
            int done = 0;
            for (AttendanceRecord r : records) {
                total += r.totalHours;
                if (r.timeOut > 0) done++;
            }
            tvSummary.setText(String.format(Locale.getDefault(),
                    "Records: %d  |  Completed: %d  |  Total hours: %.2f",
                    records.size(), done, total));
        });
    }
}