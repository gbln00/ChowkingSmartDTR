package com.chowking.smartdtr.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.chowking.smartdtr.R;
import com.chowking.smartdtr.database.AppDatabase;
import com.chowking.smartdtr.model.User;
import com.chowking.smartdtr.ui.LoginActivity;
import com.chowking.smartdtr.utils.HashUtils;
import com.chowking.smartdtr.utils.SessionManager;
import java.util.concurrent.Executors;

public class AdminHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        SessionManager session = new SessionManager(this);

        EditText etId    = findViewById(R.id.etNewId);
        EditText etName  = findViewById(R.id.etNewName);
        EditText etPass  = findViewById(R.id.etNewPass);
        EditText etRole  = findViewById(R.id.etNewRole);
        TextView tvResult= findViewById(R.id.tvAddResult);

        findViewById(R.id.btnAddUser).setOnClickListener(v -> {
            String id   = etId.getText().toString().trim();
            String name = etName.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String role = etRole.getText().toString().trim().toUpperCase();

            if (id.isEmpty() || name.isEmpty() || pass.isEmpty() || role.isEmpty()) {
                tvResult.setText("Please fill in all fields.");
                tvResult.setTextColor(0xFFD32F2F);
                tvResult.setVisibility(View.VISIBLE);
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                User user = new User();
                user.employeeId   = id;
                user.fullName     = name;
                user.role         = role;
                user.passwordHash = HashUtils.hashPassword(pass);
                AppDatabase.getInstance(this).userDao().insertUser(user);

                runOnUiThread(() -> {
                    tvResult.setText("User " + id + " added successfully!");
                    tvResult.setTextColor(0xFF2E7D32);
                    tvResult.setVisibility(View.VISIBLE);
                    etId.setText(""); etName.setText("");
                    etPass.setText(""); etRole.setText("");
                });
            });
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}