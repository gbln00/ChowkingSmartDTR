package com.chowking.smartdtr.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedules")
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    @NonNull
    public String employeeId;
    
    @NonNull
    public String date; // yyyy-MM-dd
    
    public String shiftStart; // e.g., "08:00"
    public String shiftEnd;   // e.g., "17:00"
    public String note;
    public int isPublished; // 0 for draft, 1 for visible to crew
}
