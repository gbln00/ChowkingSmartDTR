package com.chowking.smartdtr.model;

/**
 * Data holder for one employee's salary computation.
 * Not a Room @Entity — computed on the fly by SalaryRepository.
 */
public class SalaryEntry {

    public String employeeId;
    public String fullName;
    public String position;
    public float  hourlyRate;

    public int   daysWorked;
    public float totalHours;
    public float regularHours;   // hours up to 8 per day × days
    public float overtimeHours;  // totalHours - regularHours (if any)
    public float grossPay;       // (regularHours × rate) + (overtimeHours × rate × 1.25)

    public SalaryEntry() {}

    /**
     * Compute derived salary fields from raw totals.
     * Overtime rate in the Philippines is 125% of the hourly rate.
     */
    public void compute() {
        float maxRegular   = daysWorked * 8f;
        regularHours       = Math.min(totalHours, maxRegular);
        overtimeHours      = Math.max(0, totalHours - maxRegular);
        float regularPay   = regularHours  * hourlyRate;
        float overtimePay  = overtimeHours * hourlyRate * 1.25f;
        grossPay           = regularPay + overtimePay;
    }
}