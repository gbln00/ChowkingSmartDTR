package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.SalaryEntry;
import com.chowking.smartdtr.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SalaryRepository {

    private final UserDao userDao;
    private final AttendanceDao attendanceDao;
    private final ExecutorService executor;

    public SalaryRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao = db.userDao();
        attendanceDao = db.attendanceDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<SalaryEntry>> getSalaryReport(String fromDate, String toDate) {
        MutableLiveData<List<SalaryEntry>> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<User> crewMembers = userDao.getActiveCrewMembers();
            List<SalaryEntry> report = new ArrayList<>();

            for (User user : crewMembers) {
                SalaryEntry entry = new SalaryEntry();
                entry.employeeId = user.employeeId;
                entry.fullName   = user.fullName;
                entry.position   = user.position;
                entry.hourlyRate = user.hourlyRate;

                entry.totalHours = attendanceDao.getTotalHoursByEmployee(user.employeeId, fromDate, toDate);
                entry.daysWorked = attendanceDao.getDaysWorkedByEmployee(user.employeeId, fromDate, toDate);

                entry.compute();
                report.add(entry);
            }
            result.postValue(report);
        });
        return result;
    }
}