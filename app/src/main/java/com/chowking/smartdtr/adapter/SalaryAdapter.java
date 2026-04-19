package com.chowking.smartdtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.SalaryEntry;

import java.util.List;
import java.util.Locale;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.ViewHolder> {

    private List<SalaryEntry> entries;

    public SalaryAdapter(List<SalaryEntry> entries) {
        this.entries = entries;
    }

    public void updateEntries(List<SalaryEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_salary, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        SalaryEntry e = entries.get(position);

        h.tvName.setText(e.fullName);
        h.tvPosition.setText(e.position + " · " + e.employeeId);
        h.tvDays.setText(e.daysWorked + (e.daysWorked == 1 ? " day" : " days"));
        h.tvRegularHours.setText(
                String.format(Locale.getDefault(), "Regular: %.2f hrs", e.regularHours)
        );

        if (e.overtimeHours > 0) {
            h.tvOvertimeHours.setVisibility(View.VISIBLE);
            h.tvOvertimeHours.setText(
                    String.format(Locale.getDefault(), "Overtime: %.2f hrs (+25%%)", e.overtimeHours)
            );
        } else {
            h.tvOvertimeHours.setVisibility(View.GONE);
        }

        h.tvRate.setText(
                String.format(Locale.getDefault(), "₱%.2f/hr", e.hourlyRate)
        );
        h.tvGrossPay.setText(
                String.format(Locale.getDefault(), "₱%.2f", e.grossPay)
        );
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPosition, tvDays, tvRegularHours,
                tvOvertimeHours, tvRate, tvGrossPay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName          = itemView.findViewById(R.id.tvSalaryName);
            tvPosition      = itemView.findViewById(R.id.tvSalaryPosition);
            tvDays          = itemView.findViewById(R.id.tvSalaryDays);
            tvRegularHours  = itemView.findViewById(R.id.tvRegularHours);
            tvOvertimeHours = itemView.findViewById(R.id.tvOvertimeHours);
            tvRate          = itemView.findViewById(R.id.tvHourlyRate);
            tvGrossPay      = itemView.findViewById(R.id.tvGrossPay);
        }
    }
}