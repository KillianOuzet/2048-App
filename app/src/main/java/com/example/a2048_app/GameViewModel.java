package com.example.a2048_app;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.Player;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameViewModel extends AndroidViewModel {
    private MutableLiveData<Grid> currentGrid;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AppDatabase db;

    public GameViewModel(@NonNull Application application, int gridSize) {
        super(application);
        this.currentGrid = new MutableLiveData<>(new Grid(gridSize));
        db = AppDatabase.getInstance(application);
    }

    public GameViewModel(@NonNull Application application, Grid savedGrid) {
        super(application);
        this.currentGrid = new MutableLiveData<>(savedGrid);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<Grid> getCurrentGrid() {
        return this.currentGrid;
    }

    public void resetGrid(int size) {
        currentGrid.setValue(new Grid(size));
    }

    public boolean rightSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).rightSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean leftSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).leftSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean upSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).upSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    public boolean downSlide() {
        boolean hasMoved = Objects.requireNonNull(this.currentGrid.getValue()).downSlide();
        if (hasMoved) {
            this.currentGrid.setValue(this.currentGrid.getValue());
        }
        return hasMoved;
    }

    // BD méthodes

    public void saveGame(Game game) {
        executor.execute(() -> {
            db.gameDao().insert(game);
        });
    }

    public LiveData<Player> getPlayerByName(String name) {
        MutableLiveData<Player> playerLiveData = new MutableLiveData<>();

        executor.execute(() -> {
            Player player = db.playerDao().getByName(name);
            playerLiveData.postValue(player);
        });

        return playerLiveData;
    }

    public LiveData<List<Player>> getAllPlayers() {
        return db.playerDao().getAll();
    }

    public void insertPlayerAndSaveGame(String name, Grid grid) {
        executor.execute(() -> {
            long newPlayerId = db.playerDao().insert(new Player(name));
            Log.d("GameActivity", "Joueur créé avec l'ID : " + newPlayerId);

            Game game = new Game(grid.getScore(), grid.getSize(), grid.getMaxTile(), grid.getNbMove(), (int) newPlayerId, 1);
            long gameId = db.gameDao().insert(game);
            Log.d("GameActivity", "Partie sauvegardée avec l'ID : " + gameId);
        });
    }
}
