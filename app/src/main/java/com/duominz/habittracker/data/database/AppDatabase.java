package com.duominz.habittracker.data.database;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.duominz.habittracker.data.DAOs.HabitDao;
import com.duominz.habittracker.data.DAOs.HabitLogDao;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.data.models.HabitLog;

@Database(entities = {Habit.class, HabitLog.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HabitDao habitDao();
    public abstract HabitLogDao habitLogDao();

    private static volatile AppDatabase instance;
    public static AppDatabase getInstance(final Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "app_database"
                    ).build();
                }
            }
        }
        return instance;
    }
}
