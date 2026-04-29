package com.chowking.smartdtr.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.chowking.smartdtr.database.dao.AttendanceDao;
import com.chowking.smartdtr.database.dao.PayrollDao;
import com.chowking.smartdtr.database.dao.UserDao;
import com.chowking.smartdtr.model.AttendanceRecord;
import com.chowking.smartdtr.model.PayrollEntry;
import com.chowking.smartdtr.model.User;

@Database(entities = {User.class, AttendanceRecord.class, PayrollEntry.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract UserDao userDao();
    public abstract AttendanceDao attendanceDao();
    public abstract PayrollDao payrollDao();

    // ── Migration: v1 → v2 ────────────────────────────────────────────────
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE users ADD COLUMN hourlyRate REAL NOT NULL DEFAULT 0.0");
            database.execSQL("ALTER TABLE users ADD COLUMN position TEXT");
            database.execSQL("ALTER TABLE users ADD COLUMN isActive INTEGER NOT NULL DEFAULT 0");
        }
    };

    // ── Migration: v2 → v3 ────────────────────────────────────────────────
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            // AttendanceRecord new columns - Use a safer approach for existing columns
            // Room validation will fail if we don't handle DEFAULT values exactly as expected.
            db.execSQL("ALTER TABLE attendance ADD COLUMN isNightShift INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE attendance ADD COLUMN isHoliday INTEGER NOT NULL DEFAULT 0");
            db.execSQL("ALTER TABLE attendance ADD COLUMN isSpecialHoliday INTEGER NOT NULL DEFAULT 0");

            // User new columns
            db.execSQL("ALTER TABLE users ADD COLUMN sssLoanMonthly REAL NOT NULL DEFAULT 0.0");
            db.execSQL("ALTER TABLE users ADD COLUMN pagibigLoanMonthly REAL NOT NULL DEFAULT 0.0");
            db.execSQL("ALTER TABLE users ADD COLUMN mealDeductionRate REAL NOT NULL DEFAULT 0.0");

            // New payroll table - Ensure alignment with Room's TableInfo expectations
            db.execSQL("DROP TABLE IF EXISTS payroll");
            db.execSQL("CREATE TABLE payroll (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "employeeId TEXT, " +
                    "cutoffFrom TEXT, " +
                    "cutoffTo TEXT, " +
                    "generatedAt INTEGER NOT NULL, " +
                    "basicPay REAL NOT NULL, " +
                    "regularOtPay REAL NOT NULL, " +
                    "nightPremiumPay REAL NOT NULL, " +
                    "legalHolidayPay REAL NOT NULL, " +
                    "specialHolidayPay REAL NOT NULL, " +
                    "holidayOtPay REAL NOT NULL, " +
                    "silPay REAL NOT NULL, " +
                    "grossPay REAL NOT NULL, " +
                    "sssPremium REAL NOT NULL, " +
                    "philhealth REAL NOT NULL, " +
                    "pagibigPremium REAL NOT NULL, " +
                    "sssLoan REAL NOT NULL, " +
                    "pagibigLoan REAL NOT NULL, " +
                    "mealDeduction REAL NOT NULL, " +
                    "totalDeductions REAL NOT NULL, " +
                    "netPay REAL NOT NULL, " +
                    "totalHours REAL NOT NULL, " +
                    "regularHours REAL NOT NULL, " +
                    "otHours REAL NOT NULL, " +
                    "nightHours REAL NOT NULL, " +
                    "holidayHours REAL NOT NULL, " +
                    "daysWorked INTEGER NOT NULL, " +
                    "status TEXT)");

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_payroll_employeeId_cutoffFrom_cutoffTo " +
                    "ON payroll (employeeId, cutoffFrom, cutoffTo)");
        }
    };


// Add this migration constant:
    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL(
                    "ALTER TABLE users ADD COLUMN googleId TEXT"
            );
        }
    };

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "chowking_dtr_db"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
