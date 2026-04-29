package com.chowking.smartdtr.ui.manager;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.AttendanceRecord;
import androidx.lifecycle.ViewModelProvider;
import com.chowking.smartdtr.viewmodel.AttendanceViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ManagerDashboardFragment extends Fragment {

    private AttendanceViewModel viewModel;
    private TextView tvGreeting, tvDate, tvStatPresent, tvStatStillIn, tvStatTotalHours;
    private BarChart barChart;
    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel        = new ViewModelProvider(requireActivity()).get(AttendanceViewModel.class);
        tvGreeting       = view.findViewById(R.id.tvManagerGreeting);
        tvDate           = view.findViewById(R.id.tvManagerDate);
        tvStatPresent    = view.findViewById(R.id.tvStatPresent);
        tvStatStillIn    = view.findViewById(R.id.tvStatStillIn);
        tvStatTotalHours = view.findViewById(R.id.tvStatTotalHours);
        barChart         = view.findViewById(R.id.barChart);
        pieChart         = view.findViewById(R.id.pieChart);

        tvGreeting.setText("Good " + getGreeting() + "!");
        tvDate.setText(new SimpleDateFormat("EEEE, MMMM dd yyyy",
                Locale.getDefault()).format(new Date()));

        setupBarChart();
        setupPieChart();
        observeData();
    }

    private void observeData() {
        String today = dateStr(new Date());
        viewModel.getRecordsByDate(today).observe(getViewLifecycleOwner(), records -> {
            if (records != null) {
                updateTodayStats(records);
            }
        });

        // Weekly chart data still requires some manual calculation but we trigger it when data changes
        loadWeeklyStats();
    }

    private void updateTodayStats(List<AttendanceRecord> todayRecords) {
        int present = 0, stillIn = 0;
        float totalHours = 0;
        for (AttendanceRecord r : todayRecords) {
            if (r.timeOut > 0) {
                present++;
                totalHours += r.totalHours;
            } else {
                stillIn++;
            }
        }

        tvStatPresent.setText(String.valueOf(present));
        tvStatStillIn.setText(String.valueOf(stillIn));
        tvStatTotalHours.setText(String.format(Locale.getDefault(), "%.1f", totalHours));

        updatePieChart(present, stillIn);
    }

    private void loadWeeklyStats() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            float[] weeklyHours = new float[7];
            Calendar cal = Calendar.getInstance();
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            int delta = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // Explicitly set to Monday

            for (int i = 0; i < 7; i++) {
                String d = dateStr(cal.getTime());
                // Using DAO directly for synchronous aggregation in background thread
                List<AttendanceRecord> dayRecs = db.attendanceDao().getRecordsByDateSync(d);
                for (AttendanceRecord r : dayRecs) weeklyHours[i] += r.totalHours;
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }

            if (isAdded()) {
                requireActivity().runOnUiThread(() -> updateBarChart(weeklyHours));
            }
        });
    }

    private void setupBarChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.getLegend().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.setExtraBottomOffset(8f);
        barChart.getAxisLeft().setTextColor(Color.parseColor("#888780"));
        barChart.getAxisLeft().setGridColor(Color.parseColor("#E8E6E0"));
        barChart.getAxisLeft().setAxisLineColor(Color.TRANSPARENT);
        barChart.getAxisLeft().setTextSize(11f);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setTextColor(Color.parseColor("#888780"));
        barChart.getXAxis().setTextSize(11f);
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(
                new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}));
        barChart.setTouchEnabled(false);
        barChart.animateY(600);
    }

    private void updateBarChart(float[] weeklyHours) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 7; i++) entries.add(new BarEntry(i, weeklyHours[i]));

        BarDataSet dataSet = new BarDataSet(entries, "Hours");
        dataSet.setColor(Color.parseColor("#EE2D24"));
        dataSet.setValueTextColor(Color.parseColor("#888780"));
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(true);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void setupPieChart() {
        pieChart.getDescription().setEnabled(false);
        pieChart.setUsePercentValues(true);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(52f);
        pieChart.setTransparentCircleRadius(56f);
        pieChart.setTransparentCircleColor(Color.WHITE);
        pieChart.setTransparentCircleAlpha(80);
        pieChart.getLegend().setTextColor(Color.parseColor("#5F5E5A"));
        pieChart.getLegend().setTextSize(12f);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(11f);
        pieChart.setTouchEnabled(false);
        pieChart.animateY(600);
    }

    private void updatePieChart(int present, int stillIn) {
        if (present == 0 && stillIn == 0) {
            pieChart.setVisibility(View.GONE);
            return;
        }
        pieChart.setVisibility(View.VISIBLE);

        List<PieEntry> entries = new ArrayList<>();
        if (present > 0)  entries.add(new PieEntry(present, "Done"));
        if (stillIn > 0)  entries.add(new PieEntry(stillIn, "In"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#3B6D11"),
                Color.parseColor("#185FA5")
        );
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(pieChart));
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private String getGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "morning";
        if (hour < 17) return "afternoon";
        return "evening";
    }

    private String dateStr(Date d) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
    }

    private String weekStartStr() {
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        int delta = (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow;
        cal.add(Calendar.DAY_OF_MONTH, delta);
        return dateStr(cal.getTime());
    }
}