package com.chowking.smartdtr.utils.pdf;

import android.content.Context;
import android.os.Environment;

import com.chowking.smartdtr.model.AttendanceRecord;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceReportPdfGenerator {

    public static File generateAttendancePdf(Context context, List<AttendanceRecord> records, String period) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Attendance_Report_" + System.currentTimeMillis() + ".pdf";
            File file = new File(downloadsDir, fileName);

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            Paragraph title = new Paragraph("CHOWKING SMART DTR")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(new DeviceRgb(238, 45, 36))
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            document.add(new Paragraph("Attendance Report: " + period)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Table
            float[] columnWidths = {2, 3, 2, 2, 1};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Employee ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("In").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Out").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Hrs").setBold()));

            SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

            for (AttendanceRecord r : records) {
                table.addCell(new Cell().add(new Paragraph(r.date)));
                table.addCell(new Cell().add(new Paragraph(r.employeeId)));
                table.addCell(new Cell().add(new Paragraph(timeFmt.format(new Date(r.timeIn)))));
                table.addCell(new Cell().add(new Paragraph(r.timeOut > 0 ? timeFmt.format(new Date(r.timeOut)) : "---")));
                table.addCell(new Cell().add(new Paragraph(String.format("%.1f", r.totalHours))));
            }

            document.add(table);
            document.close();
            return file;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
