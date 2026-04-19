package com.duominz.habittracker.data.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "habit_logs",
        primaryKeys = {"habitId", "date"},
        foreignKeys = @ForeignKey(
                entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE
        )
)
public class HabitLog {
    public int habitId;
    @NonNull
    public String date; // Format: "yyyy-MM-dd"

    public Boolean isSkipped = false;

    public HabitLog(int habitId, @NonNull String date, Boolean isSkipped) {
        this.habitId = habitId;
        this.date = date;
        this.isSkipped = isSkipped;
    }

}