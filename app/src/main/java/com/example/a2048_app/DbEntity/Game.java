package com.example.a2048_app.DbEntity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entité Room représentant une session de jeu terminée.
 * Cette table est liée aux tables Player et Mode via des clés étrangères (Foreign Keys).
 */
@Entity(tableName = "Game", foreignKeys = {
        // Si un joueur est supprimé de la base, toutes ses parties associées
        // seront automatiquement supprimées grâce à "onDelete = ForeignKey.CASCADE".
        @ForeignKey(entity = Player.class, parentColumns = "id", childColumns = "playerId", onDelete = ForeignKey.CASCADE), @ForeignKey(entity = Mode.class, parentColumns = "id", childColumns = "modeId", onDelete = ForeignKey.CASCADE)},
        // L'ajout d'index sur les clés étrangères améliore considérablement
        // les performances des requêtes de recherche (ex: trouver toutes les parties d'un joueur).
        indices = {@Index("playerId"), @Index("modeId")})
public class Game implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int score;
    private final int gridSize;

    // Essentiel pour déterminer si la partie est une victoire (>= 2048) ou débloquer des succès
    private final int biggestTile;

    private final int nbMove;
    private final int playerId;
    private final int modeId;

    public Game(int score, int gridSize, int biggestTile, int nbMove, int playerId, int modeId) {
        this.score = score;
        this.gridSize = gridSize;
        this.biggestTile = biggestTile;
        this.nbMove = nbMove;
        this.playerId = playerId;
        this.modeId = modeId;
    }

    // =========================================================================================
    // IMPLÉMENTATION DE PARCELABLE
    // =========================================================================================
    // L'interface Parcelable permet de "sérialiser" cet objet de manière très performante
    // sous Android, afin de pouvoir passer un objet Game complet d'une Activity/Fragment à un autre
    // via des Intents ou des Bundles.

    protected Game(Parcel in) {
        id = in.readInt();
        score = in.readInt();
        gridSize = in.readInt();
        biggestTile = in.readInt();
        nbMove = in.readInt();
        playerId = in.readInt();
        modeId = in.readInt();
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(score);
        dest.writeInt(gridSize);
        dest.writeInt(biggestTile);
        dest.writeInt(nbMove);
        dest.writeInt(playerId);
        dest.writeInt(modeId);
    }

    // =========================================================================================
    // GETTERS & SETTERS
    // =========================================================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getGridSize() {
        return gridSize;
    }

    public int getBiggestTile() {
        return biggestTile;
    }

    public int getNbMove() {
        return nbMove;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getModeId() {
        return modeId;
    }
}