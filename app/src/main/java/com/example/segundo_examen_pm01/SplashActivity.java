package com.example.segundo_examen_pm01;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int DURACION_SPLASH = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Esperar 3 segundos y abrir ListActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, ListActivity.class);
            startActivity(intent);
            finish();
        }, DURACION_SPLASH);
    }
}
