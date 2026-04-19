package com.chowking.smartdtr.ui.manager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
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

public class ReportActivity extends AppCompatActivity {

    private AttendanceAdapter adapter;
    private AttendanceViewModel viewModel;
    private TextView tvTotalSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        viewModel       = new ViewModelProvider(this).get(AttendanceViewModel.class);
        tvTotalSummary  = findViewById(R.id.tvTotalSummary);

        RecyclerView rv = findViewById(R.id.rvReport);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        EditText etDate = findViewById(R.id.etFilterDate);
        Button   btnFilter = findViewById(R.id.btnFilter);

        // Load today's report by default
        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());
        etDate.setText(today);
        loadReport(today);

        btnFilter.setOnClickListener(v -> {
            String date = etDate.getText().toString().trim();
            if (!date.isEmpty()) loadReport(date);
        });
    }

    private void loadReport(String date) {
        viewModel.getRecordsByDate(date).observe(this, records -> {
            adapter.updateRecords(records);
            showSummary(records);
        });
    }

    private void showSummary(List<AttendanceRecord> records) {
        float totalHours = 0;
        int completed = 0;
        for (AttendanceRecord r : records) {
            totalHours += r.totalHours;
            if (r.timeOut > 0) completed++;
        }
        tvTotalSummary.setText(
                String.format(Locale.getDefault(),
                        "Records: %d  |  Completed: %d  |  Total Hours: %.2f",
                        records.size(), completed, totalHours)
        );
    }
}