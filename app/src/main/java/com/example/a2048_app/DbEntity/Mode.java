package com.example.a2048_app.DbEntity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entité Room représentant un mode de jeu (ex: Classique, Multijoueur, Défi).
 * Cette table sert de dictionnaire/référence pour classer et lier
 * chaque partie (Game) à son mode spécifique de manière optimisée.
 */
@Entity(tableName = "Mode")
public class Mode implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    // Le nom textuel du mode (utilisé pour les requêtes ou l'affichage)
    private final String gameMode;

    public Mode(String gameMode) {
        this.gameMode = gameMode;
    }

    // --- Implémentation de Parcelable ---
    // Permet de sérialiser l'objet pour le transmettre facilement
    // entre différentes Activités ou Fragments via des Intents/Bundles.

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(gameMode);
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGameMode() {
        return gameMode;
    }
}