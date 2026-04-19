package com.chowking.smartdtr.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String employeeId;    // "CHW-001"
    public String fullName;
    public String role;          // "CREW", "MANAGER", "ADMIN"
    public String passwordHash;  // BCrypt hash — never plain text
}