package com.example.a2048_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Activité principale servant de point d'entrée et de conteneur pour l'application.
 * Elle gère la barre de navigation inférieure (Bottom Navigation) et orchestre
 * le passage entre les différents Fragments (Accueil, Scores, Stats, Réglages).
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("2048_settings", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);

        // Active l'affichage plein écran (sous les barres système) pour un look moderne
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- Gestion de la musique au démarrage ---
        // On vérifie les préférences de l'utilisateur avant de lancer le MusicManager (Singleton)
        if (prefs.getInt("musiqueDeFond", 1) == 1) {
            MusicManager.getInstance().demarrerMusique(this);
        } else {
            MusicManager.getInstance().arreterTout();
        }

        // --- Gestion des Insets (Espaces système) ---
        // Assure que le contenu ne soit pas caché derrière l'heure ou l'encoche de l'appareil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // Configuration de la barre de navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // On annule le "tint" par défaut pour afficher les couleurs originales de nos icônes
        bottomNavigationView.setItemIconTintList(null);

        // Au premier lancement (savedInstanceState est nul), on affiche le Fragment d'accueil par défaut
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        // --- Logique de navigation ---
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();
            if (itemId == R.id.accueil) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.reglages) {
                selectedFragment = new SettingsFragment();
            } else if (itemId == R.id.scores) {
                selectedFragment = new ScoresFragment();
            } else if (itemId == R.id.stats) {
                selectedFragment = new StatsFragment();
            }

            // Remplacement dynamique du Fragment dans le conteneur XML (R.id.fragment_container)
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }
}