package com.chowking.smartdtr.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.chowking.smartdtr.model.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insertUser(User user);

    @Update
    void updateUser(User user);

    @Delete
    void deleteUser(User user);

    // ── Basic lookups ──────────────────────────────────────────────────────

    @Query("SELECT * FROM users WHERE employeeId = :employeeId LIMIT 1")
    User getUserByEmployeeId(String employeeId);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getUserById(int id);

    @Query("SELECT * FROM users ORDER BY fullName ASC")
    List<User> getAllUsers();

    // ── Active users only (used in salary report and login) ───────────────

    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY fullName ASC")
    List<User> getActiveUsers();

    @Query("SELECT * FROM users WHERE employeeId = :employeeId AND isActive = 1 LIMIT 1")
    User getActiveUserByEmployeeId(String employeeId);

    // ── Soft-delete: set isActive = 0 instead of deleting ─────────────────

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    void deactivateUser(int userId);

    @Query("UPDATE users SET isActive = 1 WHERE id = :userId")
    void reactivateUser(int userId);

    // ── Password reset ─────────────────────────────────────────────────────

    @Query("UPDATE users SET passwordHash = :newHash WHERE id = :userId")
    void updatePassword(int userId, String newHash);

    // ── Salary report ──────────────────────────────────────────────────────

    @Query("SELECT * FROM users WHERE role = 'CREW' AND isActive = 1 ORDER BY fullName ASC")
    List<User> getActiveCrewMembers();

    // ── Google login ───────────────────────────────────────────────────────
    @Query("SELECT * FROM users WHERE googleId = :googleId AND isActive = 1 LIMIT 1")
    User getUserByGoogleId(String googleId);

    @Query("SELECT * FROM users WHERE email = :email AND isActive = 1 LIMIT 1")
    User getUserByEmail(String email);

    // ── Link Google account ───────────────────────────────────────────────
    @Query("UPDATE users SET googleId = :googleId WHERE id = :userId")
    void linkGoogleAccount(int userId, String googleId);
}