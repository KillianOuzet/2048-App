package com.example.a2048_app;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Classe de base dont hériteront toutes les autres Activités de l'application (MultiplayerActivity, GameActivity...).
 * Son rôle stratégique est de centraliser la gestion du thème visuel. En appliquant la logique ici,
 * on évite de dupliquer ce code dans chaque écran, garantissant une transition fluide et homogène.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("2048_settings", MODE_PRIVATE);
        int themeChoisi = prefs.getInt("themeChoisi", -1);

        // Si l'utilisateur n'a pas encore choisi de thème (premier lancement),
        // on lit la configuration actuelle de son téléphone (Mode Sombre ou Clair global)
        // afin de lui offrir une expérience intégrée et native.
        if (themeChoisi == -1) {
            int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            themeChoisi = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) ? 1 : 0;
            prefs.edit().putInt("themeChoisi", themeChoisi).apply();
        }

        // Application du style correspondant au choix enregistré.
        // Règle d'or sur Android : il est impératif d'appeler setTheme() AVANT super.onCreate().
        // Si on l'appelle après, les vues XML auront déjà été dessinées avec le mauvais thème.
        switch (themeChoisi) {
            case 1:
                setTheme(R.style.Theme_App_Dark);
                break;
            case 2:
                setTheme(R.style.Theme_App_Color);
                break;
            default:
                setTheme(R.style.Theme_App_Light);
                break;
        }

        super.onCreate(savedInstanceState);
    }
}