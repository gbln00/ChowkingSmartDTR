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
        paint.setColor(Color.RED);
        paint.setTextSize(24f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("CHOWKING SMART DTR", 50, 50, paint);
        
        paint.setColor(Color.BLACK);
        paint.setTextSize(18f);
        canvas.drawText("PAYSLIP", 50, 80, paint);
        
        paint.setTextSize(12f);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("Employee: " + employeeName, 50, 110, paint);
        canvas.drawText("Period: " + e.cutoffFrom + " to " + e.cutoffTo, 50, 130, paint);
        
        canvas.drawLine(50, 150, 545, 150, paint);
        
        // Earnings
        int y = 180;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("EARNINGS", 50, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        
        y += 25; canvas.drawText("Basic Pay", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.basicPay), 400, y, paint);
        y += 20; canvas.drawText("Overtime", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.regularOtPay), 400, y, paint);
        y += 20; canvas.drawText("Night Premium", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.nightPremiumPay), 400, y, paint);
        y += 20; canvas.drawText("Holiday Pay", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.legalHolidayPay + e.specialHolidayPay), 400, y, paint);
        y += 25; paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("GROSS PAY", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.grossPay), 400, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        
        y += 40;
        canvas.drawLine(50, y, 545, y, paint);
        
        // Deductions
        y += 30;
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DEDUCTIONS", 50, y, paint);
        paint.setTypeface(Typeface.DEFAULT);
        
        y += 25; canvas.drawText("SSS Premium", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.sssPremium), 400, y, paint);
        y += 20; canvas.drawText("PhilHealth", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.philhealth), 400, y, paint);
        y += 20; canvas.drawText("Pag-IBIG", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.pagibigPremium), 400, y, paint);
        y += 20; canvas.drawText("Loans", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.sssLoan + e.pagibigLoan), 400, y, paint);
        y += 25; paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("TOTAL DEDUCTIONS", 50, y, paint); canvas.drawText(String.format("₱%,.2f", e.totalDeductions), 400, y, paint);
        
        y += 40;
        canvas.drawLine(50, y, 545, y, paint);
        
        y += 40;
        paint.setTextSize(20f);
        canvas.drawText("NET PAY", 50, y, paint);
        canvas.drawText(String.format("₱%,.2f", e.netPay), 400, y, paint);

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