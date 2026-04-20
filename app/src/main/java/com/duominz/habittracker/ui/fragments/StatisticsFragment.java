package com.duominz.habittracker.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.database.AppDatabase;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.data.models.HabitLog;
import com.duominz.habittracker.ui.adapters.WeeklyStatsAdapter;
import com.google.android.material.tabs.TabLayout;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class StatisticsFragment extends Fragment {

    private TabLayout tabLayoutStats;
    private FrameLayout contentPlaceholder;
    
    // Weekly Views
    private View weeklyLayout;
    private TextView tvWeekRange;
    private RecyclerView rvWeeklyHabits;
    private WeeklyStatsAdapter weeklyAdapter;
    private ImageView[] ivBestDays = new ImageView[7];
    
    private LocalDate currentStartOfWeek;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        db = AppDatabase.getInstance(requireContext());
        
        tabLayoutStats = view.findViewById(R.id.tabLayoutStats);
        contentPlaceholder = view.findViewById(R.id.statsContentPlaceholder);

        setupTabs();
        initWeeklyViews(inflater);

        // Mặc định hiển thị Weekly
        showWeeklyStats();

        return view;
    }

    private void setupTabs() {
        tabLayoutStats.addTab(tabLayoutStats.newTab().setText("Weekly"));
        tabLayoutStats.addTab(tabLayoutStats.newTab().setText("Monthly"));
        tabLayoutStats.addTab(tabLayoutStats.newTab().setText("Yearly"));

        tabLayoutStats.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) showWeeklyStats();
                else showPlaceholder(tab.getText().toString());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void initWeeklyViews(LayoutInflater inflater) {
        weeklyLayout = inflater.inflate(R.layout.layout_stats_weekly, null);
        tvWeekRange = weeklyLayout.findViewById(R.id.tvWeekRange);
        rvWeeklyHabits = weeklyLayout.findViewById(R.id.rvWeeklyHabits);
        
        ivBestDays[0] = weeklyLayout.findViewById(R.id.ivBestM);
        ivBestDays[1] = weeklyLayout.findViewById(R.id.ivBestT);
        ivBestDays[2] = weeklyLayout.findViewById(R.id.ivBestW);
        ivBestDays[3] = weeklyLayout.findViewById(R.id.ivBestTh);
        ivBestDays[4] = weeklyLayout.findViewById(R.id.ivBestF);
        ivBestDays[5] = weeklyLayout.findViewById(R.id.ivBestS);
        ivBestDays[6] = weeklyLayout.findViewById(R.id.ivBestSu);

        weeklyAdapter = new WeeklyStatsAdapter();
        rvWeeklyHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWeeklyHabits.setAdapter(weeklyAdapter);

        currentStartOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        weeklyLayout.findViewById(R.id.ivPrevWeek).setOnClickListener(v -> {
            currentStartOfWeek = currentStartOfWeek.minusWeeks(1);
            loadWeeklyData();
        });

        weeklyLayout.findViewById(R.id.ivNextWeek).setOnClickListener(v -> {
            currentStartOfWeek = currentStartOfWeek.plusWeeks(1);
            loadWeeklyData();
        });
    }

    private void showWeeklyStats() {
        contentPlaceholder.removeAllViews();
        contentPlaceholder.addView(weeklyLayout);
        loadWeeklyData();
    }

    private void loadWeeklyData() {
        LocalDate endOfWeek = currentStartOfWeek.plusDays(6);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM");
        tvWeekRange.setText(currentStartOfWeek.format(formatter) + " ~ " + endOfWeek.format(formatter));

        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) weekDates.add(currentStartOfWeek.plusDays(i));

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Habit> allHabits = db.habitDao().getAllHabits();
            Map<Integer, Map<String, Integer>> habitLogsMap = new HashMap<>();

            for (Habit h : allHabits) {
                Map<String, Integer> logs = new HashMap<>();
                List<String> completed = db.habitLogDao().getCompletedDatesInRange(h.id, currentStartOfWeek.toString(), endOfWeek.toString());
                List<String> skipped = db.habitLogDao().getSkippedDatesInRange(h.id, currentStartOfWeek.toString(), endOfWeek.toString());
                
                for (String d : completed) logs.put(d, 1);
                for (String d : skipped) logs.put(d, 2);
                
                habitLogsMap.put(h.id, logs);
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    weeklyAdapter.setData(allHabits, weekDates, habitLogsMap, (dailyBest, habitWeeklyGoals) -> {
                        for (int i = 0; i < 7; i++) {
                            ivBestDays[i].setVisibility(dailyBest[i] ? View.VISIBLE : View.INVISIBLE);
                        }
                    });
                });
            }
        });
    }

    private void showPlaceholder(String title) {
        contentPlaceholder.removeAllViews();
        TextView tv = new TextView(getContext());
        tv.setText("Content for " + title + " coming soon");
        tv.setGravity(android.view.Gravity.CENTER);
        contentPlaceholder.addView(tv);
    }
}