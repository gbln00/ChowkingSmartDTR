package com.chowking.smartdtr.utils.pdf;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import com.chowking.smartdtr.model.PayrollEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfGenerator {

    public static File generatePayslipPdf(Context context, PayrollEntry e, String employeeName) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        
        // Header
        paint.setColor(Color.parseColor("#EE2D24")); // Chowking Red
        paint.setTextSize(26f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("CHOWKING SMART DTR", 50, 60, paint);
        
        paint.setColor(Color.BLACK);
        paint.setTextSize(14f);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Employee Attendance & Payroll System", 50, 80, paint);
        
        paint.setTextSize(18f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("PAYSLIP", 50, 120, paint);
        
        paint.setTextSize(12f);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Employee Name: " + employeeName, 50, 150, paint);
        canvas.drawText("Employee ID:   " + e.employeeId, 50, 165, paint);
        canvas.drawText("Pay Period:    " + e.cutoffFrom + " to " + e.cutoffTo, 50, 180, paint);
        
        canvas.drawLine(50, 200, 545, 200, paint);
        
        // Hours Breakdown
        int y = 230;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("HOURS SUMMARY", 50, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        
        y += 20; canvas.drawText("Regular Hours:", 70, y, paint); canvas.drawText(String.format("%.2f", e.regularHours), 200, y, paint);
        y += 18; canvas.drawText("Overtime Hours:", 70, y, paint); canvas.drawText(String.format("%.2f", e.otHours), 200, y, paint);
        y += 18; canvas.drawText("Night Hours:", 70, y, paint); canvas.drawText(String.format("%.2f", e.nightHours), 200, y, paint);
        y += 18; canvas.drawText("Days Worked:", 70, y, paint); canvas.drawText(String.valueOf(e.daysWorked), 200, y, paint);

        // Earnings and Deductions Table
        y += 40;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("EARNINGS", 50, y, paint);
        canvas.drawText("DEDUCTIONS", 300, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        
        int tableTop = y + 10;
        int currentY = tableTop + 20;
        
        // Earnings items
        canvas.drawText("Basic Pay", 50, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.basicPay), 200, currentY, paint);
        currentY += 18;
        canvas.drawText("OT Pay", 50, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.regularOtPay), 200, currentY, paint);
        currentY += 18;
        canvas.drawText("Night Prem.", 50, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.nightPremiumPay), 200, currentY, paint);
        currentY += 18;
        canvas.drawText("Holiday Pay", 50, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.legalHolidayPay + e.specialHolidayPay), 200, currentY, paint);
        
        // Deductions items (reset Y for second column)
        currentY = tableTop + 20;
        canvas.drawText("SSS", 300, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.sssPremium), 450, currentY, paint);
        currentY += 18;
        canvas.drawText("PhilHealth", 300, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.philhealth), 450, currentY, paint);
        currentY += 18;
        canvas.drawText("Pag-IBIG", 300, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.pagibigPremium), 450, currentY, paint);
        currentY += 18;
        canvas.drawText("Loans/Other", 300, currentY, paint); canvas.drawText(String.format("₱%,.2f", e.sssLoan + e.pagibigLoan + e.mealDeduction), 450, currentY, paint);

        y = currentY + 40;
        canvas.drawLine(50, y, 545, y, paint);
        
        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("GROSS PAY", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.grossPay), 200, y, paint);
        
        canvas.drawText("TOTAL DEDUCT", 300, y, paint); canvas.drawText(String.format("₱%,.2f", e.totalDeductions), 450, y, paint);
        
        y += 50;
        paint.setTextSize(22f);
        paint.setColor(Color.parseColor("#2E7D32")); // Success Green
        canvas.drawText("NET TAKE HOME PAY", 50, y, paint);
        canvas.drawText(String.format("₱%,.2f", e.netPay), 380, y, paint);

        y += 60;
        paint.setColor(Color.GRAY);
        paint.setTextSize(10f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        canvas.drawText("This is a computer-generated document. No signature is required.", 50, y, paint);

        document.finishPage(page);

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = "Payslip_" + e.cutoffTo + ".pdf";
        File file = new File(downloadsDir, fileName);

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (IOException ioException) {
            ioException.printStackTrace();
            document.close();
            return null;
        }
    }
}