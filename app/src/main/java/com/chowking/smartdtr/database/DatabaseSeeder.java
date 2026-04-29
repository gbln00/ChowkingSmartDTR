package com.chowking.smartdtr.database;

import android.content.Context;
import android.os.AsyncTask;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.utils.HashUtils;
import com.chowking.smartdtr.utils.PayrollCalculator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseSeeder {

    public static void seed(Context context) {
        new SeedAsyncTask(AppDatabase.getInstance(context)).execute();
    }

    private static class SeedAsyncTask extends AsyncTask<Void, Void, Void> {
        private final AppDatabase db;
        private final FirebaseFirestore firestore;

        SeedAsyncTask(AppDatabase db) {
            this.db = db;
            this.firestore = FirebaseFirestore.getInstance();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // 0. Clear old seeded attendance and payroll data
            // (We keep users but update them)
            db.attendanceDao().deleteAll();
            db.payrollDao().deleteAll();

            // 1. Seed Users
            seedUser("CHW-001", "Admin User", "ADMIN", "admin123", 0, "System Admin", 0, 0, 0);
            seedUser("CHW-002", "Rosa Reyes", "MANAGER", "manager123", 120.00f, "Branch Manager", 500, 200, 50);
            seedUser("CHW-003", "Juan Dela Cruz", "CREW", "crew123", 76.25f, "Service Crew", 200, 100, 35);
            seedUser("CHW-004", "Maria Santos", "CREW", "crew123", 80.00f, "Cashier", 0, 0, 35);
            seedUser("CHW-005", "Pedro Penduko", "CREW", "crew123", 76.25f, "Kitchen Staff", 150, 0, 35);

            // 2. Seed Attendance Records (Last 30 days to ensure at least 15 working days)
            for (int i = -1; i >= -30; i--) {
                seedAttendance("CHW-003", i);
                seedAttendance("CHW-004", i);
                seedAttendance("CHW-005", i);
            }

            // 3. Seed Future Schedules (Next 7 days)
            for (int i = 1; i <= 7; i++) {
                seedSchedule("CHW-003", i);
                seedSchedule("CHW-004", i);
                seedSchedule("CHW-005", i);
            }

            // 4. Seed Payroll History (Current and Previous Period)
            seedPayrollHistory("CHW-003");
            seedPayrollHistory("CHW-004");
            seedPayrollHistory("CHW-005");

            return null;
        }

        private void seedPayrollHistory(String empId) {
            User user = db.userDao().getUserByEmployeeId(empId);
            if (user == null) return;

            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            // 1st Period (1st to 15th of current month)
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String from1 = sdf.format(cal.getTime());
            cal.set(Calendar.DAY_OF_MONTH, 15);
            String to1 = sdf.format(cal.getTime());

            generateAndSavePayroll(user, from1, to1);

            // 2nd Period (16th to end of previous month)
            cal.add(Calendar.MONTH, -1);
            cal.set(Calendar.DAY_OF_MONTH, 16);
            String from2 = sdf.format(cal.getTime());
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String to2 = sdf.format(cal.getTime());

            generateAndSavePayroll(user, from2, to2);
        }

        private void generateAndSavePayroll(User user, String from, String to) {
            List<AttendanceRecord> records = db.attendanceDao().getRecordsByEmployeeAndDateRangeSync(user.employeeId, from, to);
            if (!records.isEmpty()) {
                com.chowking.smartdtr.model.PayrollEntry entry = 
                    com.chowking.smartdtr.utils.PayrollCalculator.compute(user, records, from, to);
                entry.status = "FINAL"; // Make it ready for download
                db.payrollDao().insertOrReplace(entry);
            }
        }

        private void seedSchedule(String empId, int daysOffset) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, daysOffset);
            
            // Random days off in the future too
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY) return;

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFmt.format(cal.getTime());

            com.chowking.smartdtr.model.Schedule schedule = db.scheduleDao().getScheduleByDateSync(empId, dateStr);
            if (schedule == null) {
                schedule = new com.chowking.smartdtr.model.Schedule();
                schedule.employeeId = empId;
                schedule.date = dateStr;
                
                int[] startOptions = {8, 12, 15};
                int startHour = startOptions[new java.util.Random().nextInt(3)];
                schedule.shiftStart = String.format(Locale.getDefault(), "%02d:00", startHour);
                schedule.shiftEnd = String.format(Locale.getDefault(), "%02d:00", startHour + 9);
                schedule.isPublished = 1;
                schedule.note = "Standard Shift";
                
                db.scheduleDao().insertSchedule(schedule);
            }
        }

        private void seedUser(String empId, String name, String role, String password, float rate, String pos, float sssL, float pagL, float mealD) {
            User user = db.userDao().getUserByEmployeeId(empId);
            boolean isNew = false;
            if (user == null) {
                user = new User();
                user.employeeId = empId;
                isNew = true;
            }
            
            // Force seed values even if user exists
            user.fullName = name;
            user.role = role;
            user.passwordHash = HashUtils.hashPassword(password);
            user.isActive = 1;
            user.hourlyRate = rate;
            user.position = pos;
            user.sssLoanMonthly = sssL;
            user.pagibigLoanMonthly = pagL;
            user.mealDeductionRate = mealD;

            if (isNew) {
                db.userDao().insertUser(user);
            } else {
                db.userDao().updateUser(user);
            }

            // Always push to Cloud to ensure multi-device sync
            firestore.collection("users").document(empId).set(user, SetOptions.merge());
        }

        private void seedAttendance(String empId, int daysOffset) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, daysOffset);
            
            // Most crew don't work 7 days a week. Let's give them 2 random days off.
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.MONDAY) return;

            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String dateStr = dateFmt.format(cal.getTime());

            AttendanceRecord record = null;
            List<AttendanceRecord> localRecords = db.attendanceDao().getRecordsByEmployeeAndDateRangeSync(empId, dateStr, dateStr);
            
            if (localRecords.isEmpty()) {
                record = new AttendanceRecord();
                record.employeeId = empId;
                record.date = dateStr;
                
                // Realistic Shift Logic:
                // Morning Shift: 8:00 AM, Mid: 12:00 PM, Closing: 3:00 PM
                int[] startOptions = {8, 12, 15};
                int startHour = startOptions[new java.util.Random().nextInt(3)];
                int startMinute = new java.util.Random().nextInt(15); // Random 0-14 mins delay
                
                // Duration: 9 hours (8h work + 1h break) is standard. 
                // Occasionally 5 hours for short shifts.
                int duration = (new java.util.Random().nextFloat() > 0.8) ? 5 : 9;
                
                // Occasional Overtime (only 10% chance, and only for full shifts)
                if (duration == 9 && new java.util.Random().nextFloat() > 0.9) {
                    duration += 2; // 2 hours OT
                }

                if (startHour >= 15) record.isNightShift = 1;
                if (daysOffset == -10) record.isHoliday = 1;

                cal.set(Calendar.HOUR_OF_DAY, startHour);
                cal.set(Calendar.MINUTE, startMinute);
                record.timeIn = cal.getTimeInMillis();
                
                cal.set(Calendar.HOUR_OF_DAY, startHour + duration);
                record.timeOut = cal.getTimeInMillis();
                
                // Total hours worked (deduct 1 hour for lunch if shift > 5 hours)
                record.totalHours = (duration > 5) ? (duration - 1) : duration;
                
                db.attendanceDao().insertRecord(record);
            } else {
                record = localRecords.get(0);
            }

            if (record != null) {
                String docId = record.employeeId + "_" + record.date + "_" + record.timeIn;
                firestore.collection("attendance").document(docId).set(record, SetOptions.merge());
            }
        }
    }
}
