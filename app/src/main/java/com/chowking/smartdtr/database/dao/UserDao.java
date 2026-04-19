package com.chowking.smartdtr.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.chowking.smartdtr.model.User;
import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE employeeId = :employeeId LIMIT 1")
    User getUserByEmployeeId(String employeeId);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}