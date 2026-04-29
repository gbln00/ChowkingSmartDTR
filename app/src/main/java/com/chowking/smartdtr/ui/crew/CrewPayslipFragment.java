package com.chowking.smartdtr.ui.crew;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.utils.PayslipPdfUtils;
import com.chowking.smartdtr.utils.SessionManager;
import com.chowking.smartdtr.viewmodel.PayrollViewModel;
import com.chowking.smartdtr.utils.pdf.PdfGenerator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CrewPayslipFragment extends Fragment {

    private PayrollViewModel viewModel;
    private SessionManager session;
    private PayslipHistoryAdapter adapter;

    // Summary header views
    private TextView tvYtdGross, tvYtdNet, tvYtdDeductions;
    private TextView tvEmptyState;
    private View cardYtdSummary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crew_payslip, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session   = new SessionManager(requireContext());
        viewModel = new ViewModelProvider(requireActivity()).get(PayrollViewModel.class);

        // Header summary views
        tvYtdGross      = view.findViewById(R.id.tvYtdGross);
        tvYtdNet        = view.findViewById(R.id.tvYtdNet);
        tvYtdDeductions = view.findViewById(R.id.tvYtdDeductions);
        tvEmptyState    = view.findViewById(R.id.tvEmptyState);
        cardYtdSummary  = view.findViewById(R.id.cardYtdSummary);

        // RecyclerView
        RecyclerView rv = view.findViewById(R.id.rvPayslips);
        adapter = new PayslipHistoryAdapter(new ArrayList<>(), this::showPayslipDetail);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(adapter);

        loadPayslips();
    }

    private void loadPayslips() {
        String employeeId = session.getEmployeeId();
        viewModel.getPayrollHistory(employeeId).observe(getViewLifecycleOwner(), entries -> {
            if (entries == null || entries.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                cardYtdSummary.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                cardYtdSummary.setVisibility(View.VISIBLE);
                adapter.updateEntries(entries);
                updateYtdSummary(entries);
            }
        });
    }

    private void updateYtdSummary(List<PayrollEntry> entries) {
        float ytdGross = 0, ytdNet = 0, ytdDeductions = 0;
        for (PayrollEntry e : entries) {
            ytdGross      += e.grossPay;
            ytdNet        += e.netPay;
            ytdDeductions += e.totalDeductions;
        }
        tvYtdGross.setText(formatPeso(ytdGross));
        tvYtdNet.setText(formatPeso(ytdNet));
        tvYtdDeductions.setText(formatPeso(ytdDeductions));
    }

    // ── Full payslip detail dialog ─────────────────────────────────────────

    private void showPayslipDetail(PayrollEntry entry) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_payslip_detail, null);

        // Period header
        bindText(dialogView, R.id.tvPayslipPeriod,
                entry.cutoffFrom + "  →  " + entry.cutoffTo);
        bindText(dialogView, R.id.tvPayslipEmployee, session.getFullName());
        bindText(dialogView, R.id.tvPayslipId, session.getEmployeeId());

        // Status chip
        Chip chipStatus = dialogView.findViewById(R.id.chipPayslipStatus);
        if ("FINAL".equals(entry.status)) {
            chipStatus.setText("FINALIZED");
            chipStatus.setChipBackgroundColorResource(R.color.color_present);
            chipStatus.setTextColor(0xFFFFFFFF);
        } else {
            chipStatus.setText("DRAFT");
            chipStatus.setChipBackgroundColorResource(R.color.color_still_in);
            chipStatus.setTextColor(0xFFFFFFFF);
        }

        // Hours summary
        bindText(dialogView, R.id.tvDaysWorked,
                entry.daysWorked + (entry.daysWorked == 1 ? " day" : " days"));
        bindText(dialogView, R.id.tvRegularHrs,
                String.format(Locale.getDefault(), "%.2f hrs", entry.regularHours));
        bindText(dialogView, R.id.tvOtHrs,
                String.format(Locale.getDefault(), "%.2f hrs", entry.otHours));
        bindText(dialogView, R.id.tvNightHrs,
                String.format(Locale.getDefault(), "%.2f hrs", entry.nightHours));

        // Earnings
        bindText(dialogView, R.id.tvBasicPay,       formatPeso(entry.basicPay));
        bindText(dialogView, R.id.tvOvertimePay,    formatPeso(entry.regularOtPay));
        bindText(dialogView, R.id.tvNightPremium,   formatPeso(entry.nightPremiumPay));
        bindText(dialogView, R.id.tvHolidayPay,     formatPeso(entry.legalHolidayPay));
        bindText(dialogView, R.id.tvSplHolidayPay,  formatPeso(entry.specialHolidayPay));
        bindText(dialogView, R.id.tvHolidayOtPay,   formatPeso(entry.holidayOtPay));
        bindText(dialogView, R.id.tvSilPay,         formatPeso(entry.silPay));
        bindText(dialogView, R.id.tvGrossPay,       formatPeso(entry.grossPay));

        // Deductions
        bindText(dialogView, R.id.tvSssPremium,     formatPeso(entry.sssPremium));
        bindText(dialogView, R.id.tvPhilhealth,     formatPeso(entry.philhealth));
        bindText(dialogView, R.id.tvPagibig,        formatPeso(entry.pagibigPremium));
        bindText(dialogView, R.id.tvSssLoan,        formatPeso(entry.sssLoan));
        bindText(dialogView, R.id.tvPagibigLoan,    formatPeso(entry.pagibigLoan));
        bindText(dialogView, R.id.tvMealDeduction,  formatPeso(entry.mealDeduction));
        bindText(dialogView, R.id.tvTotalDeductions,formatPeso(entry.totalDeductions));

        // Net Pay
        bindText(dialogView, R.id.tvNetPayAmount,   formatPeso(entry.netPay));

        // Share button inside dialog
        MaterialButton btnShare = dialogView.findViewById(R.id.btnSharePayslip);
        btnShare.setOnClickListener(v -> sharePayslip(entry));

        MaterialButton btnDownload = dialogView.findViewById(R.id.btnDownloadPdf);
        btnDownload.setOnClickListener(v -> downloadPdf(entry));

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void downloadPdf(PayrollEntry entry) {
        File file = PdfGenerator.generatePayslipPdf(requireContext(), entry, session.getFullName());
        if (file != null) {
            Toast.makeText(requireContext(), "PDF saved to Downloads: " + file.getName(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(requireContext(), "Failed to generate PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePayslip(PayrollEntry entry) {
        String text = PayslipPdfUtils.generateText(entry, session.getFullName());
        Intent intent = PayslipPdfUtils.createShareIntent(
                text, entry.cutoffFrom + " to " + entry.cutoffTo);
        startActivity(Intent.createChooser(intent, "Share payslip"));
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void bindText(View root, int viewId, String text) {
        TextView tv = root.findViewById(viewId);
        if (tv != null) tv.setText(text);
    }

    private String formatPeso(float amount) {
        if (amount == 0f) return "—";
        return String.format(Locale.getDefault(), "₱%,.2f", amount);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Inner RecyclerView adapter — payslip history list cards
    // ══════════════════════════════════════════════════════════════════════

    public static class PayslipHistoryAdapter
            extends RecyclerView.Adapter<PayslipHistoryAdapter.ViewHolder> {

        public interface OnPayslipClickListener {
            void onClick(PayrollEntry entry);
        }

        private List<PayrollEntry> entries;
        private final OnPayslipClickListener clickListener;

        public PayslipHistoryAdapter(List<PayrollEntry> entries,
                                     OnPayslipClickListener clickListener) {
            this.entries       = entries;
            this.clickListener = clickListener;
        }

        public void updateEntries(List<PayrollEntry> newEntries) {
            this.entries = newEntries;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_payslip_history, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            PayrollEntry e = entries.get(position);

            // Period label e.g. "Mar 26 – Apr 10"
            h.tvPeriod.setText(formatShortPeriod(e.cutoffFrom, e.cutoffTo));
            h.tvCutoffDates.setText(e.cutoffFrom + " → " + e.cutoffTo);

            // Net pay — the big number
            h.tvNetPay.setText(
                    String.format(Locale.getDefault(), "₱%,.2f", e.netPay));

            // Gross and deductions subline
            h.tvGrossLine.setText(
                    String.format(Locale.getDefault(),
                            "Gross ₱%,.2f  ·  Deductions ₱%,.2f",
                            e.grossPay, e.totalDeductions));

            // Days & hours summary
            h.tvHoursSummary.setText(
                    String.format(Locale.getDefault(),
                            "%d days  ·  %.1f hrs total",
                            e.daysWorked, e.totalHours));

            // Status chip
            if ("FINAL".equals(e.status)) {
                h.chipStatus.setText("Final");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_present);
                h.chipStatus.setTextColor(0xFFFFFFFF);
            } else {
                h.chipStatus.setText("Draft");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_still_in);
                h.chipStatus.setTextColor(0xFFFFFFFF);
            }

            h.itemView.setOnClickListener(v -> clickListener.onClick(e));
        }

        @Override
        public int getItemCount() {
            return entries == null ? 0 : entries.size();
        }

        /** "2026-03-26" → "Mar 26" */
        private String formatShortPeriod(String from, String to) {
            try {
                String[] fp = from.split("-");
                String[] tp = to.split("-");
                String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                        "Jul","Aug","Sep","Oct","Nov","Dec"};
                int fm = Integer.parseInt(fp[1]) - 1;
                int tm = Integer.parseInt(tp[1]) - 1;
                String fromLabel = months[fm] + " " + fp[2];
                String toLabel   = months[tm] + " " + tp[2];
                return fromLabel + " – " + toLabel;
            } catch (Exception e) {
                return from + " – " + to;
            }
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvPeriod, tvCutoffDates, tvNetPay, tvGrossLine, tvHoursSummary;
            Chip     chipStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvPeriod       = itemView.findViewById(R.id.tvPayslipPeriodLabel);
                tvCutoffDates  = itemView.findViewById(R.id.tvPayslipCutoffDates);
                tvNetPay       = itemView.findViewById(R.id.tvPayslipNetPay);
                tvGrossLine    = itemView.findViewById(R.id.tvPayslipGrossLine);
                tvHoursSummary = itemView.findViewById(R.id.tvPayslipHoursSummary);
                chipStatus     = itemView.findViewById(R.id.chipPayslipHistoryStatus);
            }
        }
    }
}
