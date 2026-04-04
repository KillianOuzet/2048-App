package com.example.a2048_app.DbEntity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Game",
        foreignKeys = {
                @ForeignKey(
                        entity = Player.class,
                        parentColumns = "id",
                        childColumns = "playerId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Mode.class,
                        parentColumns = "id",
                        childColumns = "modeId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("playerId"),
                @Index("modeId")
        }
)
public class Game implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int score;
    private int gridSize;
    private int biggestTile;
    private int nbMove;
    private int playerId;
    private int modeId;

    public Game(int score, int gridSize, int biggestTile, int nbMove, int playerId, int modeId) {
        this.score = score;
        this.gridSize = gridSize;
        this.biggestTile = biggestTile;
        this.nbMove = nbMove;
        this.playerId = playerId;
        this.modeId = modeId;
    }

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

    // Getters
    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
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

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    public void setBiggestTile(int biggestTile) {
        this.biggestTile = biggestTile;
    }

    public void setNbMove(int nbMove) {
        this.nbMove = nbMove;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

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
}