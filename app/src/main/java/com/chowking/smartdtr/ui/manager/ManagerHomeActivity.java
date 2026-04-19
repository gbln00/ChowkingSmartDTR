package com.chowking.smartdtr.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.CsvExportUtils;
import com.chowking.smartdtr.utils.EdgeToEdgeHelper;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ManagerHomeActivity extends AppCompatActivity {

    private AttendanceAdapter   adapter;
    private AttendanceViewModel viewModel;
    private List<AttendanceRecord> currentRecords = new ArrayList<>();
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_home);

        EdgeToEdgeHelper.applyInsets(findViewById(R.id.rootLayout));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manager Dashboard");

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        SessionManager session = new SessionManager(this);

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());

        ((TextView) findViewById(R.id.tvDate)).setText("Attendance — " + currentDate);

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvAttendance);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Long-press on a record = manual override dialog
        adapter.setOnLongClickListener(this::showEditDialog);

        // Quick-date chip group
        ChipGroup chipGroup = findViewById(R.id.chipGroupDate);
        ((Chip) findViewById(R.id.chipToday)).setOnClickListener(v -> loadDate(getTodayStr()));
        ((Chip) findViewById(R.id.chipYesterday)).setOnClickListener(v -> loadDate(getYesterdayStr()));
        ((Chip) findViewById(R.id.chipThisWeek)).setOnClickListener(v -> loadDate(currentDate));

        // Load today
        loadDate(currentDate);

        // Toolbar buttons
        findViewById(R.id.btnReport).setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class))
        );
        findViewById(R.id.btnSalary).setOnClickListener(v ->
                startActivity(new Intent(this, SalaryReportActivity.class))
        );
        findViewById(R.id.btnExport).setOnClickListener(v -> exportCsv());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadDate(String date) {
        currentDate = date;
        ((TextView) findViewById(R.id.tvDate)).setText("Attendance — " + date);
        viewModel.getRecordsByDate(date).observe(this, records -> {
            currentRecords = records != null ? records : new ArrayList<>();
            adapter.updateRecords(currentRecords);
            updateCounts(currentRecords);
        });
    }

    private void updateCounts(List<AttendanceRecord> records) {
        int present = 0, stillIn = 0;
        for (AttendanceRecord r : records) {
            if (r.timeOut > 0) present++;
            else stillIn++;
        }
        ((TextView) findViewById(R.id.tvPresentCount)).setText("Done: " + present);
        ((TextView) findViewById(R.id.tvStillInCount)).setText("Still In: " + stillIn);
    }

    // ── Manual override dialog ─────────────────────────────────────────────

    private void showEditDialog(AttendanceRecord record) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 0);

        EditText etTimeIn  = new EditText(this);
        etTimeIn.setHint("Time In (HH:mm)");
        etTimeIn.setText(fmt.format(new Date(record.timeIn)));
        layout.addView(etTimeIn);

        EditText etTimeOut = new EditText(this);
        etTimeOut.setHint("Time Out (HH:mm, leave blank if still in)");
        if (record.timeOut > 0) etTimeOut.setText(fmt.format(new Date(record.timeOut)));
        layout.addView(etTimeOut);

        new AlertDialog.Builder(this)
                .setTitle("Edit record: " + record.employeeId)
                .setMessage("Date: " + record.date + "\nLong-press a record to edit.")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    saveEditedRecord(record, etTimeIn.getText().toString(),
                            etTimeOut.getText().toString());
                })
                .setNegativeButton("Delete Record", (dialog, which) ->
                        confirmDelete(record)
                )
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void saveEditedRecord(AttendanceRecord record,
                                  String timeInStr, String timeOutStr) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                SimpleDateFormat dayFmt  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                long newTimeIn = timeFmt.parse(record.date + " " + timeInStr).getTime();
                record.timeIn  = newTimeIn;

                if (!timeOutStr.isEmpty()) {
                    long newTimeOut   = timeFmt.parse(record.date + " " + timeOutStr).getTime();
                    record.timeOut    = newTimeOut;
                    float hours       = (newTimeOut - newTimeIn) / 3600000f;
                    record.totalHours = Math.round(hours * 100) / 100f;
                } else {
                    record.timeOut    = 0;
                    record.totalHours = 0;
                }

                AppDatabase.getInstance(this).attendanceDao().updateRecord(record);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Record updated.", Toast.LENGTH_SHORT).show();
                    loadDate(currentDate);
                });
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Invalid time format. Use HH:mm.", Toast.LENGTH_LONG).show()
                );
            }
        });
    }

    private void confirmDelete(AttendanceRecord record) {
        new AlertDialog.Builder(this)
                .setTitle("Delete record?")
                .setMessage("This will permanently delete the attendance record for "
                        + record.employeeId + " on " + record.date + ".")
                .setPositiveButton("Delete", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(this).attendanceDao().deleteRecord(record.id);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Record deleted.", Toast.LENGTH_SHORT).show();
                            loadDate(currentDate);
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── CSV export ─────────────────────────────────────────────────────────

    private void exportCsv() {
        if (currentRecords.isEmpty()) {
            Toast.makeText(this, "No records to export.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = CsvExportUtils.exportAndShare(this, currentRecords, currentDate);
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, "Export attendance report"));
        } else {
            Toast.makeText(this, "Export failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getTodayStr() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getYesterdayStr() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }
}