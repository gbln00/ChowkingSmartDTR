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
}