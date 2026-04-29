package com.chowking.smartdtr.utils;

import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.model.User;

import java.util.List;

public class PayrollCalculator {

    // ── Philippine labor law rates ─────────────────────────────────────────
    public static final float OT_MULTIPLIER            = 1.25f;
    public static final float NIGHT_DIFF_RATE          = 0.10f; // +10% of hourly
    public static final float LEGAL_HOLIDAY_RATE       = 2.00f; // 200% of daily
    public static final float SPECIAL_HOLIDAY_RATE     = 1.30f; // 130% of daily
    public static final float HOLIDAY_OT_MULTIPLIER    = 2.60f; // 260% on holiday OT

    // ── Government-mandated deductions (2024 brackets) ─────────────────────
    public static final float SSS_PREMIUM              = 400f;  // approx mid-range
    public static final float PHILHEALTH_PREMIUM       = 250f;
    public static final float PAGIBIG_PREMIUM          = 200f;

    /**
     * Compute a full PayrollEntry from raw attendance data for one employee.
     */
    public static PayrollEntry compute(
            User user,
            List<AttendanceRecord> records,
            String cutoffFrom,
            String cutoffTo) {

        PayrollEntry entry = new PayrollEntry();
        entry.employeeId = user.employeeId;
        entry.cutoffFrom = cutoffFrom;
        entry.cutoffTo   = cutoffTo;
        entry.generatedAt = System.currentTimeMillis();
        entry.status = "DRAFT";

        float hourlyRate = user.hourlyRate;
        float dailyRate  = hourlyRate * 8f;

        // Tally hours by type
        float regularHours = 0, otHours = 0;
        float nightHours = 0, holidayHours = 0;
        float specialHolidayHours = 0, holidayOtHours = 0;
        int daysWorked = 0;

        for (AttendanceRecord r : records) {
            if (r.timeOut == 0) continue; // skip open shifts
            daysWorked++;
            float total = r.totalHours;
            float reg   = Math.min(total, 8f);
            float ot    = Math.max(0, total - 8f);

            if (r.isHoliday == 1) {
                holidayHours += reg;
                holidayOtHours += ot;
            } else if (r.isSpecialHoliday == 1) {
                specialHolidayHours += reg;
                otHours += ot; // special holiday OT at regular OT rate
            } else {
                regularHours += reg;
                otHours += ot;
            }

            if (r.isNightShift == 1) {
                nightHours += total; // approximate — all hours get NP
            }
        }

        // ── Compute earnings ──────────────────────────────────────────────
        entry.daysWorked       = daysWorked;
        entry.regularHours     = regularHours;
        entry.otHours          = otHours;
        entry.nightHours       = nightHours;
        entry.holidayHours     = holidayHours;
        entry.totalHours       = regularHours + otHours + holidayHours
                + specialHolidayHours + holidayOtHours;

        entry.basicPay          = regularHours * hourlyRate;
        entry.regularOtPay      = otHours * hourlyRate * OT_MULTIPLIER;
        entry.nightPremiumPay   = nightHours * hourlyRate * NIGHT_DIFF_RATE;
        entry.legalHolidayPay   = holidayHours * hourlyRate * LEGAL_HOLIDAY_RATE;
        entry.specialHolidayPay = specialHolidayHours * hourlyRate * SPECIAL_HOLIDAY_RATE;
        entry.holidayOtPay      = holidayOtHours * hourlyRate * HOLIDAY_OT_MULTIPLIER;

        // SIL: 5 days per year → prorated per cutoff period (26 cutoffs/year)
        entry.silPay = (daysWorked > 0) ? (dailyRate * 5f / 26f) : 0f;

        entry.grossPay = entry.basicPay + entry.regularOtPay
                + entry.nightPremiumPay + entry.legalHolidayPay
                + entry.specialHolidayPay + entry.holidayOtPay
                + entry.silPay;

        // ── Compute deductions ────────────────────────────────────────────
        // Only apply mandatory deductions if employee worked this period
        if (daysWorked > 0) {
            entry.sssPremium     = SSS_PREMIUM;
            entry.philhealth     = PHILHEALTH_PREMIUM;
            entry.pagibigPremium = PAGIBIG_PREMIUM;
        }
        entry.sssLoan       = user.sssLoanMonthly / 2f; // per cutoff (semi-monthly)
        entry.pagibigLoan   = user.pagibigLoanMonthly / 2f;
        entry.mealDeduction = user.mealDeductionRate * daysWorked;

        entry.totalDeductions = entry.sssPremium + entry.philhealth
                + entry.pagibigPremium + entry.sssLoan
                + entry.pagibigLoan + entry.mealDeduction;

        entry.netPay = Math.max(0, entry.grossPay - entry.totalDeductions);

        return entry;
    }

    /** Round to 2 decimal places */
    public static float round2(float val) {
        return Math.round(val * 100f) / 100f;
    }
}
