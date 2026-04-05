package com.example.a2048_app.DbDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.a2048_app.DbEntity.Player;

import java.util.List;

@Dao
public interface PlayerDao {

    @Insert
    long insert(Player player);

    @Update
    void update(Player player);

    @Delete
    void delete(Player player);

    @Query("SELECT * FROM Player WHERE id = :id")
    Player getById(int id);

    @Query("SELECT * FROM Player WHERE name = :name")
    Player getByName(String name);

    @Query("SELECT * FROM Player")
    LiveData<List<Player>> getAll();
}