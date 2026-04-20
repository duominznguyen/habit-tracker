package com.duominz.habittracker.ui.adapters;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class YearlyStatsAdapter extends RecyclerView.Adapter<YearlyStatsAdapter.ViewHolder> {

    private List<Habit> habits = new ArrayList<>();
    private Map<Integer, Map<String, Integer>> logsMap;
    private int currentYear;

    public void setData(List<Habit> habits, int year, Map<Integer, Map<String, Integer>> logsMap) {
        this.habits = habits;
        this.currentYear = year;
        this.logsMap = logsMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_yearly_habit_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.tvHabitName.setText(habit.habitName);
        int resId = holder.itemView.getContext().getResources().getIdentifier(habit.icon, "drawable", holder.itemView.getContext().getPackageName());
        holder.ivIcon.setImageResource(resId != 0 ? resId : R.drawable.ic_book);

        Map<String, Integer> habitLogs = logsMap.get(habit.id);
        
        LocalDate firstDay = LocalDate.of(currentYear, 1, 1);
        int daysInYear = firstDay.lengthOfYear();
        int completedCount = 0;
        if (habitLogs != null) {
            for (Integer status : habitLogs.values()) {
                if (status == 1) completedCount++;
            }
        }
        
        holder.tvTotalDays.setText(completedCount + "d");
        float percent = (completedCount * 100f) / daysInYear;
        holder.tvPercent.setText(String.format("%.1f%%", percent));

        ContributionAdapter adapter = new ContributionAdapter(currentYear, habitLogs, habit.displayColor);
        holder.rvContribution.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 7, GridLayoutManager.HORIZONTAL, false));
        holder.rvContribution.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvHabitName, tvPercent, tvTotalDays;
        RecyclerView rvContribution;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivYearlyHabitIcon);
            tvHabitName = itemView.findViewById(R.id.tvYearlyHabitName);
            tvPercent = itemView.findViewById(R.id.tvYearlyPercent);
            tvTotalDays = itemView.findViewById(R.id.tvYearlyTotalDays);
            rvContribution = itemView.findViewById(R.id.rvYearlyContribution);
        }
    }

    private static class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.CellViewHolder> {
        private final int year;
        private final Map<String, Integer> logs;
        private final String habitColor;
        private final int daysInYear;
        private final String EMPTY_COLOR = "#F1F1F1";

        public ContributionAdapter(int year, Map<String, Integer> logs, String habitColor) {
            this.year = year;
            this.logs = logs;
            this.habitColor = habitColor;
            this.daysInYear = LocalDate.of(year, 1, 1).lengthOfYear();
        }

        @NonNull
        @Override
        public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new View(parent.getContext());
            float density = parent.getContext().getResources().getDisplayMetrics().density;

            // TÍNH TOÁN CHO MÀN HÌNH 412dp:
            // Chiều ngang khả dụng (đã thu hẹp padding layout): 412 - 24 - 4 = 384dp
            // Để dàn đều 53 cột trên 384dp: 384 / 53 = 7.24dp mỗi cột
            // Dùng margin = 0.5dp (khe hở 1.0dp) để không bị díu.
            // Suy ra kích thước ô s = 7.24 - 1.0 = 6.24dp

            int size = Math.round(6.24f * density);
            int margin = Math.round(0.5f * density);

            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(size, size);
            params.setMargins(margin, margin, margin, margin);
            view.setLayoutParams(params);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.RECTANGLE);
            shape.setCornerRadius(Math.round(1f * density));
            shape.setColor(Color.parseColor(EMPTY_COLOR));
            view.setBackground(shape);
            
            return new CellViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CellViewHolder holder, int position) {
            LocalDate date = LocalDate.of(year, 1, 1).plusDays(position);
            String dateStr = date.toString();
            Integer status = (logs != null) ? logs.get(dateStr) : null;

            if (status != null) {
                int color;
                if (status == 1) {
                    color = Color.parseColor(habitColor);
                } else {
                    int baseColor = Color.parseColor(habitColor);
                    color = Color.argb(80, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
                }
                holder.itemView.setBackgroundTintList(ColorStateList.valueOf(color));
            } else {
                holder.itemView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(EMPTY_COLOR)));
            }
        }

        @Override
        public int getItemCount() {
            return daysInYear;
        }

        static class CellViewHolder extends RecyclerView.ViewHolder {
            public CellViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }
}
