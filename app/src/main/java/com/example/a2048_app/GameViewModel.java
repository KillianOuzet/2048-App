package com.example.a2048_app;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Objects;

public class GameViewModel extends ViewModel {
    private MutableLiveData<Grid> currentGrid;

    public GameViewModel(int gridSize) {
        this.currentGrid = new MutableLiveData<>(new Grid(gridSize));
    }

    public GameViewModel(Grid savedGrid) {
        this.currentGrid = new MutableLiveData<>(savedGrid);
    }

    public LiveData<Grid> getCurrentGrid() {
        return this.currentGrid;
    }

    public void resetGrid(int size) {
        currentGrid.setValue(new Grid(size));
    }

    public void rightSlide() {
        Objects.requireNonNull(this.currentGrid.getValue()).rightSlide();
        this.currentGrid.setValue(this.currentGrid.getValue());
    }

    public void leftSlide() {
        Objects.requireNonNull(this.currentGrid.getValue()).leftSlide();
        this.currentGrid.setValue(this.currentGrid.getValue());
    }

    public void upSlide() {
        Objects.requireNonNull(this.currentGrid.getValue()).upSlide();
        this.currentGrid.setValue(this.currentGrid.getValue());
    }

    public void downSlide() {
        Objects.requireNonNull(this.currentGrid.getValue()).downSlide();
        this.currentGrid.setValue(this.currentGrid.getValue());
    }
}
