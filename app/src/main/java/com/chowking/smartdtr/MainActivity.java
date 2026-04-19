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

        // Seed test users on first run
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(this);
            if (db.userDao().getAllUsers().isEmpty()) {

                User crew = new User();
                crew.employeeId   = "CHW-001";
                crew.fullName     = "Juan Dela Cruz";
                crew.role         = "CREW";
                crew.passwordHash = HashUtils.hashPassword("crew123");
                db.userDao().insertUser(crew);

                User manager = new User();
                manager.employeeId   = "CHW-MGR";
                manager.fullName     = "Maria Santos";
                manager.role         = "MANAGER";
                manager.passwordHash = HashUtils.hashPassword("manager123");
                db.userDao().insertUser(manager);

                User admin = new User();
                admin.employeeId   = "CHW-ADM";
                admin.fullName     = "Admin User";
                admin.role         = "ADMIN";
                admin.passwordHash = HashUtils.hashPassword("admin123");
                db.userDao().insertUser(admin);
            }
        });

        // Go straight to Login
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}