package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.model.AttendanceRecord;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AttendanceRepository {

    private final AttendanceDao attendanceDao;
    private final ExecutorService executor;

    public AttendanceRepository(Context context) {
        attendanceDao = AppDatabase.getInstance(context).attendanceDao();
        executor      = Executors.newSingleThreadExecutor();
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
                        attendanceDao.getOpenRecord(employeeId, today);

                if (open == null) {
                    // No open record — TIME IN
                    AttendanceRecord rec = new AttendanceRecord();
                    rec.employeeId = employeeId;
                    rec.date       = today;
                    rec.timeIn     = System.currentTimeMillis();
                    rec.timeOut    = 0;
                    rec.totalHours = 0;
                    attendanceDao.insertRecord(rec);
                    result.postValue("TIME_IN");
                } else {
                    // Open record found — TIME OUT
                    long now = System.currentTimeMillis();
                    float hours = (now - open.timeIn) / 3600000f;
                    open.timeOut    = now;
                    open.totalHours = Math.round(hours * 100) / 100f;
                    attendanceDao.updateRecord(open);
                    result.postValue("TIME_OUT");
                }
            } catch (Exception e) {
                result.postValue("ERROR");
            }
        });
        return result;
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDate(String date) {
        MutableLiveData<List<AttendanceRecord>> result = new MutableLiveData<>();
        executor.execute(() ->
                result.postValue(attendanceDao.getRecordsByDate(date))
        );
        return result;
    }
}