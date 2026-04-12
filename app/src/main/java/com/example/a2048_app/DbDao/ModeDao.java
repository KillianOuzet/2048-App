package com.example.a2048_app.DbDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.a2048_app.DbEntity.Mode;

import java.util.List;

/**
 * Data Access Object (DAO) pour l'entité Mode.
 * Gère l'accès aux données de la table définissant les différents modes de jeu
 * (ex: Classique, Multijoueur, Défi).
 */
@Dao
public interface ModeDao {

    /**
     * Insère un nouveau mode de jeu dans la base de données.
     *
     * @return L'identifiant (ID) auto-généré pour ce nouveau mode.
     */
    @Insert
    long insert(Mode mode);

    @Update
    void update(Mode mode);

    @Delete
    void delete(Mode mode);

    /**
     * Récupère un mode de jeu spécifique via son identifiant unique.
     */
    @Query("SELECT * FROM Mode WHERE id = :id")
    Mode getById(int id);

    /**
     * Récupère un mode de jeu directement à partir de son nom textuel.
     * Très utile dans la logique métier pour récupérer l'ID d'un mode (ex: "Classique")
     * juste avant d'enregistrer une nouvelle partie associée à ce mode.
     */
    @Query("SELECT * FROM Mode WHERE gameMode = :gameMode")
    Mode getByGameMode(String gameMode);

    /**
     * Récupère la liste complète des modes de jeu disponibles.
     * Enveloppé dans un LiveData pour réagir automatiquement si des modes sont ajoutés dynamiquement.
     */
    @Query("SELECT * FROM Mode")
    LiveData<List<Mode>> getAll();
}