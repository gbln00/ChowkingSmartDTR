package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceRepository {

    private final AttendanceDao attendanceDao;
    private final FirebaseFirestore firestore;
    private final ExecutorService executor;

    public AttendanceRepository(Context context) {
        attendanceDao = AppDatabase.getInstance(context).attendanceDao();
        firestore = FirebaseFirestore.getInstance();
        executor = Executors.newSingleThreadExecutor();
    }

    // Returns "TIME_IN", "TIME_OUT", or "ERROR"
    public LiveData<String> recordAttendance(String employeeId) {
        MutableLiveData<String> result = new MutableLiveData<>();
        executor.execute(() -> {
            try {
                String today = new SimpleDateFormat(
                        "yyyy-MM-dd", Locale.getDefault()
                ).format(new Date());

                AttendanceRecord open =
                        attendanceDao.getOpenRecordSync(employeeId, today);

                if (open == null) {
                    // No open record — TIME IN
                    AttendanceRecord rec = new AttendanceRecord();
                    rec.employeeId = employeeId;
                    rec.date       = today;
                    rec.timeIn     = System.currentTimeMillis();
                    rec.timeOut    = 0;
                    rec.totalHours = 0;
                    
                    attendanceDao.insertRecord(rec);
                    // Sync to Cloud
                    syncRecordToCloud(rec);
                    
                    result.postValue("TIME_IN");
                } else {
                    // Open record found — TIME OUT
                    long now = System.currentTimeMillis();
                    float hours = (now - open.timeIn) / 3600000f;
                    open.timeOut    = now;
                    open.totalHours = Math.round(hours * 100) / 100f;
                    
                    attendanceDao.updateRecord(open);
                    // Sync to Cloud
                    syncRecordToCloud(open);
                    
                    result.postValue("TIME_OUT");
                }
            } catch (Exception e) {
                result.postValue("ERROR");
            }
        });
        return result;
    }

    private void syncRecordToCloud(AttendanceRecord record) {
        // Use a unique ID for Firestore: employeeId + date + timeIn
        String docId = record.employeeId + "_" + record.date + "_" + record.timeIn;
        firestore.collection("attendance")
                .document(docId)
                .set(record, SetOptions.merge())
                .addOnSuccessListener(aVoid -> android.util.Log.d("AttendanceRepo", "Sync success for: " + docId))
                .addOnFailureListener(e -> android.util.Log.e("AttendanceRepo", "Sync failed for: " + docId, e));
    }

    public LiveData<List<AttendanceRecord>> getRecordsByEmployee(String id) {
        return attendanceDao.getRecordsByEmployee(id);
    }

    public LiveData<AttendanceRecord> getOpenRecord(String id, String date) {
        return attendanceDao.getOpenRecord(id, date);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDate(String date) {
        return attendanceDao.getRecordsByDate(date);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDateRange(String fromDate, String toDate) {
        return attendanceDao.getAllRecordsByDateRange(fromDate, toDate);
    }

    public float getTotalHoursByEmployee(String id, String start, String end) {
        return attendanceDao.getTotalHoursByEmployee(id, start, end);
    }

    public int getDaysWorkedByEmployee(String id, String start, String end) {
        return attendanceDao.getDaysWorkedByEmployee(id, start, end);
    }
}