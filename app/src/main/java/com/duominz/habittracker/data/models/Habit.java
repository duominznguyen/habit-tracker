package com.duominz.habittracker.data.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String habitName;
    public String icon;
    public String displayColor;
    public String startDate;
    public String endDate;
    public String frequencyType; // Daily, Weekly (Special days in the week), Monthly (Specific days in the month)
    public String frequencyValue; // null (for Daily), 1,3,4,... (for Weekly), 1,2,5,10,21,... (for Monthly)
    public boolean isReminderEnabled;
    public String reminderTime;
    public int currentStreak;
    public int highestStreak;
    public String lastCompletedDate;
    public boolean isArchived;
    @Ignore
    public boolean isJustSkipped = false;
    @Ignore
    public boolean isCompletedOnSelectedDate = false;
    @Ignore
    public boolean isSkippedOnSelectedDate = false;


    public boolean isCompletedToday() {
        if (lastCompletedDate == null || lastCompletedDate.isEmpty()) {
            return false;
        }
        // Lấy ngày hôm nay format chuỗi
        String todayStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return todayStr.equals(lastCompletedDate);
    }
}
