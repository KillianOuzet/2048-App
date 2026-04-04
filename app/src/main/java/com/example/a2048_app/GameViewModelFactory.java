package com.example.a2048_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class GameViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final int gridSize;

    public GameViewModelFactory(Application application, int gridSize) {
        this.application = application;
        this.gridSize = gridSize;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GameViewModel(application, gridSize);
    }
}
