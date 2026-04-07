package com.example.a2048_app;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.GameWithPlayer;

import java.util.List;
import java.util.concurrent.Executors;

public class ScoresViewModel extends AndroidViewModel {

    private final AppDatabase db;

    public ScoresViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
    }

    public LiveData<List<GameWithPlayer>> getLeaderboard(int gridSize, int modeId) {
        return db.gameDao().getLeaderboardWithPlayers(gridSize, modeId, 10);
    }

    public void deleteAll() {
        Executors.newSingleThreadExecutor().execute(() -> db.gameDao().deleteAll());
    }
}
