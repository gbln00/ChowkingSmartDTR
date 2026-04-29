package com.chowking.smartdtr.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.database.dao.PayrollDao;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.utils.PayrollCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PayrollRepository {

    private final UserDao userDao;
    private final AttendanceDao attendanceDao;
    private final PayrollDao payrollDao;
    private final ExecutorService executor;

    public PayrollRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        userDao       = db.userDao();
        attendanceDao = db.attendanceDao();
        payrollDao    = db.payrollDao();
        executor      = Executors.newSingleThreadExecutor();
    }

    /** Generate payroll for ALL active crew in a date range */
    public LiveData<List<PayrollEntry>> generatePayroll(String from, String to) {
        MutableLiveData<List<PayrollEntry>> result = new MutableLiveData<>();
        executor.execute(() -> {
            List<User> crew = userDao.getActiveCrewMembers();
            List<PayrollEntry> entries = new ArrayList<>();

            for (User user : crew) {
                List<AttendanceRecord> records =
                        attendanceDao.getRecordsByEmployeeAndDateRange(
                                user.employeeId, from, to);
                PayrollEntry entry =
                        PayrollCalculator.compute(user, records, from, to);
                payrollDao.insertOrReplace(entry);
                entries.add(entry);
            }
            result.postValue(entries);
        });
        return result;
    }

    /** Get payroll for a specific period (already generated) */
    public LiveData<List<PayrollEntry>> getPayrollByPeriod(String from, String to) {
        MutableLiveData<List<PayrollEntry>> result = new MutableLiveData<>();
        executor.execute(() ->
                result.postValue(payrollDao.getPayrollByPeriod(from, to))
        );
        return result;
    }

    /** Get one employee's full payslip history */
    public LiveData<List<PayrollEntry>> getPayrollHistory(String employeeId) {
        MutableLiveData<List<PayrollEntry>> result = new MutableLiveData<>();
        executor.execute(() ->
                result.postValue(payrollDao.getPayrollHistory(employeeId))
        );
        return result;
    }

    /** Finalize (lock) a payroll run for a period */
    public void finalizePayroll(String from, String to) {
        executor.execute(() -> {
            List<PayrollEntry> entries = payrollDao.getPayrollByPeriod(from, to);
            for (PayrollEntry e : entries) {
                e.status = "FINAL";
                payrollDao.updateEntry(e);
            }
        });
    }
}
