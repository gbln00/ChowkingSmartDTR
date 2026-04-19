package com.chowking.smartdtr.ui.crew;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.EdgeToEdgeHelper;
import com.chowking.smartdtr.utils.QrUtils;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

import androidx.activity.result.ActivityResultLauncher;

public class CrewHomeActivity extends AppCompatActivity {

    private AttendanceViewModel viewModel;
    private SessionManager      session;

    private TextView tvWelcome, tvDate, tvStatus, tvTimer, tvScanResult;
    private ImageView ivQrCode;

    // Live elapsed-time ticker (updates every second while clocked in)
    private Handler  timerHandler;
    private Runnable timerRunnable;
    private long     timeInMillis = 0;
    private boolean  isClockedIn  = false;

    private final ActivityResultLauncher<ScanOptions> qrLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    handleScan(result.getContents());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crew_home);

        EdgeToEdgeHelper.applyInsets(findViewById(R.id.rootLayout));

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("My Attendance");

        session   = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        // Views
        tvWelcome    = findViewById(R.id.tvWelcome);
        tvDate       = findViewById(R.id.tvDate);
        tvStatus     = findViewById(R.id.tvStatus);
        tvTimer      = findViewById(R.id.tvTimer);
        tvScanResult = findViewById(R.id.tvScanResult);
        ivQrCode     = findViewById(R.id.ivQrCode);

        // Welcome header
        tvWelcome.setText("Hello, " + session.getFullName() + "!");
        tvDate.setText(new SimpleDateFormat(
                "EEEE, MMMM dd yyyy", Locale.getDefault()
        ).format(new Date()));

        // Generate QR code for this crew member (background thread)
        generateQrCode();

        // Check if already clocked in today (e.g. after app restart)
        checkCurrentStatus();

        // Scan button
        findViewById(R.id.btnScan).setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Scan your employee QR code");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            qrLauncher.launch(options);
        });

        // My Records button
        findViewById(R.id.btnMyRecords).setOnClickListener(v ->
                startActivity(new Intent(this, CrewHistoryActivity.class))
        );

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            stopTimer();
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void handleScan(String scannedId) {
        viewModel.recordAttendance(scannedId).observe(this, status -> {
            tvScanResult.setVisibility(View.VISIBLE);
            switch (status) {
                case "TIME_IN":
                    tvScanResult.setText("✓ Timed IN successfully!");
                    tvScanResult.setTextColor(getColor(R.color.color_present));
                    isClockedIn  = true;
                    timeInMillis = System.currentTimeMillis();
                    updateStatusBadge(true);
                    startTimer();
                    break;
                case "TIME_OUT":
                    tvScanResult.setText("✓ Timed OUT successfully!");
                    tvScanResult.setTextColor(getColor(R.color.color_still_in));
                    isClockedIn = false;
                    updateStatusBadge(false);
                    stopTimer();
                    break;
                default:
                    tvScanResult.setText("Error recording attendance. Try again.");
                    tvScanResult.setTextColor(getColor(R.color.color_absent));
            }
        });
    }

    /** Check if there's an open punch-in record for today (e.g. app reopened) */
    private void checkCurrentStatus() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        Executors.newSingleThreadExecutor().execute(() -> {
            AttendanceRecord open = AppDatabase.getInstance(this)
                    .attendanceDao()
                    .getOpenRecord(session.getEmployeeId(), today);
            runOnUiThread(() -> {
                if (open != null) {
                    isClockedIn  = true;
                    timeInMillis = open.timeIn;
                    updateStatusBadge(true);
                    startTimer();
                } else {
                    updateStatusBadge(false);
                }
            });
        });
    }

    private void updateStatusBadge(boolean clockedIn) {
        if (clockedIn) {
            tvStatus.setText("● Clocked In");
            tvStatus.setTextColor(getColor(R.color.color_present));
            tvTimer.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("○ Not clocked in");
            tvStatus.setTextColor(getColor(android.R.color.darker_gray));
            tvTimer.setVisibility(View.GONE);
            tvTimer.setText("");
        }
    }

    // ── Elapsed time ticker ────────────────────────────────────────────────

    private void startTimer() {
        timerHandler  = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - timeInMillis;
                long hours   = elapsed / 3600000;
                long minutes = (elapsed % 3600000) / 60000;
                long seconds = (elapsed % 60000) / 1000;

                String timeStr = String.format(
                        Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds
                );
                tvTimer.setText(timeStr);

                // Overtime warning: after 8 hours
                if (hours >= 8) {
                    tvTimer.setTextColor(getColor(R.color.color_overtime));
                    tvTimer.setText(timeStr + " ⚠ OT");
                } else {
                    tvTimer.setTextColor(getColor(R.color.color_present));
                }

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    // ── QR code generation ─────────────────────────────────────────────────

    private void generateQrCode() {
        String employeeId = session.getEmployeeId();
        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap qr = QrUtils.generate(employeeId, 600);
            runOnUiThread(() -> {
                if (qr != null) ivQrCode.setImageBitmap(qr);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}