package com.chowking.smartdtr.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.User;

@Database(entities = {User.class, AttendanceRecord.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract AttendanceDao attendanceDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "chowking_dtr_db"
            ).build();
        }
        return instance;
    }
}