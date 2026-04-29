package com.chowking.smartdtr.utils;

import android.content.Intent;
import com.chowking.smartdtr.model.PayrollEntry;

public class PayslipPdfUtils {

    /** Generate a plain-text payslip for sharing via any app */
    public static String generateText(PayrollEntry e, String employeeName) {
        return "CHOWKING SMART DTR — PAYSLIP\n" +
                "Employee : " + employeeName + "\n" +
                "Period   : " + e.cutoffFrom + " to " + e.cutoffTo + "\n" +
                "─────────────────────────────\n" +
                "EARNINGS\n" +
                String.format("Basic Pay        : ₱%,.2f%n", e.basicPay) +
                String.format("Overtime         : ₱%,.2f%n", e.regularOtPay) +
                String.format("Night Premium    : ₱%,.2f%n", e.nightPremiumPay) +
                String.format("Legal Holiday    : ₱%,.2f%n", e.legalHolidayPay) +
                String.format("Special Holiday  : ₱%,.2f%n", e.specialHolidayPay) +
                String.format("Holiday OT       : ₱%,.2f%n", e.holidayOtPay) +
                String.format("SIL              : ₱%,.2f%n", e.silPay) +
                String.format("GROSS PAY        : ₱%,.2f%n", e.grossPay) +
                "─────────────────────────────\n" +
                "DEDUCTIONS\n" +
                String.format("SSS Premium      : ₱%,.2f%n", e.sssPremium) +
                String.format("PhilHealth       : ₱%,.2f%n", e.philhealth) +
                String.format("Pag-IBIG         : ₱%,.2f%n", e.pagibigPremium) +
                String.format("SSS Loan         : ₱%,.2f%n", e.sssLoan) +
                String.format("Pag-IBIG Loan    : ₱%,.2f%n", e.pagibigLoan) +
                String.format("Meal Deduction   : ₱%,.2f%n", e.mealDeduction) +
                String.format("TOTAL DEDUCTIONS : ₱%,.2f%n", e.totalDeductions) +
                "─────────────────────────────\n" +
                String.format("NET PAY          : ₱%,.2f%n", e.netPay) +
                "Status: " + e.status + "\n";
    }

    public static Intent createShareIntent(String payslipText, String period) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, payslipText);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Payslip — " + period);
        return intent;
    }
}
