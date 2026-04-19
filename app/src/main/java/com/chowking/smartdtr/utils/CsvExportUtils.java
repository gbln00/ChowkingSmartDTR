package com.chowking.smartdtr.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import androidx.core.content.FileProvider;

import com.chowking.smartdtr.model.AttendanceRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CsvExportUtils {

    /**
     * Writes a list of attendance records to a CSV file in the app's
     * external cache directory (no WRITE_EXTERNAL_STORAGE permission needed
     * on API 29+ when using getExternalCacheDir).
     *
     * Returns an Intent ready to fire with startActivity() that opens
     * the system share sheet so the manager can send it via email, Drive, etc.
     *
     * Usage:
     *   Intent shareIntent = CsvExportUtils.exportAndShare(context, records, "2026-04-01");
     *   if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, "Share report"));
     */
    public static Intent exportAndShare(
            Context context,
            List<AttendanceRecord> records,
            String dateLabel
    ) {
        try {
            File cacheDir = context.getExternalCacheDir();
            if (cacheDir == null) cacheDir = context.getCacheDir();

            String fileName = "attendance_" + dateLabel.replace("-", "") + ".csv";
            File   csvFile  = new File(cacheDir, fileName);

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

            FileWriter writer = new FileWriter(csvFile);

            // CSV header
            writer.append("Date,Employee ID,Time In,Time Out,Total Hours\n");

            for (AttendanceRecord r : records) {
                writer.append(escape(r.date)).append(",");
                writer.append(escape(r.employeeId)).append(",");
                writer.append(escape(timeFmt.format(new Date(r.timeIn)))).append(",");

                if (r.timeOut > 0) {
                    writer.append(escape(timeFmt.format(new Date(r.timeOut)))).append(",");
                    writer.append(String.format(Locale.getDefault(), "%.2f", r.totalHours));
                } else {
                    writer.append("Still In,");
                    writer.append("0.00");
                }
                writer.append("\n");
            }

            writer.flush();
            writer.close();

            // Use FileProvider to get a content URI (required on API 24+)
            Uri contentUri = FileProvider.getUriForFile(
                    context,
                    context.getPackageName() + ".fileprovider",
                    csvFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Attendance Report — " + dateLabel);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return shareIntent;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Wrap a CSV field in quotes and escape any embedded quotes */
    private static String escape(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}