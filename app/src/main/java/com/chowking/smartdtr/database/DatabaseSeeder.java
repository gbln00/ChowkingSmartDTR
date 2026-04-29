package com.chowking.smartdtr.database;

import android.content.Context;
import android.os.AsyncTask;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.utils.HashUtils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatabaseSeeder {

    public static void seed(Context context) {
        new SeedAsyncTask(AppDatabase.getInstance(context)).execute();
    }

    private static class SeedAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDatabase db;

        SeedAsyncTask(AppDatabase db) {
            this.db = db;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // 1. Seed Users with varied rates and deductions for a realistic Payroll Report
            seedUser("CHW-001", "Admin User", "ADMIN", "admin123", 0, "System Admin", 0, 0, 0);
            seedUser("CHW-002", "Rosa Reyes", "MANAGER", "manager123", 120.00f, "Branch Manager", 500, 200, 50);
            seedUser("CHW-003", "Juan Dela Cruz", "CREW", "crew123", 76.25f, "Service Crew", 200, 100, 35);
            seedUser("CHW-004", "Maria Santos", "CREW", "crew123", 80.00f, "Cashier", 0, 0, 35);
            seedUser("CHW-005", "Pedro Penduko", "CREW", "crew123", 76.25f, "Kitchen Staff", 150, 0, 35);

            // 2. Seed Attendance Records for the past 14 days (Full Cutoff Period)
            for (int i = 0; i >= -14; i--) {
                seedAttendance("CHW-003", i);
                seedAttendance("CHW-004", i);
                seedAttendance("CHW-005", i);
            }

            return null;
        }

        private void seedUser(String empId, String name, String role, String password, float rate, String pos, float sssL, float pagL, float mealD) {
            if (db.userDao().getUserByEmployeeId(empId) == null) {
                User user = new User();
                user.employeeId = empId;
                user.fullName = name;
                user.role = role;
                user.passwordHash = HashUtils.hashPassword(password);
                user.isActive = 1;
                user.hourlyRate = rate;
                user.position = pos;
                user.sssLoanMonthly = sssL;
                user.pagibigLoanMonthly = pagL;
                user.mealDeductionRate = mealD;
                db.userDao().insertUser(user);
            }
        }

        private void seedAttendance(String empId, int daysOffset) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, daysOffset);
            
            // Skip Sundays for a realistic 6-day work week
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) return;

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFmt.format(cal.getTime());

            if (db.attendanceDao().getRecordsByEmployeeAndDateRangeSync(empId, dateStr, dateStr).isEmpty()) {
                AttendanceRecord record = new AttendanceRecord();
                record.employeeId = empId;
                record.date = dateStr;
                
                int startHour = 8;
                int duration = 9; // 8 hours work + 1 hour break

                // Add variety: Overtime on Wednesdays
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
                    duration = 11; // 2 hours OT
                }

                // Add variety: Night Shift on Fridays
                if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
                    startHour = 15; // 3 PM to 12 AM
                    record.isNightShift = 1;
                }

                // Add variety: Mark the 10th day back as a Holiday
                if (daysOffset == -10) {
                    record.isHoliday = 1;
                }

                cal.set(Calendar.HOUR_OF_DAY, startHour);
                cal.set(Calendar.MINUTE, 0);
                record.timeIn = cal.getTimeInMillis();
                
                cal.set(Calendar.HOUR_OF_DAY, startHour + duration);
                record.timeOut = cal.getTimeInMillis();
                
                record.totalHours = duration - 1; // Subtract 1 hour for lunch
                
                db.attendanceDao().insertRecord(record);
            }
        }
    }
}
