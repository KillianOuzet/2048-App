package com.example.a2048_app;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private final int gridSize;

    public GameViewModelFactory(int gridSize) {
        this.gridSize = gridSize;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GameViewModel(gridSize);
    }
}
