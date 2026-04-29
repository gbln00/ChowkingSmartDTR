package com.chowking.smartdtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.PayrollEntry;
import java.util.List;
import java.util.Locale;

public class PayrollAdapter extends RecyclerView.Adapter<PayrollAdapter.ViewHolder> {

    private List<PayrollEntry> entries;

    public PayrollAdapter(List<PayrollEntry> entries) {
        this.entries = entries;
    }

    public void updateEntries(List<PayrollEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payroll, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PayrollEntry entry = entries.get(position);
        holder.tvId.setText(entry.employeeId);
        holder.tvHours.setText(String.format(Locale.getDefault(), "%.1f hrs", entry.totalHours));
        holder.tvGross.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.grossPay));
        holder.tvDeductions.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.totalDeductions));
        holder.tvNet.setText(String.format(Locale.getDefault(), "₱%,.2f", entry.netPay));
    }

    @Override
    public int getItemCount() {
        return entries != null ? entries.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvId, tvHours, tvGross, tvDeductions, tvNet;
        ViewHolder(View itemView) {
            super(itemView);
            tvId = itemView.findViewById(R.id.tvEmployeeId);
            tvHours = itemView.findViewById(R.id.tvTotalHours);
            tvGross = itemView.findViewById(R.id.tvGrossPay);
            tvDeductions = itemView.findViewById(R.id.tvDeductions);
            tvNet = itemView.findViewById(R.id.tvNetPay);
        }
    }
}
