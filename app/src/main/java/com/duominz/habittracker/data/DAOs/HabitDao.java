package com.duominz.habittracker.data.DAOs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.duominz.habittracker.data.models.Habit;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert
    void insert(Habit habit);
    @Update
    void update(Habit habit);
    @Delete
    void delete(Habit habit);
    @Query("SELECT * FROM habits")
    List<Habit> getAllHabits();
    @Query("SELECT * FROM habits WHERE isArchived = 0")
    List<Habit> getActiveHabits();
    @Query("SELECT * FROM habits WHERE isArchived = 1")
    List<Habit> getArchivedHabits();
    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitById(int id);

}
