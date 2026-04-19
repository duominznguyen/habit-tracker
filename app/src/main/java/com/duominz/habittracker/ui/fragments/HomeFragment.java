package com.duominz.habittracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.database.AppDatabase;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.data.models.HabitLog;
import com.duominz.habittracker.data.repositories.HabitRepository;
import com.duominz.habittracker.ui.adapters.DateStripAdapter;
import com.duominz.habittracker.ui.adapters.HabitAdapter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    // Lịch (Top)
    private RecyclerView rvDateStrip;
    private DateStripAdapter dateAdapter;
    private LocalDate selectedDate;
    private RecyclerView rvHabits;
    private HabitAdapter habitAdapter;

    // Database & Repository
    private AppDatabase db;
    private HabitRepository habitRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Ánh xạ View
        rvDateStrip = view.findViewById(R.id.rvDateStrip);
        rvHabits = view.findViewById(R.id.rvHabits);

        // init bd với repo
        db = AppDatabase.getInstance(requireContext());
        habitRepository = new HabitRepository(requireActivity().getApplication());

        // sync streaks sau khi mở app
        habitRepository.syncAllStreaks();

        // Setup các danh sách
        setupHabitList();
        setupDateStrip();

        return view;
    }

    private void setupHabitList() {
        rvHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        habitAdapter = new HabitAdapter();
        habitAdapter.setRepository(habitRepository);
        
        habitAdapter.setActionListener(() -> {
            if (selectedDate != null) {
                loadHabitsForDate(selectedDate);
            }
        });
        
        rvHabits.setAdapter(habitAdapter);
    }

    private void setupDateStrip() {
        List<List<LocalDate>> weeksList = new ArrayList<>();
        LocalDate today = LocalDate.now();
        selectedDate = today;

        LocalDate startOfCurrentWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        for (int w = -5; w <= 5; w++) {
            List<LocalDate> week = new ArrayList<>();
            LocalDate startOfWeek = startOfCurrentWeek.plusWeeks(w);
            for (int d = 0; d < 7; d++) {
                week.add(startOfWeek.plusDays(d));
            }
            weeksList.add(week);
        }
        int currentWeekPosition = 5;

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvDateStrip.setLayoutManager(layoutManager);

        SnapHelper snapHelper = new PagerSnapHelper();
        rvDateStrip.setOnFlingListener(null);
        snapHelper.attachToRecyclerView(rvDateStrip);

        loadHabitsForDate(selectedDate);

        dateAdapter = new DateStripAdapter(weeksList, today, date -> {
            selectedDate = date;
            loadHabitsForDate(selectedDate);
        });

        rvDateStrip.setAdapter(dateAdapter);
        rvDateStrip.scrollToPosition(currentWeekPosition);
    }

    private void loadHabitsForDate(LocalDate date) {
        String dateString = date.toString();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Habit> allHabitsFromDb = db.habitDao().getAllHabits();
            List<HabitLog> dayLogs = db.habitLogDao().getLogsByDate(dateString);

            // danh sách habit (đã lọc theo frequencyType và frequencyValue để không hiển thị thừa)
            List<Habit> filteredHabits = new ArrayList<>();

            for (Habit habit : allHabitsFromDb) {
                // chỉ thêm vào ds nếu đến hạn hoặc đã có log(Ví dụ: đã hoàn thành ngày hôm đó)
                boolean isDue = habitRepository.isHabitDue(habit, date);
                
                // Tìm xem có log không
                HabitLog existingLog = null;
                for (HabitLog log : dayLogs) {
                    if (log.habitId == habit.id) {
                        existingLog = log;
                        break;
                    }
                }

                // Logic: Hiển thị nếu đến hạn HOẶC nếu đã lỡ có log rồi (để người dùng có thể Reset nếu muốn)
                if (isDue || existingLog != null) {
                    habit.isCompletedOnSelectedDate = false;
                    habit.isSkippedOnSelectedDate = false;

                    if (existingLog != null) {
                        if (existingLog.isSkipped) {
                            habit.isSkippedOnSelectedDate = true;
                        } else {
                            habit.isCompletedOnSelectedDate = true;
                        }
                    }
                    filteredHabits.add(habit);
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    habitAdapter.setHabits(filteredHabits, dateString);
                });
            }
        });
    }
}
