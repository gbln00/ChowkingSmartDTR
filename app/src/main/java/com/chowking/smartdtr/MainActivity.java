package com.chowking.smartdtr;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.chowking.smartdtr.ui.SplashActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect to SplashActivity which handles seeding and session routing
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }
}
