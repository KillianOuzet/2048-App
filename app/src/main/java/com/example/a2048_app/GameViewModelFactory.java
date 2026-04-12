package com.example.a2048_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Fabrique (Factory) personnalisée pour instancier le GameViewModel.
 * Par défaut, le système Android ne sait créer que des ViewModels avec un constructeur vide.
 * Comme notre GameViewModel a besoin de paramètres spécifiques (le contexte de l'Application,
 * la taille de la grille, ou une grille sauvegardée), cette fabrique est indispensable
 * pour faire le pont et lui transmettre ces informations lors de sa création.
 */
public class GameViewModelFactory implements ViewModelProvider.Factory {

    private final Application application;
    private final int gridSize;
    private Grid savedGrid = null;

    /**
     * Constructeur utilisé pour démarrer une TOUTE NOUVELLE partie.
     */
    public GameViewModelFactory(Application application, int gridSize) {
        this.application = application;
        this.gridSize = gridSize;
    }

    /**
     * Constructeur utilisé pour RESTAURER une partie en cours.
     *
     * @param savedGrid L'objet Grid récupéré et désérialisé depuis les SharedPreferences.
     */
    public GameViewModelFactory(Application application, int gridSize, Grid savedGrid) {
        this.application = application;
        this.gridSize = gridSize;
        this.savedGrid = savedGrid;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        // Android appelle automatiquement cette méthode quand la GameActivity
        // réclame son ViewModel. On aiguille alors vers le bon constructeur.
        if (savedGrid != null) {
            return (T) new GameViewModel(application, savedGrid);
        } else {
            return (T) new GameViewModel(application, gridSize);
        }
    }
}