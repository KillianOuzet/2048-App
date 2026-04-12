package com.example.a2048_app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.a2048_app.DbDao.GameDao;
import com.example.a2048_app.DbDao.PlayerDao;
import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.Mode;
import com.example.a2048_app.DbEntity.Player;

/**
 * Classe principale de la base de données Room.
 * Elle regroupe toutes les entités (tables) et fournit l'accès aux DAO.
 * Elle est implémentée avec le design pattern "Singleton" pour éviter les fuites de mémoire.
 */
@Database(entities = {Player.class, Mode.class, Game.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PlayerDao playerDao();

    public abstract GameDao gameDao();

    // Le mot-clé "volatile" garantit que les modifications de cette variable
    // sont immédiatement visibles par tous les autres threads.
    private static volatile AppDatabase instance;

    /**
     * Récupère l'instance unique de la base de données (Pattern Singleton avec "Double-Checked Locking").
     * La synchronisation (synchronized) garantit qu'un seul thread peut créer la base lors du tout premier appel,
     * évitant les conflits si plusieurs fragments/activités demandent la BDD en même temps.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "2048_db").fallbackToDestructiveMigration().addCallback(roomCallback).build();
                }
            }
        }
        return instance;
    }

    /**
     * Callback déclenché uniquement lors de la création initiale (onCreate) de la base de données SQLite.
     * C'est une étape critique pour pré-peupler les tables de référence.
     * Sans ces insertions (ID 1, 2 et 3 pour les modes), la toute première sauvegarde d'une partie planterait
     * à cause d'une contrainte de clé étrangère (ForeignKey) pointant vers un élément inexistant.
     */
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // Pré-remplissage des joueurs
            db.execSQL("INSERT INTO Player (name) VALUES ('Anonyme')");

            // Pré-remplissage des modes de jeu (Les ID 1, 2 et 3 seront assignés automatiquement)
            db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Classique')");
            db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Multijoueur')");
            db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Defi')");
        }
    };
}