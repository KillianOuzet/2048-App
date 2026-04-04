package com.example.a2048_app;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.a2048_app.DbDao.GameDao;
import com.example.a2048_app.DbDao.ModeDao;
import com.example.a2048_app.DbDao.PlayerDao;
import com.example.a2048_app.DbEntity.Game;
import com.example.a2048_app.DbEntity.Mode;
import com.example.a2048_app.DbEntity.Player;

@Database(entities = {Player.class, Mode.class, Game.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PlayerDao playerDao();

    public abstract ModeDao modeDao();

    public abstract GameDao gameDao();

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "2048_db"
                            )
                            .fallbackToDestructiveMigration()
                            .addCallback(new RoomDatabase.Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    // Insère le joueur Anonyme à la création de la bd pour être sûr qu'il ait l'id 1
                                    db.execSQL("INSERT INTO Player (name) VALUES ('Anonyme')");
                                    db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Classique')");
                                    db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Multijoueur')");
                                    db.execSQL("INSERT INTO Mode (gameMode) VALUES ('Defi')");
                                }
                            })
                            .build();
                }
            }
        }
        return instance;
    }
}