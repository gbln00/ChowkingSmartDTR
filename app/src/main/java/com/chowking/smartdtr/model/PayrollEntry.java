package com.chowking.smartdtr.model;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "payroll",
        indices = {@Index(value = {"employeeId","cutoffFrom","cutoffTo"}, unique = true)})
public class PayrollEntry {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String employeeId;
    public String cutoffFrom;   // "2026-03-26"
    public String cutoffTo;     // "2026-04-10"
    public long   generatedAt;  // System.currentTimeMillis()

    // ── Earnings ──────────────────────────────────────────────────────────
    public float basicPay;
    public float regularOtPay;        // OT hours × hourlyRate × 1.25
    public float nightPremiumPay;     // NP hours × hourlyRate × 0.10
    public float legalHolidayPay;     // holiday hours × hourlyRate × 2.0
    public float specialHolidayPay;   // spcl hours × hourlyRate × 1.30
    public float holidayOtPay;        // holiday OT × hourlyRate × 2.6
    public float silPay;              // 5 SIL days / year → prorated
    public float grossPay;

    // ── Deductions ────────────────────────────────────────────────────────
    public float sssPremium    = 400f;
    public float philhealth    = 250f;
    public float pagibigPremium = 200f;
    public float sssLoan       = 0f;
    public float pagibigLoan   = 0f;
    public float mealDeduction = 0f;
    public float totalDeductions;

    // ── Net ───────────────────────────────────────────────────────────────
    public float netPay;

    // ── Hours summary ─────────────────────────────────────────────────────
    public float totalHours;
    public float regularHours;
    public float otHours;
    public float nightHours;
    public float holidayHours;
    public int   daysWorked;

    // ── Status ────────────────────────────────────────────────────────────
    public String status = "DRAFT"; // "DRAFT" or "FINAL"
}
