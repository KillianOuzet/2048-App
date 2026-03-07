package com.example.a2048_app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("2048_settings", MODE_PRIVATE);
        int themeChoisi = prefs.getInt("themeChoisi", -1);

        if (themeChoisi == -1) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                themeChoisi = 1;
            } else {
                themeChoisi = 0;
            }
            prefs.edit().putInt("themeChoisi", themeChoisi).apply();
        }

        if (themeChoisi == 0) {
            setTheme(R.style.Theme_App_Light);
        } else if (themeChoisi == 1) {
            setTheme(R.style.Theme_App_Dark);
        } else if (themeChoisi == 2) {
            setTheme(R.style.Theme_App_Color);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (prefs.getInt("musiqueDeFond", 1) == 1) {
            MusicManager.getInstance().demarrerMusique(this);
        } else {
            MusicManager.getInstance().arreterTout();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.accueil) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.reglages) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }
}