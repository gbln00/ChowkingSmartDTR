package com.chowking.smartdtr.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.repository.AttendanceRepository;
import java.util.List;

public class AttendanceViewModel extends AndroidViewModel {

    private final AttendanceRepository repository;

    public AttendanceViewModel(@NonNull Application app) {
        super(app);
        repository = new AttendanceRepository(app);
    }

    public LiveData<String> recordAttendance(String employeeId) {
        return repository.recordAttendance(employeeId);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByEmployee(String id) {
        return repository.getRecordsByEmployee(id);
    }

    public LiveData<AttendanceRecord> getOpenRecord(String id, String date) {
        return repository.getOpenRecord(id, date);
    }

    public LiveData<List<AttendanceRecord>> getRecordsByDate(String date) {
        return repository.getRecordsByDate(date);
    }
}