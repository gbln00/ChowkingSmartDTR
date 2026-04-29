package com.chowking.smartdtr.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chowking.smartdtr.model.AttendanceRecord;

import java.util.List;

@Dao
public interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRecord(AttendanceRecord record);

    @Update
    void updateRecord(AttendanceRecord record);

    @Query("DELETE FROM attendance WHERE id = :recordId")
    void deleteRecord(int recordId);

    // ── Daily queries ──────────────────────────────────────────────────────

    /** Find an open punch-in (timeOut == 0) for today */
    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date = :date AND timeOut = 0 LIMIT 1")
    LiveData<AttendanceRecord> getOpenRecord(String id, String date);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date = :date AND timeOut = 0 LIMIT 1")
    AttendanceRecord getOpenRecordSync(String id, String date);

    @Query("SELECT * FROM attendance WHERE employeeId = :id AND date = :date AND timeIn = :timeIn LIMIT 1")
    AttendanceRecord getRecordSpecificSync(String id, String date, long timeIn);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date BETWEEN :fromDate AND :toDate ORDER BY date DESC, timeIn DESC")
    LiveData<List<AttendanceRecord>> getRecordsByEmployeeAndDateRange(
            String id, String fromDate, String toDate
    );

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date BETWEEN :fromDate AND :toDate ORDER BY date DESC, timeIn DESC")
    List<AttendanceRecord> getRecordsByEmployeeAndDateRangeSync(
            String id, String fromDate, String toDate
    );

    @Query("SELECT * FROM attendance WHERE date = :date " +
            "ORDER BY CASE WHEN timeOut = 0 THEN 0 ELSE 1 END ASC, timeIn DESC")
    LiveData<List<AttendanceRecord>> getRecordsByDate(String date);

    @Query("SELECT * FROM attendance WHERE date = :date " +
            "ORDER BY CASE WHEN timeOut = 0 THEN 0 ELSE 1 END ASC, timeIn DESC")
    List<AttendanceRecord> getRecordsByDateSync(String date);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "ORDER BY CASE WHEN timeOut = 0 THEN 0 ELSE 1 END ASC, date DESC, timeIn DESC")
    LiveData<List<AttendanceRecord>> getRecordsByEmployee(String id);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "ORDER BY CASE WHEN timeOut = 0 THEN 0 ELSE 1 END ASC, date DESC, timeIn DESC")
    List<AttendanceRecord> getRecordsByEmployeeSync(String id);

    // ── Date range (all employees) — used by salary report ────────────────

    @Query("SELECT * FROM attendance WHERE date BETWEEN :fromDate AND :toDate " +
            "ORDER BY date DESC, timeIn DESC")
    LiveData<List<AttendanceRecord>> getAllRecordsByDateRange(String fromDate, String toDate);

    @Query("SELECT * FROM attendance WHERE date BETWEEN :fromDate AND :toDate " +
            "ORDER BY date DESC, timeIn DESC")
    List<AttendanceRecord> getAllRecordsByDateRangeSync(String fromDate, String toDate);

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

    @Query("DELETE FROM attendance")
    void deleteAll();
}