package com.example.a2048_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final int gridSize;
    private Grid savedGrid = null;

    public GameViewModelFactory(Application application, int gridSize) {
        this.application = application;
        this.gridSize = gridSize;
    }

    public GameViewModelFactory(Application application, int gridSize, Grid savedGrid) {
        this.application = application;
        this.gridSize = gridSize;
        this.savedGrid = savedGrid;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (savedGrid != null) {
            // On utilise le nouveau constructeur du ViewModel
            return (T) new GameViewModel(application, savedGrid);
        } else {
            // On utilise le constructeur classique
            return (T) new GameViewModel(application, gridSize);
        }
    }
}
