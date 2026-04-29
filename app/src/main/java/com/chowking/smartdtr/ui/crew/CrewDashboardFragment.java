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
import androidx.lifecycle.ViewModelProvider;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CrewDashboardFragment extends Fragment {

    private SessionManager session;
    private AttendanceViewModel viewModel;
    private TextView tvWelcome, tvDate, tvStatus, tvTimer, tvWeekHours, tvWeekPay, tvNoRecords;
    private ImageView ivQrCode;
    private BarChart barChart;
    private AttendanceAdapter adapter;
    private ShimmerFrameLayout shimmerLayout;
    private View mainContent;

    private Handler  timerHandler;
    private Runnable timerRunnable;
    private long     timeInMillis = 0;

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
        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        shimmerLayout = view.findViewById(R.id.shimmerLayout);
        mainContent = view.findViewById(R.id.mainContent);

        tvWelcome   = view.findViewById(R.id.tvWelcome);
        tvDate      = view.findViewById(R.id.tvDate);
        tvStatus    = view.findViewById(R.id.tvStatus);
        tvTimer     = view.findViewById(R.id.tvTimer);
        tvWeekHours = view.findViewById(R.id.tvWeekHours);
        tvWeekPay   = view.findViewById(R.id.tvWeekPay);
        tvNoRecords = view.findViewById(R.id.tvNoRecords);
        ivQrCode    = view.findViewById(R.id.ivQrCode);
        barChart    = view.findViewById(R.id.barChart);

        setupChart();

        tvWelcome.setText("Hello, " + session.getFullName() + "!");
        tvDate.setText(new SimpleDateFormat("EEEE, MMMM dd yyyy",
                Locale.getDefault()).format(new Date()));

        RecyclerView rv = view.findViewById(R.id.rvRecentRecords);
        adapter = new AttendanceAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        generateQrCode();
        observeData();
    }

    private void observeData() {
        String employeeId = session.getEmployeeId();
        String today = todayStr();

        // Observe open record for status and timer
        viewModel.getOpenRecord(employeeId, today).observe(getViewLifecycleOwner(), open -> {
            stopTimer();
            if (open != null) {
                timeInMillis = open.timeIn;
                updateStatusBadge(true);
                startTimer();
            } else {
                updateStatusBadge(false);
            }
        });

        // Observe recent records
        viewModel.getRecordsByEmployee(employeeId).observe(getViewLifecycleOwner(), records -> {
            if (records == null || records.isEmpty()) {
                tvNoRecords.setVisibility(View.VISIBLE);
                adapter.updateRecords(new ArrayList<>());
            } else {
                tvNoRecords.setVisibility(View.GONE);
                List<AttendanceRecord> recent = records.size() > 5
                        ? records.subList(0, 5) : records;
                adapter.updateRecords(recent);
            }
            
            // Re-calculate stats and chart whenever records change
            loadDashboardStats();
        });
    }

    private void loadDashboardStats() {
        String employeeId = session.getEmployeeId();
        String weekStart = weekStartStr();
        String today = todayStr();

        // Prepare labels for the chart
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        List<String> dateList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int delta = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Explicitly set to Monday
        
        Calendar tempCal = (Calendar) cal.clone();
        for (int i = 0; i < 7; i++) {
            dateList.add(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tempCal.getTime()));
            tempCal.add(Calendar.DAY_OF_MONTH, 1);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Get hours for each day of the week for the chart
            List<BarEntry> entries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                float hours = db.attendanceDao().getTotalHoursByEmployee(employeeId, dateList.get(i), dateList.get(i));
                entries.add(new BarEntry(i, hours));
            }

            float totalWeekHours = db.attendanceDao()
                    .getTotalHoursByEmployee(employeeId, weekStart, today);

            User user = db.userDao().getActiveUserByEmployeeId(employeeId);
            float rate = user != null ? user.hourlyRate : 76.25f;
            float maxReg = db.attendanceDao()
                    .getDaysWorkedByEmployee(employeeId, weekStart, today) * 8f;
            float regHrs = Math.min(totalWeekHours, maxReg);
            float otHrs = Math.max(0, totalWeekHours - maxReg);
            float estPay = (regHrs * rate) + (otHrs * rate * 1.25f);

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    updateChart(entries, days);
                    tvWeekHours.setText(String.format(Locale.getDefault(), "%.1f hrs", totalWeekHours));
                    tvWeekPay.setText(String.format(Locale.getDefault(), "₱%.2f", estPay));

                    // Hide shimmer and show content
                    shimmerLayout.stopShimmer();
                    shimmerLayout.setVisibility(View.GONE);
                    mainContent.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void updateChart(List<BarEntry> entries, String[] days) {
        BarDataSet dataSet = new BarDataSet(entries, "Hours Worked");
        dataSet.setColor(getResources().getColor(R.color.chowking_red));
        dataSet.setValueTextColor(getResources().getColor(R.color.text_secondary));
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);
        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(days));
        barChart.invalidate();
    }

    /** Called by parent activity after a scan result changes status */
    public void refreshStatus() {
        // No longer needed to manually refresh as LiveData observers will handle it
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.text_secondary));

        barChart.getAxisLeft().setDrawGridLines(true);
        barChart.getAxisLeft().setGridColor(0x11000000);
        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.text_secondary));
        barChart.getAxisRight().setEnabled(false);
    }



    private void updateStatusBadge(boolean clockedIn) {
        if (clockedIn) {
            tvStatus.setText("● Clocked in — show QR to clock out");
            tvTimer.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText("○ Not clocked in — show QR to clock in");
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
        int dow   = cal.get(Calendar.DAY_OF_WEEK);
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