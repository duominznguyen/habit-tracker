package com.duominz.habittracker.data.repositories;

import android.app.Application;
import android.os.Looper;
import android.os.Handler;

import com.duominz.habittracker.data.DAOs.HabitDao;
import com.duominz.habittracker.data.DAOs.HabitLogDao;
import com.duominz.habittracker.data.database.AppDatabase;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.data.models.HabitLog;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class HabitRepository {
    private final HabitDao habitDao;
    private final HabitLogDao habitLogDao;
    private final ExecutorService executorService;
    private final Handler mainThreadHandler;

    public HabitRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        habitDao = db.habitDao();
        habitLogDao = db.habitLogDao();
        executorService = Executors.newFixedThreadPool(4);
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void insertLog(HabitLog log, Runnable onComplete) {
        executorService.execute(() -> {
            habitLogDao.insert(log);
            recalculateStreakInternal(log.habitId);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void deleteLog(int habitId, String date, Runnable onComplete) {
        executorService.execute(() -> {
            habitLogDao.delete(habitId, date);
            recalculateStreakInternal(habitId);
            if (onComplete != null) {
                mainThreadHandler.post(onComplete);
            }
        });
    }

    public void syncAllStreaks() {
        executorService.execute(() -> {
            List<Habit> habits = habitDao.getAllHabits();
            for (Habit h : habits) {
                recalculateStreakInternal(h.id);
            }
        });
    }

    private void recalculateStreakInternal(int habitId) {
        Habit habit = habitDao.getHabitById(habitId);
        if (habit == null) return;

        List<String> completedDates = habitLogDao.getAllCompletedDates(habitId);
        List<LocalDate> dates = completedDates.stream()
                .map(LocalDate::parse)
                .collect(Collectors.toList());

        int streak = calculateStreakLogic(habit, dates);
        habit.currentStreak = streak;
        if (streak > habit.highestStreak) {
            habit.highestStreak = streak;
        }
        habitDao.update(habit);
    }

    private int calculateStreakLogic(Habit habit, List<LocalDate> completedDates) {
        if (completedDates.isEmpty()) return 0;
        LocalDate today = LocalDate.now();
        int streak = 0;
        LocalDate checkDate = today;
        if (!completedDates.contains(today)) {
            checkDate = getPreviousDueDate(habit, today);
        }
        while (completedDates.contains(checkDate)) {
            streak++;
            checkDate = getPreviousDueDate(habit, checkDate);
        }
        return streak;
    }

    public boolean isHabitDue(Habit habit, LocalDate date) {
        if (habit.startDate != null && date.isBefore(LocalDate.parse(habit.startDate))) return false;
        
        switch (habit.frequencyType) {
            case "Daily": return true;
            case "Weekly":
                if (habit.frequencyValue == null || habit.frequencyValue.isEmpty()) return true;
                List<Integer> targetDays = Arrays.stream(habit.frequencyValue.split(","))
                        .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                
                // Trở lại ISO Day of Week: Mon=1, Tue=2, ..., Sun=7
                int dayOfWeek = date.getDayOfWeek().getValue();
                return targetDays.contains(dayOfWeek);

            case "Monthly":
                if (habit.frequencyValue == null || habit.frequencyValue.isEmpty()) return true;
                List<Integer> targetDates = Arrays.stream(habit.frequencyValue.split(","))
                        .map(String::trim).map(Integer::parseInt).collect(Collectors.toList());
                return targetDates.contains(date.getDayOfMonth());
            default: return true;
        }
    }

    private LocalDate getPreviousDueDate(Habit habit, LocalDate date) {
        LocalDate prev = date.minusDays(1);
        LocalDate startLimit = LocalDate.parse(habit.startDate).minusDays(1);
        while (prev.isAfter(startLimit)) {
            if (isHabitDue(habit, prev)) return prev;
            prev = prev.minusDays(1);
        }
        return prev;
    }

    public void getActiveHabits(OnHabitsLoadedListener listener) {
        executorService.execute(() -> {
            List<Habit> habits = habitDao.getActiveHabits();
            mainThreadHandler.post(() -> listener.onLoaded(habits));
        });
    }

    public interface OnHabitsLoadedListener {
        void onLoaded(List<Habit> habits);
    }
}
