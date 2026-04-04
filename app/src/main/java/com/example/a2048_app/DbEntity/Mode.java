package com.example.a2048_app.DbEntity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Mode")
public class Mode implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String gameMode;

    public Mode(String gameMode) {
        this.gameMode = gameMode;
    }

    protected Mode(Parcel in) {
        id = in.readInt();
        gameMode = in.readString();
    }

    public static final Creator<Mode> CREATOR = new Creator<Mode>() {
        @Override
        public Mode createFromParcel(Parcel in) {
            return new Mode(in);
        }

        @Override
        public Mode[] newArray(int size) {
            return new Mode[size];
        }
    };

    // Getters
    public int getId() {
        return id;
    }

    public String getGameMode() {
        return gameMode;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(gameMode);
    }
}