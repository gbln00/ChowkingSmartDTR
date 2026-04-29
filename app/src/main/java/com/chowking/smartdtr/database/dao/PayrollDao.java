package com.chowking.smartdtr.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chowking.smartdtr.model.PayrollEntry;

import java.util.List;

@Dao
public interface PayrollDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrReplace(PayrollEntry entry);

    @Update
    void updateEntry(PayrollEntry entry);

    @Query("SELECT * FROM payroll WHERE employeeId = :id ORDER BY cutoffFrom DESC")
    List<PayrollEntry> getPayrollHistory(String id);

    @Query("SELECT * FROM payroll WHERE cutoffFrom = :from AND cutoffTo = :to " +
            "ORDER BY employeeId ASC")
    List<PayrollEntry> getPayrollByPeriod(String from, String to);

    @Query("SELECT * FROM payroll WHERE employeeId = :id AND " +
            "cutoffFrom = :from AND cutoffTo = :to LIMIT 1")
    PayrollEntry getPayrollEntry(String id, String from, String to);

    @Query("DELETE FROM payroll WHERE cutoffFrom = :from AND cutoffTo = :to")
    void deleteByPeriod(String from, String to);
}