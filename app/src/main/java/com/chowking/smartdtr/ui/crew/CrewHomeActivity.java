package com.chowking.smartdtr.ui.crew;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrewHomeActivity extends AppCompatActivity {

    private AttendanceViewModel viewModel;
    private SessionManager session;
    private TextView tvResult;

    // Modern ZXing launcher (replaces deprecated onActivityResult)
    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedId = result.getContents();
                    viewModel.recordAttendance(scannedId).observe(this, status -> {
                        tvResult.setVisibility(View.VISIBLE);
                        if ("TIME_IN".equals(status)) {
                            tvResult.setText("Timed IN successfully!");
                            tvResult.setTextColor(0xFF2E7D32);
                        } else if ("TIME_OUT".equals(status)) {
                            tvResult.setText("Timed OUT successfully!");
                            tvResult.setTextColor(0xFF1565C0);
                        } else {
                            tvResult.setText("Error recording attendance.");
                            tvResult.setTextColor(0xFFD32F2F);
                        }
                    });
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_home);

        session   = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);
        tvResult  = findViewById(R.id.tvResult);

        // Set welcome text and date
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvDate    = findViewById(R.id.tvDate);
        tvWelcome.setText("Welcome, " + session.getFullName() + "!");
        tvDate.setText(new SimpleDateFormat(
                "EEEE, MMMM dd yyyy", Locale.getDefault()
        ).format(new Date()));

        // Scan button
        findViewById(R.id.btnScan).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan your employee QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            qrLauncher.launch(options);
        });

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}