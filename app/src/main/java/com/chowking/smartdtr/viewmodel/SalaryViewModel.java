package com.chowking.smartdtr.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.chowking.smartdtr.model.SalaryEntry;
import com.chowking.smartdtr.repository.SalaryRepository;

import java.util.List;

public class SalaryViewModel extends AndroidViewModel {

    private final SalaryRepository repository;

    public SalaryViewModel(@NonNull Application app) {
        super(app);
        repository = new SalaryRepository(app);
    }

    public LiveData<List<SalaryEntry>> getSalaryReport(String fromDate, String toDate) {
        return repository.getSalaryReport(fromDate, toDate);
    }
}