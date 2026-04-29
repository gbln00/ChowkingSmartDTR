package com.chowking.smartdtr.ui.crew;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class CrewHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Attendance History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SessionManager session = new SessionManager(this);
        RecyclerView rv = findViewById(R.id.rvHistory);
        AttendanceAdapter adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        TextView tvSummary = findViewById(R.id.tvSummary);
        TextView tvEmptyState = findViewById(R.id.tvEmptyState);

        Executors.newSingleThreadExecutor().execute(() -> {
            List<AttendanceRecord> records = AppDatabase.getInstance(this)
                    .attendanceDao()
                    .getRecordsByEmployeeSync(session.getEmployeeId());

            runOnUiThread(() -> {
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
                        if (r.timeOut > 0) {
                            done++;
                            total += r.totalHours;
                        }
                    }
                    tvSummary.setText(String.format(Locale.getDefault(),
                            "Shifts: %d  ·  Total hours: %.1f", done, total));
                }
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}