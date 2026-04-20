package com.duominz.habittracker.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.models.Habit;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyStatsAdapter extends RecyclerView.Adapter<WeeklyStatsAdapter.ViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private List<LocalDate> weekDates = new ArrayList<>();
    // habitId -> (date -> status) : status: 0=None, 1=Completed, 2=Skipped
    private Map<Integer, Map<String, Integer>> habitLogs = new HashMap<>();
    private OnWeekStatusChangeListener weekStatusListener;

    public interface OnWeekStatusChangeListener {
        void onStatusCalculated(boolean[] dailyBest, boolean[] habitWeeklyGoals);
    }

    public void setData(List<Habit> habits, List<LocalDate> weekDates, Map<Integer, Map<String, Integer>> habitLogs, OnWeekStatusChangeListener listener) {
        this.habits = habits;
        this.weekDates = weekDates;
        this.habitLogs = habitLogs;
        this.weekStatusListener = listener;
        notifyDataSetChanged();
        calculateAggregates();
    }

    private void calculateAggregates() {
        if (weekDates.size() < 7 || habits.isEmpty()) return;

        boolean[] dailyBest = new boolean[7];
        boolean[] habitWeeklyGoals = new boolean[habits.size()];

        for (int d = 0; d < 7; d++) {
            LocalDate date = weekDates.get(d);
            boolean allDoneForDay = true;
            boolean hasAtLeastOneHabit = false;

            for (Habit h : habits) {
                if (isHabitRequired(h, date)) {
                    hasAtLeastOneHabit = true;
                    int status = getStatus(h.id, date.toString());
                    if (status == 0) {
                        allDoneForDay = false;
                        break;
                    }
                }
            }
            dailyBest[d] = hasAtLeastOneHabit && allDoneForDay;
        }

        for (int i = 0; i < habits.size(); i++) {
            Habit h = habits.get(i);
            boolean allDoneForWeek = true;
            boolean hasAtLeastOneDay = false;

            for (LocalDate date : weekDates) {
                if (isHabitRequired(h, date)) {
                    hasAtLeastOneDay = true;
                    int status = getStatus(h.id, date.toString());
                    if (status == 0) {
                        allDoneForWeek = false;
                        break;
                    }
                }
            }
            habitWeeklyGoals[i] = hasAtLeastOneDay && allDoneForWeek;
        }

        if (weekStatusListener != null) {
            weekStatusListener.onStatusCalculated(dailyBest, habitWeeklyGoals);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weekly_habit_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvName.setText(habit.habitName);
        
        int iconRes = holder.itemView.getContext().getResources().getIdentifier(habit.icon, "drawable", holder.itemView.getContext().getPackageName());
        holder.ivIcon.setImageResource(iconRes != 0 ? iconRes : R.drawable.ic_book);

        ImageView[] cells = {holder.cell1, holder.cell2, holder.cell3, holder.cell4, holder.cell5, holder.cell6, holder.cell7};
        
        boolean allDone = true;
        boolean hasRequiredDay = false;

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekDates.get(i);
            boolean isRequired = isHabitRequired(habit, date);
            int status = getStatus(habit.id, date.toString());

            if (!isRequired) {
                // Trường hợp KHÔNG CẦN LÀM: Hiện icon gạch chéo xám, không có nền
                cells[i].setImageResource(R.drawable.ic_not_required);
                cells[i].setBackground(null); // Xóa màu nền
            } else {
                // Trường hợp CẦN LÀM: Ẩn icon, hiện màu trạng thái
                hasRequiredDay = true;
                cells[i].setImageResource(0);
                updateCellColor(cells[i], status, habit.displayColor);
                if (status == 0) allDone = false;
            }
        }

        holder.ivGoal.setVisibility((hasRequiredDay && allDone) ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateCellColor(ImageView cell, int status, String habitColorHex) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(12f);
        
        if (status == 1) { // Completed
            shape.setColor(Color.parseColor(habitColorHex));
        } else if (status == 2) { // Skipped
            int habitColor = Color.parseColor(habitColorHex);
            int fadedColor = Color.argb(100, Color.red(habitColor), Color.green(habitColor), Color.blue(habitColor));
            shape.setColor(fadedColor);
        } else { // Not done (status 0)
            shape.setColor(Color.parseColor("#E0E0E0"));
        }
        cell.setBackground(shape);
    }

    private boolean isHabitRequired(Habit habit, LocalDate date) {
        if (habit.startDate != null && date.isBefore(LocalDate.parse(habit.startDate))) return false;
        if (habit.endDate != null && date.isAfter(LocalDate.parse(habit.endDate))) return false;

        switch (habit.frequencyType) {
            case "Daily": return true;
            case "Weekly":
                if (habit.frequencyValue == null || habit.frequencyValue.isEmpty()) return true;
                String[] days = habit.frequencyValue.split(",");
                int dayOfWeek = date.getDayOfWeek().getValue();
                for (String d : days) {
                    if (Integer.parseInt(d.trim()) == dayOfWeek) return true;
                }
                return false;
            case "Monthly":
                if (habit.frequencyValue == null || habit.frequencyValue.isEmpty()) return true;
                String[] dates = habit.frequencyValue.split(",");
                int dayOfMonth = date.getDayOfMonth();
                for (String d : dates) {
                    if (Integer.parseInt(d.trim()) == dayOfMonth) return true;
                }
                return false;
            default: return true;
        }
    }

    private int getStatus(int habitId, String date) {
        Map<String, Integer> logs = habitLogs.get(habitId);
        if (logs != null && logs.containsKey(date)) {
            return logs.get(date);
        }
        return 0;
    }

    @Override
    public int getItemCount() { return habits.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageView ivIcon, ivGoal;
        ImageView cell1, cell2, cell3, cell4, cell5, cell6, cell7;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvWeeklyHabitName);
            ivIcon = itemView.findViewById(R.id.ivWeeklyHabitIcon);
            ivGoal = itemView.findViewById(R.id.ivWeeklyGoal);
            cell1 = itemView.findViewById(R.id.cellDay1);
            cell2 = itemView.findViewById(R.id.cellDay2);
            cell3 = itemView.findViewById(R.id.cellDay3);
            cell4 = itemView.findViewById(R.id.cellDay4);
            cell5 = itemView.findViewById(R.id.cellDay5);
            cell6 = itemView.findViewById(R.id.cellDay6);
            cell7 = itemView.findViewById(R.id.cellDay7);
        }
    }
}