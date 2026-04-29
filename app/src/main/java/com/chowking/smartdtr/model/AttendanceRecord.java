package com.chowking.smartdtr.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "attendance")
public class AttendanceRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String employeeId;
    public String date;        // "2026-04-19"
    public long   timeIn;      // System.currentTimeMillis()
    public long   timeOut;     // 0 until punched out
    public float  totalHours;  // filled on punch-out
    public int isNightShift    = 0;  // 1 = any hours 10pm–6am
    public int isHoliday       = 0;  // 1 = regular holiday (200%)
    public int isSpecialHoliday = 0; // 1 = special non-working (130%)
}