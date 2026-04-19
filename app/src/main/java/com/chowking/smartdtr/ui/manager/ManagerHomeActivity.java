package com.chowking.smartdtr.ui.manager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ManagerHomeActivity extends AppCompatActivity {

    private AttendanceAdapter adapter;
    private AttendanceViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager_home);

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        SessionManager session = new SessionManager(this);

        // Set today's date label
        String today = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault()
        ).format(new Date());
        ((TextView) findViewById(R.id.tvDate)).setText(
                "Attendance for: " + today
        );

        // Set up RecyclerView
        RecyclerView rv = findViewById(R.id.rvAttendance);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Load today's records
        viewModel.getRecordsByDate(today).observe(this, records -> {
            adapter.updateRecords(records);
            updateCounts(records);
        });

        Button btnReport = findViewById(R.id.btnReport);
        btnReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportActivity.class))
        );


        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void updateCounts(List<AttendanceRecord> records) {
        int present = 0, stillIn = 0;
        for (AttendanceRecord r : records) {
            if (r.timeOut > 0) present++;
            else stillIn++;
        }
        ((TextView) findViewById(R.id.tvPresentCount))
                .setText("Present: " + present);
        ((TextView) findViewById(R.id.tvStillInCount))
                .setText("Still In: " + stillIn);
    }
}