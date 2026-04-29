package com.chowking.smartdtr.model;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * USER ENTITY — DB version 2
 * Added fields: hourlyRate, position, isActive
 *
 * Migration SQL (add to AppDatabase):
 *   ALTER TABLE users ADD COLUMN hourlyRate REAL NOT NULL DEFAULT 76.25;
 *   ALTER TABLE users ADD COLUMN position TEXT NOT NULL DEFAULT 'Crew Member';
 *   ALTER TABLE users ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1;
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String employeeId;   // "CHW-001"
    public String fullName;
    // New in DB v4
    @Nullable
    public String googleId = null; // stores the Google account sub/ID
    public String email;
    public String role;          // "CREW", "MANAGER", "ADMIN"
    public String passwordHash;  // BCrypt — never plain text

    // ── New in v2 ──────────────────────────────────────────────────────────
    /** Hourly rate in PHP. Default = ₱76.25 (Region 10 min wage ÷ 8 hrs) */
    @ColumnInfo(defaultValue = "0.0")
    public float hourlyRate = 76.25f;

    /** Human-readable position title, e.g. "Service Crew", "Cashier" */
    public String position = "Crew Member";

    /**
     * Soft-delete flag. 1 = active, 0 = deactivated.
     * Deactivated users cannot log in but their attendance records are preserved.
     */
    @ColumnInfo(defaultValue = "0")
    public int isActive = 1;

    @ColumnInfo(defaultValue = "0.0")
    public float sssLoanMonthly    = 0f;  // monthly amortization
    @ColumnInfo(defaultValue = "0.0")
    public float pagibigLoanMonthly = 0f;
    @ColumnInfo(defaultValue = "0.0")
    public float mealDeductionRate  = 0f; // daily meal deduction amount


}