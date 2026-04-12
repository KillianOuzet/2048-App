package com.example.a2048_app.DbDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.a2048_app.DbEntity.Player;

import java.util.List;

/**
 * Data Access Object (DAO) pour l'entité Player.
 * Gère l'accès et la manipulation des données liées aux joueurs (pseudos)
 * enregistrés localement sur l'appareil.
 */
@Dao
public interface PlayerDao {

    /**
     * Insère un nouveau joueur dans la base de données.
     *
     * @return L'identifiant (ID) auto-généré, qui est essentiel pour lier
     * immédiatement une nouvelle partie (Game) à ce joueur.
     */
    @Insert
    long insert(Player player);

    @Update
    void update(Player player);

    @Delete
    void delete(Player player);

    @Query("SELECT * FROM Player WHERE id = :id")
    Player getById(int id);

    /**
     * Recherche un joueur de manière exacte via son pseudo.
     * Très utile lors de la sauvegarde d'un score pour vérifier si le joueur
     * existe déjà, afin d'éviter de créer des doublons dans la table.
     */
    @Query("SELECT * FROM Player WHERE name = :name")
    Player getByName(String name);

    /**
     * Récupère la liste de tous les joueurs connus.
     * L'utilisation de LiveData est parfaite ici pour alimenter automatiquement
     * les suggestions de l'AutoCompleteTextView à la fin d'une partie.
     */
    @Query("SELECT * FROM Player")
    LiveData<List<Player>> getAll();
}