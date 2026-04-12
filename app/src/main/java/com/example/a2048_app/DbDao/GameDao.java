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

/**
 * Data Access Object (DAO) pour l'entité Game.
 * Interface faisant le pont entre la logique métier (ViewModel) et la base de données Room (SQLite).
 * L'utilisation de LiveData permet à l'interface graphique de se mettre à jour automatiquement
 * dès qu'une donnée change dans la base.
 */
@Dao
public interface GameDao {

    @Insert
    long insert(Game game);

    @Update
    void update(Game game);

    @Query("DELETE FROM Game")
    void deleteAll();

    /**
     * Récupère le record absolu pour une taille de grille et un mode donnés.
     * Idéal pour afficher le "Meilleur Score" en haut de la grille pendant une partie.
     */
    @Query("SELECT * FROM Game WHERE gridSize = :gridSize AND modeId = :modeId ORDER BY score DESC LIMIT 1")
    LiveData<Game> getBestScoreByGridSizeAndMode(int gridSize, int modeId);

    /**
     * Récupère le classement (Leaderboard) complet.
     * L'annotation @Transaction est indispensable pour effectuer une jointure SQL sécurisée
     * entre la table Game et la table Player via la classe relationnelle GameWithPlayer.
     */
    @Transaction
    @Query("SELECT * FROM Game WHERE gridSize = :gridSize AND modeId = :modeId ORDER BY score DESC LIMIT :limit")
    LiveData<List<GameWithPlayer>> getLeaderboardWithPlayers(int gridSize, int modeId, int limit);

    /**
     * Calcule le nombre total de parties jouées sur une configuration précise pour les statistiques.
     */
    @Query("SELECT COUNT(*) FROM GAME WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbGamePlayedByGridSizeAndMode(int gridSize, int modeId);

    /**
     * Comptabilise les victoires. Une victoire est définie par l'obtention de la tuile 2048 (ou plus).
     */
    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 2048 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbVictoriesByGridSizeAndMode(int gridSize, int modeId);

    /**
     * Comptabilise les défaites. Une défaite correspond à une fin de partie avec une tuile max inférieure à 2048.
     */
    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile < 2048 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbDefeatsByGridSizeAndMode(int gridSize, int modeId);

    /**
     * Somme de tous les mouvements (swipes) effectués par l'utilisateur pour calculer l'engagement.
     */
    @Query("SELECT SUM(nbMove) FROM Game WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbCoupsByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 1024 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbReached1024ByGridSizeAndMode(int gridSize, int modeId);

    @Query("SELECT COUNT(*) FROM Game WHERE biggestTile >= 512 AND gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getNbReached512ByGridSizeAndMode(int gridSize, int modeId);

    /**
     * Récupère la valeur de la plus haute tuile jamais atteinte (ex: 4096) pour débloquer visuellement les badges.
     */
    @Query("SELECT MAX(biggestTile) FROM Game WHERE gridSize = :gridSize AND modeId = :modeId")
    LiveData<Integer> getMaxTileByGridSizeAndMode(int gridSize, int modeId);
}