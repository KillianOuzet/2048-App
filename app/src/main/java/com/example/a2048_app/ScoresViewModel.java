package com.example.a2048_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.a2048_app.DbEntity.GameWithPlayer;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * ViewModel responsable de la logique de l'écran des scores.
 * Il sépare la gestion des données de l'interface utilisateur (ScoresFragment),
 * permettant ainsi de conserver les données lors des changements de configuration (comme la rotation).
 */
public class ScoresViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public ScoresViewModel(@NonNull Application application) {
        super(application);
        // Récupération de l'instance unique de la base de données
        db = AppDatabase.getInstance(application);
    }

    /**
     * Récupère le classement mondial/local pour une configuration spécifique.
     *
     * @param gridSize Taille de la grille (3, 4, 5 ou 6).
     * @param modeId   ID du mode de jeu (Classique = 1).
     * @return Un LiveData contenant la liste des 10 meilleurs scores avec les pseudos associés.
     * Grâce au LiveData, l'interface se mettra à jour automatiquement si une nouvelle
     * partie est enregistrée.
     */
    public LiveData<List<GameWithPlayer>> getLeaderboard(int gridSize, int modeId) {
        return db.gameDao().getLeaderboardWithPlayers(gridSize, modeId, 10);
    }

    /**
     * Supprime l'intégralité des scores enregistrés dans la table Game.
     * Cette opération est effectuée sur un thread séparé (via Executor) pour ne pas
     * bloquer le thread principal (UI Thread) et éviter les saccades ou les erreurs "Application Not Responding".
     */
    public void deleteAll() {
        Executors.newSingleThreadExecutor().execute(() -> {
            db.gameDao().deleteAll();
        });
    }
}