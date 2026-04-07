// GameDao.java
package com.example.a2048_app.DbDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.GameWithPlayer;

import java.util.List;

@Dao
public interface GameDao {

    @Insert
    long insert(Game game);

    @Update
    void update(Game game);

    @Delete
    void delete(Game game);

    @Query("SELECT * FROM Game WHERE id = :id")
    Game getById(int id);

    // Toutes les parties d'un joueur
    @Query("SELECT * FROM Game WHERE playerId = :playerId ORDER BY score DESC")
    LiveData<List<Game>> getByPlayer(int playerId);

    // Toutes les parties d'un mode
    @Query("SELECT * FROM Game WHERE modeId = :modeId ORDER BY score DESC")
    LiveData<List<Game>> getByMode(int modeId);

    // Toutes les parties d'un joueur pour un mode donné
    @Query("SELECT * FROM Game WHERE playerId = :playerId AND modeId = :modeId ORDER BY score DESC")
    LiveData<List<Game>> getByPlayerAndMode(int playerId, int modeId);

    // Meilleur score global
    @Query("SELECT * FROM Game WHERE gridSize = :gridSize AND modeId = :modeId ORDER BY score DESC LIMIT 1")
    LiveData<Game> getBestScoreByGridSizeAndMode(int gridSize, int modeId);

    // Meilleur score d'un joueur
    @Query("SELECT MAX(score) FROM Game WHERE playerId = :playerId")
    LiveData<Integer> getBestScoreByPlayer(int playerId);

    // Meilleur score d'un joueur pour un mode donné
    @Query("SELECT MAX(score) FROM Game WHERE playerId = :playerId AND modeId = :modeId")
    LiveData<Integer> getBestScoreByPlayerAndMode(int playerId, int modeId);

    // Classement général
    @Transaction
    @Query("SELECT * FROM Game WHERE gridSize = :gridSize AND modeId = :modeId ORDER BY score DESC LIMIT :limit")
    LiveData<List<GameWithPlayer>> getLeaderboardWithPlayers(int gridSize, int modeId, int limit);

    @Query("SELECT COUNT(*) FROM GAME WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbGamePlayedByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 2048 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbVictoriesByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile < 2048 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbDefeatsByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT SUM(nbMove) FROM Game WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbCoupsByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 1024 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbReached1024ByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 512 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbReached512ByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT MAX(biggestTile) FROM Game WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getMaxTileByGridSizeAndMode(int gridSize, int modeId);

    // Toutes les parties
    @Query("SELECT * FROM Game ORDER BY score DESC")
    LiveData<List<Game>> getAll();

    // Supprime toutes les parties
    @Query("DELETE FROM Game")
    void deleteAll();
}