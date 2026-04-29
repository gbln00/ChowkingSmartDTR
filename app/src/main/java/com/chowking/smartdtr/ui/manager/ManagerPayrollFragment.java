package com.chowking.smartdtr.ui.manager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.adapter.PayrollAdapter;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.viewmodel.PayrollViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManagerPayrollFragment extends Fragment {

    private PayrollViewModel viewModel;
    private PayrollAdapter adapter;
    private TextInputEditText etFrom, etTo;
    private TextView tvTotalGross, tvTotalDeductions, tvTotalNet, tvStatus;
    private MaterialButton btnGenerate, btnFinalize;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_payroll, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PayrollViewModel.class);

        etFrom = view.findViewById(R.id.etFromDate);
        etTo = view.findViewById(R.id.etToDate);

        etFrom.setFocusable(false);
        etFrom.setClickable(true);
        etTo.setFocusable(false);
        etTo.setClickable(true);

        etFrom.setOnClickListener(v -> showDatePicker(etFrom));
        etTo.setOnClickListener(v -> showDatePicker(etTo));

        tvTotalGross = view.findViewById(R.id.tvTotalGross);
        tvTotalDeductions = view.findViewById(R.id.tvTotalDeductions);
        tvTotalNet = view.findViewById(R.id.tvTotalNet);
        tvStatus = view.findViewById(R.id.tvStatus);
        btnGenerate = view.findViewById(R.id.btnGenerate);
        btnFinalize = view.findViewById(R.id.btnFinalize);

        RecyclerView rv = view.findViewById(R.id.rvPayroll);
        adapter = new PayrollAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> {
            String from = etFrom.getText().toString().trim();
            String to = etTo.getText().toString().trim();
            if (from.isEmpty() || to.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter date range", Toast.LENGTH_SHORT).show();
                return;
            }

            btnGenerate.setEnabled(false);
            tvStatus.setText("Generating...");

            viewModel.generatePayroll(from, to).observe(getViewLifecycleOwner(), entries -> {
                adapter.updateEntries(entries);
                btnGenerate.setEnabled(true);

                float totalGross = 0, totalNet = 0, totalDeductions = 0;
                for (PayrollEntry e : entries) {
                    totalGross += e.grossPay;
                    totalNet += e.netPay;
                    totalDeductions += e.totalDeductions;
                }
                tvTotalGross.setText(String.format(Locale.getDefault(), "₱%,.2f", totalGross));
                tvTotalDeductions.setText(String.format(Locale.getDefault(), "₱%,.2f", totalDeductions));
                tvTotalNet.setText(String.format(Locale.getDefault(), "₱%,.2f", totalNet));
                tvStatus.setText(entries.size() + " employees · DRAFT");
            });
        });

        btnFinalize.setOnClickListener(v -> {
            String from = etFrom.getText().toString().trim();
            String to = etTo.getText().toString().trim();
            if (from.isEmpty() || to.isEmpty()) return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Finalize payroll?")
                    .setMessage("This will lock the payroll for " + from + " to " + to + ". You will not be able to regenerate it.")
                    .setPositiveButton("Finalize", (d, w) -> {
                        viewModel.finalizePayroll(from, to);
                        tvStatus.setText("FINALIZED");
                        btnFinalize.setEnabled(false);
                        btnGenerate.setEnabled(false);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            editText.setText(date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
}
