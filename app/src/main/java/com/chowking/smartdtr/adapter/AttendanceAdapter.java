package com.chowking.smartdtr.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chowking.smartdtr.R;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceAdapter extends
        RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    public interface OnRecordLongClickListener {
        void onLongClick(AttendanceRecord record);
    }

    private List<AttendanceRecord> records;
    private OnRecordLongClickListener longClickListener;

    public AttendanceAdapter(List<AttendanceRecord> records) {
        this.records = records;
    }

    /** Optional: set a long-click listener to enable edit/delete (Manager only) */
    public void setOnLongClickListener(OnRecordLongClickListener listener) {
        this.longClickListener = listener;
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
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        AttendanceRecord rec = records.get(position);
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        h.tvEmployeeId.setText(rec.employeeId);
        h.tvDate.setText(rec.date);
        h.tvTimeIn.setText(timeFmt.format(new Date(rec.timeIn)));

        if (rec.timeOut == 0) {
            h.tvTimeOut.setText("--:--");
            h.tvTotalHours.setText("Ongoing");
            h.tvTotalHours.setTextColor(h.itemView.getContext().getColor(R.color.color_still_in));
            
            h.chipStatus.setText("ACTIVE");
            h.chipStatus.setChipBackgroundColorResource(R.color.color_still_in);
        } else {
            h.tvTimeOut.setText(timeFmt.format(new Date(rec.timeOut)));
            h.tvTotalHours.setText(String.format(Locale.getDefault(), "%.2f hrs", rec.totalHours));
            h.tvTotalHours.setTextColor(h.itemView.getContext().getColor(R.color.chowking_red));

            if (rec.totalHours > 8) {
                h.chipStatus.setText("OVERTIME");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_overtime);
            } else {
                h.chipStatus.setText("COMPLETED");
                h.chipStatus.setChipBackgroundColorResource(R.color.color_present);
            }
        }

        // Add visual indicators for holidays if applicable
        if (rec.isHoliday == 1 || rec.isSpecialHoliday == 1) {
             h.tvDate.setText(rec.date + " (Holiday)");
        }

        if (longClickListener != null) {
            h.itemView.setOnLongClickListener(v -> {
                longClickListener.onLongClick(rec);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() { return records == null ? 0 : records.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmployeeId, tvDate, tvTimeIn, tvTimeOut, tvTotalHours;
        Chip     chipStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmployeeId = itemView.findViewById(R.id.tvEmployeeId);
            tvDate       = itemView.findViewById(R.id.tvDate);
            tvTimeIn     = itemView.findViewById(R.id.tvTimeIn);
            tvTimeOut    = itemView.findViewById(R.id.tvTimeOut);
            tvTotalHours = itemView.findViewById(R.id.tvTotalHours);
            chipStatus   = itemView.findViewById(R.id.chipStatus);
        }
    }
}