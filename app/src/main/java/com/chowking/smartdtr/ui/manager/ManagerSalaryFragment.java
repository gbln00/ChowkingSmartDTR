package com.chowking.smartdtr.ui.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.SalaryAdapter;
import com.chowking.smartdtr.model.SalaryEntry;
import com.chowking.smartdtr.viewmodel.SalaryViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManagerSalaryFragment extends Fragment {

    private SalaryViewModel viewModel;
    private SalaryAdapter adapter;
    private TextInputEditText etFrom, etTo;
    private TextView tvEmployees, tvHours, tvPayroll, tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_salary_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel   = new ViewModelProvider(requireActivity()).get(SalaryViewModel.class);
        tvEmployees = view.findViewById(R.id.tvTotalEmployees);
        tvHours     = view.findViewById(R.id.tvTotalHours);
        tvPayroll   = view.findViewById(R.id.tvTotalPayroll);
        tvEmpty     = view.findViewById(R.id.tvEmptyState);
        etFrom      = view.findViewById(R.id.etFromDate);
        etTo        = view.findViewById(R.id.etToDate);

        etFrom.setFocusable(false);
        etFrom.setClickable(true);
        etTo.setFocusable(false);
        etTo.setClickable(true);

        etFrom.setOnClickListener(v -> showDatePicker(etFrom));
        etTo.setOnClickListener(v -> showDatePicker(etTo));

        RecyclerView rv = view.findViewById(R.id.rvSalary);
        adapter = new SalaryAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        setDefaultRange();

        MaterialButton btnFilter = view.findViewById(R.id.btnFilter);
        btnFilter.setOnClickListener(v -> loadReport());
        loadReport();

        // Hide the toolbar back button — we're already in a fragment
        view.findViewById(R.id.toolbar).setVisibility(View.GONE);
    }

    private void loadReport() {
        String from = etFrom.getText() != null ? etFrom.getText().toString().trim() : "";
        String to   = etTo.getText()   != null ? etTo.getText().toString().trim()   : "";
        if (from.isEmpty() || to.isEmpty()) return;

        viewModel.getSalaryReport(from, to).observe(getViewLifecycleOwner(), entries -> {
            adapter.updateEntries(entries);
            float hours = 0, pay = 0;
            for (SalaryEntry e : entries) { hours += e.totalHours; pay += e.grossPay; }
            tvEmployees.setText(String.valueOf(entries.size()));
            tvHours.setText(String.format(Locale.getDefault(), "%.1f hrs", hours));
            tvPayroll.setText(String.format(Locale.getDefault(), "₱%,.2f", pay));
            tvEmpty.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private void setDefaultRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_MONTH, (dow == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - dow);
        etFrom.setText(sdf.format(cal.getTime()));
        etTo.setText(sdf.format(Calendar.getInstance().getTime()));
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editText.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}