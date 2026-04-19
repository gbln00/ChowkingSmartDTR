package com.chowking.smartdtr.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.chowking.smartdtr.model.AttendanceRecord;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert
    void insertRecord(AttendanceRecord record);

    @Update
    void updateRecord(AttendanceRecord record);

    @Query("DELETE FROM attendance WHERE id = :recordId")
    void deleteRecord(int recordId);

    // ── Daily queries ──────────────────────────────────────────────────────

    /** Find an open punch-in (timeOut == 0) for today */
    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date = :date AND timeOut = 0 LIMIT 1")
    AttendanceRecord getOpenRecord(String id, String date);

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY timeIn DESC")
    List<AttendanceRecord> getRecordsByDate(String date);

    // ── Employee history ───────────────────────────────────────────────────

    @Query("SELECT * FROM attendance WHERE employeeId = :id ORDER BY timeIn DESC")
    List<AttendanceRecord> getRecordsByEmployee(String id);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date BETWEEN :fromDate AND :toDate ORDER BY date ASC")
    List<AttendanceRecord> getRecordsByEmployeeAndDateRange(
            String id, String fromDate, String toDate
    );

    // ── Date range (all employees) — used by salary report ────────────────

    @Query("SELECT * FROM attendance WHERE date BETWEEN :fromDate AND :toDate " +
            "ORDER BY employeeId ASC, date ASC")
    List<AttendanceRecord> getAllRecordsByDateRange(String fromDate, String toDate);

    // ── Salary aggregation ─────────────────────────────────────────────────

    /** Total hours worked by one employee in a date range */
    @Query("SELECT COALESCE(SUM(totalHours), 0) FROM attendance " +
            "WHERE employeeId = :employeeId AND date BETWEEN :fromDate AND :toDate " +
            "AND timeOut > 0")
    float getTotalHoursByEmployee(String employeeId, String fromDate, String toDate);

    /** Number of distinct days an employee worked in a date range */
    @Query("SELECT COUNT(DISTINCT date) FROM attendance " +
            "WHERE employeeId = :employeeId AND date BETWEEN :fromDate AND :toDate " +
            "AND timeOut > 0")
    int getDaysWorkedByEmployee(String employeeId, String fromDate, String toDate);
}