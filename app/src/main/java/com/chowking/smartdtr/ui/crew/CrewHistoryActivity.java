package com.chowking.smartdtr.ui.crew;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.utils.EdgeToEdgeHelper;
import com.chowking.smartdtr.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CrewHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_history);

        EdgeToEdgeHelper.applyInsets(findViewById(R.id.rootLayout));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Attendance History");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);

        RecyclerView rv = findViewById(R.id.rvHistory);
        AttendanceAdapter adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TextView tvSummary   = findViewById(R.id.tvSummary);
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);

        // Load this crew member's full history off the main thread
        Executors.newSingleThreadExecutor().execute(() -> {
            List<AttendanceRecord> records = AppDatabase.getInstance(this)
                    .attendanceDao()
                    .getRecordsByEmployee(session.getEmployeeId());

            runOnUiThread(() -> {
                if (records.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvSummary.setVisibility(View.GONE);
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                    tvSummary.setVisibility(View.VISIBLE);
                    adapter.updateRecords(records);
                    showSummary(records, tvSummary);
                }
            });
        });
    }

    private void showSummary(List<AttendanceRecord> records, TextView tvSummary) {
        int completed = 0;
        float totalHours = 0;
        for (AttendanceRecord r : records) {
            if (r.timeOut > 0) {
                completed++;
                totalHours += r.totalHours;
            }
        }
        tvSummary.setText(String.format(
                Locale.getDefault(),
                "Total shifts: %d  ·  Total hours: %.1f",
                completed, totalHours
        ));
    }
}