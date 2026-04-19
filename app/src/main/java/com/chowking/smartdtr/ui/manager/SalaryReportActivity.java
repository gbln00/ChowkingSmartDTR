package com.chowking.smartdtr.ui.manager;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.SalaryAdapter;
import com.chowking.smartdtr.model.SalaryEntry;
import com.chowking.smartdtr.utils.EdgeToEdgeHelper;
import com.chowking.smartdtr.viewmodel.SalaryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SalaryReportActivity extends AppCompatActivity {

    private SalaryViewModel viewModel;
    private SalaryAdapter   adapter;

    private TextInputEditText etFromDate, etToDate;
    private TextView tvTotalEmployees, tvTotalHours, tvTotalPayroll;
    private TextView tvEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salary_report);

        // Edge-to-edge insets
        EdgeToEdgeHelper.applyInsets(findViewById(R.id.rootLayout));

        // Toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Salary Report");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(SalaryViewModel.class);

        // Summary card views
        tvTotalEmployees = findViewById(R.id.tvTotalEmployees);
        tvTotalHours     = findViewById(R.id.tvTotalHours);
        tvTotalPayroll   = findViewById(R.id.tvTotalPayroll);
        tvEmptyState     = findViewById(R.id.tvEmptyState);

        // Date inputs
        etFromDate = findViewById(R.id.etFromDate);
        etToDate   = findViewById(R.id.etToDate);

        // Default: current week (Monday to today)
        setDefaultDateRange();

        // RecyclerView
        RecyclerView rv = findViewById(R.id.rvSalary);
        adapter = new SalaryAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Filter button
        MaterialButton btnFilter = findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> loadReport());

        // Load on open
        loadReport();
    }

    private void loadReport() {
        String from = etFromDate.getText() != null
                ? etFromDate.getText().toString().trim() : "";
        String to   = etToDate.getText() != null
                ? etToDate.getText().toString().trim() : "";

        if (from.isEmpty() || to.isEmpty()) return;

        viewModel.getSalaryReport(from, to).observe(this, entries -> {
            adapter.updateEntries(entries);
            updateSummaryCard(entries);

            if (entries.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                tvEmptyState.setText("No attendance records found for this period.");
            } else {
                tvEmptyState.setVisibility(View.GONE);
            }
        });
    }

    private void updateSummaryCard(List<SalaryEntry> entries) {
        int   empCount   = entries.size();
        float totalHours = 0;
        float totalPay   = 0;

        for (SalaryEntry e : entries) {
            totalHours += e.totalHours;
            totalPay   += e.grossPay;
        }

        tvTotalEmployees.setText(String.valueOf(empCount));
        tvTotalHours.setText(
                String.format(Locale.getDefault(), "%.1f hrs", totalHours)
        );
        tvTotalPayroll.setText(
                String.format(Locale.getDefault(), "₱%,.2f", totalPay)
        );
    }

    /** Set "From" to Monday of current week, "To" to today */
    private void setDefaultDateRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        // Move to Monday
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToMonday = (dayOfWeek == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dayOfWeek;
        cal.add(Calendar.DAY_OF_MONTH, daysToMonday);
        String monday = sdf.format(cal.getTime());

        // Today
        cal = Calendar.getInstance();
        String today = sdf.format(cal.getTime());

        etFromDate.setText(monday);
        etToDate.setText(today);
    }
}