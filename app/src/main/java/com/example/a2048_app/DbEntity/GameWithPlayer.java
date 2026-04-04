package com.example.a2048_app.DbEntity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class GameWithPlayer {

    @Embedded
    public Game game;

    @Relation(
            parentColumn = "playerId",
            entityColumn = "id"
    )
    public Player player;
}