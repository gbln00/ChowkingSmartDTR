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

    // Find open punch-in (timeOut is still 0) for today
    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "AND date = :date AND timeOut = 0 LIMIT 1")
    AttendanceRecord getOpenRecord(String id, String date);

    @Query("SELECT * FROM attendance WHERE date = :date")
    List<AttendanceRecord> getRecordsByDate(String date);

    @Query("SELECT * FROM attendance WHERE employeeId = :id " +
            "ORDER BY timeIn DESC")
    List<AttendanceRecord> getRecordsByEmployee(String id);
}