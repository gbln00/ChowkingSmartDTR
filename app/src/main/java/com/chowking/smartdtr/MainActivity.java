package com.chowking.smartdtr;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.HashUtils;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);

            if (db.userDao().getAllUsers().isEmpty()) {
                // ── Seed: Service Crew ──────────────────────────────────────
                User crew         = new User();
                crew.employeeId   = "CHW-001";
                crew.fullName     = "Juan Dela Cruz";
                crew.role         = "CREW";
                crew.position     = "Service Crew";
                crew.hourlyRate   = 76.25f;  // Region 10 minimum wage
                crew.isActive     = 1;
                crew.passwordHash = HashUtils.hashPassword("crew123");
                db.userDao().insertUser(crew);

                // ── Seed: Cashier ───────────────────────────────────────────
                User cashier         = new User();
                cashier.employeeId   = "CHW-002";
                cashier.fullName     = "Maria Santos";
                cashier.role         = "CREW";
                cashier.position     = "Cashier";
                cashier.hourlyRate   = 80.00f;
                cashier.isActive     = 1;
                cashier.passwordHash = HashUtils.hashPassword("crew123");
                db.userDao().insertUser(cashier);

                // ── Seed: Manager ───────────────────────────────────────────
                User manager         = new User();
                manager.employeeId   = "CHW-MGR";
                manager.fullName     = "Rosa Reyes";
                manager.role         = "MANAGER";
                manager.position     = "Branch Manager";
                manager.hourlyRate   = 120.00f;
                manager.isActive     = 1;
                manager.passwordHash = HashUtils.hashPassword("manager123");
                db.userDao().insertUser(manager);

                // ── Seed: Admin ─────────────────────────────────────────────
                User admin         = new User();
                admin.employeeId   = "CHW-ADM";
                admin.fullName     = "Admin User";
                admin.role         = "ADMIN";
                admin.position     = "System Admin";
                admin.hourlyRate   = 0f;
                admin.isActive     = 1;
                admin.passwordHash = HashUtils.hashPassword("admin123");
                db.userDao().insertUser(admin);
            }
        });

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}