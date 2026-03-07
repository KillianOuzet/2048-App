package com.example.a2048_app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("2048_settings", MODE_PRIVATE);
        int themeChoisi = prefs.getInt("themeChoisi", -1);

        // Premier lancement
        if (themeChoisi == -1) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            themeChoisi = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? 1 : 0;
            prefs.edit().putInt("themeChoisi", themeChoisi).apply();
        }

        switch (themeChoisi) {
            case 1:  setTheme(R.style.Theme_App_Dark);  break;
            case 2:  setTheme(R.style.Theme_App_Color); break;
            default: setTheme(R.style.Theme_App_Light); break;
        }

        super.onCreate(savedInstanceState);
    }
}
