package com.chowking.smartdtr.ui.crew;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.AttendanceAdapter;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.QrUtils;
import com.chowking.smartdtr.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CrewDashboardFragment extends Fragment {

    private SessionManager session;
    private TextView tvWelcome, tvDate, tvStatus, tvTimer, tvWeekHours, tvWeekPay, tvNoRecords;
    private ImageView ivQrCode;
    private AttendanceAdapter adapter;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long timeInMillis = 0;
    private boolean isClockedIn = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crew_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());

        tvWelcome    = view.findViewById(R.id.tvWelcome);
        tvDate       = view.findViewById(R.id.tvDate);
        tvStatus     = view.findViewById(R.id.tvStatus);
        tvTimer      = view.findViewById(R.id.tvTimer);
        tvWeekHours  = view.findViewById(R.id.tvWeekHours);
        tvWeekPay    = view.findViewById(R.id.tvWeekPay);
        tvNoRecords  = view.findViewById(R.id.tvNoRecords);
        ivQrCode     = view.findViewById(R.id.ivQrCode);

        tvWelcome.setText("Hello, " + session.getFullName() + "!");
        tvDate.setText(new SimpleDateFormat("EEEE, MMMM dd yyyy",
                Locale.getDefault()).format(new Date()));

        RecyclerView rv = view.findViewById(R.id.rvRecentRecords);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        generateQrCode();
        loadDashboardData();
    }

    private void loadDashboardData() {
        String today = todayStr();
        String weekStart = weekStartStr();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Clock-in status
            AttendanceRecord open = db.attendanceDao()
                    .getOpenRecord(session.getEmployeeId(), today);

            // Weekly hours
            float weekHours = db.attendanceDao()
                    .getTotalHoursByEmployee(session.getEmployeeId(), weekStart, today);

            // Estimated pay
            User user = db.userDao().getActiveUserByEmployeeId(session.getEmployeeId());
            float rate = user != null ? user.hourlyRate : 76.25f;
            float maxRegular = db.attendanceDao()
                    .getDaysWorkedByEmployee(session.getEmployeeId(), weekStart, today) * 8f;
            float regularHrs = Math.min(weekHours, maxRegular);
            float overtimeHrs = Math.max(0, weekHours - maxRegular);
            float estPay = (regularHrs * rate) + (overtimeHrs * rate * 1.25f);

            // Last 5 records
            List<AttendanceRecord> all = db.attendanceDao()
                    .getRecordsByEmployee(session.getEmployeeId());
            List<AttendanceRecord> recent = all.size() > 5
                    ? all.subList(0, 5) : all;

            float finalEstPay = estPay;
            requireActivity().runOnUiThread(() -> {
                // Stats
                tvWeekHours.setText(String.format(Locale.getDefault(), "%.1f hrs", weekHours));
                tvWeekPay.setText(String.format(Locale.getDefault(), "₱%.2f", finalEstPay));

                // Clock status
                if (open != null) {
                    isClockedIn  = true;
                    timeInMillis = open.timeIn;
                    updateStatusBadge(true);
                    startTimer();
                } else {
                    updateStatusBadge(false);
                }

                // Recent records
                if (recent.isEmpty()) {
                    tvNoRecords.setVisibility(View.VISIBLE);
                } else {
                    tvNoRecords.setVisibility(View.GONE);
                    adapter.updateRecords(recent);
                }
            });
        });
    }

    public void refreshStatus() {
        stopTimer();
        loadDashboardData();
    }

    private void updateStatusBadge(boolean clockedIn) {
        if (clockedIn) {
            tvStatus.setText("● Clocked in");
            tvTimer.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("○ Not clocked in");
            tvTimer.setVisibility(View.GONE);
            tvTimer.setText("");
        }
    }

    private void startTimer() {
        timerHandler  = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isAdded()) return;
                long elapsed = System.currentTimeMillis() - timeInMillis;
                long h = elapsed / 3600000;
                long m = (elapsed % 3600000) / 60000;
                long s = (elapsed % 60000) / 1000;
                tvTimer.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", h, m, s));
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

    private void generateQrCode() {
        String id = session.getEmployeeId();
        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap qr = QrUtils.generate(id, 600);
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (qr != null) ivQrCode.setImageBitmap(qr);
                });
            }
        });
    }

    private String todayStr() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String weekStartStr() {
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int delta = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
        cal.add(Calendar.DAY_OF_MONTH, delta);
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopTimer();
    }
}