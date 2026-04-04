// ModeDao.java
package com.example.a2048_app.DbDao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.a2048_app.DbEntity.Mode;

import java.util.List;

@Dao
public interface ModeDao {

    @Insert
    long insert(Mode mode);

    @Update
    void update(Mode mode);

    @Delete
    void delete(Mode mode);

    @Query("SELECT * FROM Mode WHERE id = :id")
    Mode getById(int id);

    @Query("SELECT * FROM Mode WHERE gameMode = :gameMode")
    Mode getByGameMode(String gameMode);

    @Query("SELECT * FROM Mode")
    LiveData<List<Mode>> getAll();
}