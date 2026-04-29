package com.chowking.smartdtr.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.repository.PayrollRepository;
import java.util.List;

public class PayrollViewModel extends AndroidViewModel {

    private final PayrollRepository repository;

    public PayrollViewModel(@NonNull Application app) {
        super(app);
        repository = new PayrollRepository(app);
    }

    public LiveData<List<PayrollEntry>> generatePayroll(String from, String to) {
        return repository.generatePayroll(from, to);
    }

    public LiveData<List<PayrollEntry>> getPayrollByPeriod(String from, String to) {
        return repository.getPayrollByPeriod(from, to);
    }

    public LiveData<List<PayrollEntry>> getPayrollHistory(String employeeId) {
        return repository.getPayrollHistory(employeeId);
    }

    public void finalizePayroll(String from, String to) {
        repository.finalizePayroll(from, to);
    }
}
