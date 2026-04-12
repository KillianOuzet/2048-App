package com.example.a2048_app.DbEntity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entité Room représentant un joueur.
 * Cette table stocke de manière persistante les pseudonymes pour les lier aux parties (Game).
 * L'implémentation de l'interface Parcelable permet de transmettre facilement cet objet complet
 * entre différents écrans via des Intents.
 */
@Entity(tableName = "Player")
public class Player implements Parcelable {

    /**
     * Identifiant unique, généré automatiquement par la base de données.
     * C'est ce champ qui sert de clé étrangère (Foreign Key) dans la table Game.
     */
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;

    public Player(String name) {
        this.name = name;
    }

    // --- Implémentation de Parcelable ---
    // Cette méthode de sérialisation est préférée à "Serializable" (Java classique)
    // car elle est native à Android et beaucoup plus performante en mémoire.

    protected Player(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public static final Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel in) {
            return new Player(in);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    // --- Getters & Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}