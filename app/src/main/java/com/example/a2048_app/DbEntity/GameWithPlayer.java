package com.example.a2048_app.DbEntity;

import androidx.room.Embedded;
import androidx.room.Relation;

/**
 * Classe relationnelle utilisée par Room pour modéliser une jointure SQL (JOIN).
 * Elle ne représente pas une table en soi (pas d'annotation @Entity), mais sert de
 * "réceptacle" pour récupérer simultanément une partie (Game) ET le joueur (Player) associé.
 * C'est essentiel pour afficher le classement (Leaderboard) avec les pseudos des joueurs.
 */
public class GameWithPlayer {

    /**
     * L'annotation @Embedded indique à Room de "déballer" tous les champs de l'entité Game
     * directement dans le résultat de la requête. C'est l'objet principal de notre recherche.
     */
    @Embedded
    public Game game;

    /**
     * L'annotation @Relation indique à Room d'exécuter automatiquement une sous-requête
     * pour trouver le Player correspondant.
     * Il relie la clé étrangère "playerId" de la table Game à la clé primaire "id" de la table Player.
     */
    @Relation(
            parentColumn = "playerId",
            entityColumn = "id"
    )
    public Player player;
}