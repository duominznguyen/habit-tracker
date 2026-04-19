package com.duominz.habittracker.data.DAOs;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.duominz.habittracker.data.models.HabitLog;

import java.util.List;

@Dao
public interface HabitLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HabitLog habitLog);

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :date")
    void delete(int habitId, String date);

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND isSkipped = 0")
    int getTotalCompletedDays(int habitId);

    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId AND isSkipped = 0 AND date BETWEEN :startDate AND :endDate")
    List<String> getCompletedDatesInRange(int habitId, String startDate, String endDate);

    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId AND isSkipped = 1 AND date BETWEEN :startDate AND :endDate")
    List<String> getSkippedDatesInRange(int habitId, String startDate, String endDate);

    @Query("SELECT * FROM habit_logs WHERE date = :date")
    List<HabitLog> getLogsByDate(String date);

    @Query("SELECT date FROM habit_logs WHERE habitId = :habitId AND isSkipped = 0 ORDER BY date DESC")
    List<String> getAllCompletedDates(int habitId);
}
