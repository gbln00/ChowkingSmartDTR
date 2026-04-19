package com.chowking.smartdtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.AttendanceRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends
        RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<AttendanceRecord> records;

    public AttendanceAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    public void updateRecords(List<AttendanceRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AttendanceRecord rec = records.get(position);
        SimpleDateFormat fmt =
                new SimpleDateFormat("hh:mm a", Locale.getDefault());

        holder.tvEmployeeId.setText(rec.employeeId);
        holder.tvTimeIn.setText("Time In:  " + fmt.format(new Date(rec.timeIn)));

        if (rec.timeOut == 0) {
            holder.tvTimeOut.setText("Time Out: --");
            holder.tvTotalHours.setText("Still clocked in");
            holder.tvStatusBadge.setText("IN");
            holder.tvStatusBadge.setBackgroundColor(0xFF1565C0);
        } else {
            holder.tvTimeOut.setText("Time Out: " + fmt.format(new Date(rec.timeOut)));
            holder.tvTotalHours.setText(
                    String.format(Locale.getDefault(), "Total: %.2f hrs", rec.totalHours)
            );
            holder.tvStatusBadge.setText("DONE");
            holder.tvStatusBadge.setBackgroundColor(0xFF2E7D32);
        }
    }

    @Override
    public int getItemCount() { return records == null ? 0 : records.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeId, tvTimeIn, tvTimeOut, tvTotalHours, tvStatusBadge;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeId   = itemView.findViewById(R.id.tvEmployeeId);
            tvTimeIn       = itemView.findViewById(R.id.tvTimeIn);
            tvTimeOut      = itemView.findViewById(R.id.tvTimeOut);
            tvTotalHours   = itemView.findViewById(R.id.tvTotalHours);
            tvStatusBadge  = itemView.findViewById(R.id.tvStatusBadge);
        }
    }
}