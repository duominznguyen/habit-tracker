package com.duominz.habittracker.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.models.Habit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonthlyStatsAdapter extends RecyclerView.Adapter<MonthlyStatsAdapter.ViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private Map<Integer, Map<String, Integer>> logsMap;
    private LocalDate firstDayOfMonth;

    public void setData(List<Habit> habits, LocalDate firstDayOfMonth, Map<Integer, Map<String, Integer>> logsMap) {
        this.habits = habits;
        this.firstDayOfMonth = firstDayOfMonth;
        this.logsMap = logsMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_habit_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvHabitName.setText(habit.habitName);
        int resId = holder.itemView.getContext().getResources().getIdentifier(habit.icon, "drawable", holder.itemView.getContext().getPackageName());
        holder.ivIcon.setImageResource(resId != 0 ? resId : R.drawable.ic_book);

        Map<String, Integer> habitLogs = logsMap.get(habit.id);
        
        int daysInMonth = firstDayOfMonth.lengthOfMonth();
        int completedCount = 0;
        if (habitLogs != null) {
            for (Integer status : habitLogs.values()) {
                if (status == 1) completedCount++;
            }
        }
        
        holder.tvTotalCompleted.setText(completedCount + "d");
        float percent = (completedCount * 100f) / daysInMonth;
        holder.tvPercent.setText(String.format("%.1f%%", percent));

        CalendarAdapter calendarAdapter = new CalendarAdapter(firstDayOfMonth, habitLogs, habit.displayColor);
        holder.rvCalendar.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 7));
        holder.rvCalendar.setAdapter(calendarAdapter);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvHabitName, tvPercent, tvTotalCompleted;
        RecyclerView rvCalendar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivMonthlyHabitIcon);
            tvHabitName = itemView.findViewById(R.id.tvMonthlyHabitName);
            tvPercent = itemView.findViewById(R.id.tvCompletionRate);
            tvTotalCompleted = itemView.findViewById(R.id.tvTotalCompletedDays);
            rvCalendar = itemView.findViewById(R.id.rvMonthlyCalendar);
        }
    }

    private static class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CellViewHolder> {
        private final LocalDate month;
        private final Map<String, Integer> logs;
        private final String habitColor;
        private final int startOffset;

        public CalendarAdapter(LocalDate month, Map<String, Integer> logs, String habitColor) {
            this.month = month;
            this.logs = logs;
            this.habitColor = habitColor;
            int firstDayOfWeek = month.withDayOfMonth(1).getDayOfWeek().getValue(); 
            this.startOffset = firstDayOfWeek - 1; 
        }

        @NonNull
        @Override
        public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monthly_calendar_cell, parent, false);
            return new CellViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CellViewHolder holder, int position) {
            if (position < startOffset || position >= (startOffset + month.lengthOfMonth())) {
                holder.viewStatus.setBackground(null);
                holder.tvDayNum.setVisibility(View.GONE);
                return;
            }

            int dayNum = position - startOffset + 1;
            holder.tvDayNum.setText(String.valueOf(dayNum));
            holder.tvDayNum.setVisibility(View.VISIBLE);
            holder.tvDayNum.setTextColor(Color.WHITE);

            LocalDate date = month.withDayOfMonth(dayNum);
            String dateStr = date.toString();
            Integer status = (logs != null) ? logs.get(dateStr) : null;

            // Luôn đảm bảo dùng đúng drawable có bo viền
            holder.viewStatus.setBackgroundResource(R.drawable.bg_stats_cell_default);

            if (status != null) {
                int color;
                if (status == 1) { // Completed
                    color = Color.parseColor(habitColor);
                } else { // Skipped (status == 2)
                    int baseColor = Color.parseColor(habitColor);
                    color = Color.argb(80, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
                }
                holder.viewStatus.setBackgroundTintList(ColorStateList.valueOf(color));
            } else {
                // Không có log - Màu xám mặc định
                holder.viewStatus.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            }
        }

        @Override
        public int getItemCount() {
            return 42;
        }

        static class CellViewHolder extends RecyclerView.ViewHolder {
            View viewStatus;
            TextView tvDayNum;
            public CellViewHolder(@NonNull View itemView) {
                super(itemView);
                viewStatus = itemView.findViewById(R.id.viewStatus);
                tvDayNum = itemView.findViewById(R.id.tvDayNum);
            }
        }
    }
}
