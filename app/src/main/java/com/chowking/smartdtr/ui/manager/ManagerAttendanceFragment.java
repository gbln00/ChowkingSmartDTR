package com.chowking.smartdtr.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.utils.CsvExportUtils;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ManagerAttendanceFragment extends Fragment {

    private AttendanceAdapter adapter;
    private AttendanceViewModel viewModel;
    private List<AttendanceRecord> currentRecords = new ArrayList<>();
    private String currentDate;
    private TextView tvPresentCount, tvStillInCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);

        tvPresentCount = view.findViewById(R.id.tvPresentCount);
        tvStillInCount = view.findViewById(R.id.tvStillInCount);

        RecyclerView rv = view.findViewById(R.id.rvAttendance);
        adapter = new AttendanceAdapter(new ArrayList<>());
        adapter.setOnLongClickListener(this::showEditDialog);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        Chip chipToday     = view.findViewById(R.id.chipToday);
        Chip chipYesterday = view.findViewById(R.id.chipYesterday);
        chipToday.setOnClickListener(v -> loadDate(todayStr()));
        chipYesterday.setOnClickListener(v -> loadDate(yesterdayStr()));

        MaterialButton btnExport = view.findViewById(R.id.btnExport);
        btnExport.setOnClickListener(v -> exportCsv());

        currentDate = todayStr();
        loadDate(currentDate);
    }

    private void loadDate(String date) {
        currentDate = date;
        viewModel.getRecordsByDate(date).observe(getViewLifecycleOwner(), records -> {
            currentRecords = records != null ? records : new ArrayList<>();
            adapter.updateRecords(currentRecords);
            int present = 0, stillIn = 0;
            for (AttendanceRecord r : currentRecords) {
                if (r.timeOut > 0) present++; else stillIn++;
            }
            tvPresentCount.setText("Done: " + present);
            tvStillInCount.setText("Still in: " + stillIn);
        });
    }

    private void showEditDialog(AttendanceRecord record) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etIn  = new EditText(requireContext());
        etIn.setHint("Time in (HH:mm)");
        etIn.setText(fmt.format(new Date(record.timeIn)));
        layout.addView(etIn);

        EditText etOut = new EditText(requireContext());
        etOut.setHint("Time out (HH:mm, blank = still in)");
        if (record.timeOut > 0) etOut.setText(fmt.format(new Date(record.timeOut)));
        layout.addView(etOut);

        new AlertDialog.Builder(requireContext())
                .setTitle("Edit: " + record.employeeId)
                .setMessage("Date: " + record.date)
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> saveEdit(record,
                        etIn.getText().toString(), etOut.getText().toString()))
                .setNegativeButton("Delete", (d, w) -> confirmDelete(record))
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void saveEdit(AttendanceRecord record, String inStr, String outStr) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                record.timeIn = tf.parse(record.date + " " + inStr).getTime();
                if (!outStr.isEmpty()) {
                    record.timeOut    = tf.parse(record.date + " " + outStr).getTime();
                    record.totalHours = Math.round(
                            ((record.timeOut - record.timeIn) / 3600000f) * 100) / 100f;
                } else {
                    record.timeOut    = 0;
                    record.totalHours = 0;
                }
                AppDatabase.getInstance(requireContext()).attendanceDao().updateRecord(record);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Record updated", Toast.LENGTH_SHORT).show();
                    loadDate(currentDate);
                });
            } catch (Exception e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(),
                                "Invalid format. Use HH:mm", Toast.LENGTH_LONG).show());
            }
        });
    }

    private void confirmDelete(AttendanceRecord record) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete record?")
                .setMessage("This will permanently remove the record for "
                        + record.employeeId + " on " + record.date)
                .setPositiveButton("Delete", (d, w) ->
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase.getInstance(requireContext())
                                    .attendanceDao().deleteRecord(record.id);
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(),
                                        "Record deleted", Toast.LENGTH_SHORT).show();
                                loadDate(currentDate);
                            });
                        }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportCsv() {
        if (currentRecords.isEmpty()) {
            Toast.makeText(requireContext(), "No records to export", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent share = CsvExportUtils.exportAndShare(requireContext(), currentRecords, currentDate);
        if (share != null) startActivity(Intent.createChooser(share, "Export attendance"));
        else Toast.makeText(requireContext(), "Export failed", Toast.LENGTH_SHORT).show();
    }

    private String todayStr() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String yesterdayStr() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.getTime());
    }
}