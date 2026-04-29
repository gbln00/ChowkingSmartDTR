package com.chowking.smartdtr.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.chowking.smartdtr.model.Schedule;

import java.util.List;

@Dao
public interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSchedule(Schedule schedule);

    @Update
    void updateSchedule(Schedule schedule);

    @Query("DELETE FROM schedules WHERE id = :id")
    void deleteSchedule(int id);

    @Query("SELECT * FROM schedules WHERE employeeId = :empId AND date LIKE :monthYear || '%'")
    LiveData<List<Schedule>> getMonthlySchedules(String empId, String monthYear);

    @Query("SELECT * FROM schedules WHERE employeeId = :empId AND date = :date LIMIT 1")
    Schedule getScheduleByDateSync(String empId, String date);
    
    @Query("DELETE FROM schedules")
    void deleteAll();
}
