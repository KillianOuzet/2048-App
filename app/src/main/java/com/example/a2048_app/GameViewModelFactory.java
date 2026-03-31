package com.example.a2048_app;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private final int gridSize;
    private Grid savedGrid = null;

    public GameViewModelFactory(int gridSize) {
        this.gridSize = gridSize;
    }

    public GameViewModelFactory(int gridSize, Grid savedGrid) {
        this.gridSize = gridSize;
        this.savedGrid = savedGrid;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (savedGrid != null) {
            // On utilise le nouveau constructeur du ViewModel
            return (T) new GameViewModel(savedGrid);
        } else {
            // On utilise ton constructeur classique
            return (T) new GameViewModel(gridSize);
        }
    }
}
