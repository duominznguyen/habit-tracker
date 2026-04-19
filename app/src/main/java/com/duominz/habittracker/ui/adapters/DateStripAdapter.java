package com.duominz.habittracker.ui.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DateStripAdapter extends RecyclerView.Adapter<DateStripAdapter.WeekViewHolder> {

    private List<List<LocalDate>> weeks;
    private LocalDate selectedDate;
    private final OnDateSelectedListener listener;
    private Map<LocalDate, Integer> progressCache = new HashMap<>();

    public interface OnDateSelectedListener {
        void onDateSelected(LocalDate date);
    }

    public DateStripAdapter(List<List<LocalDate>> weeks, LocalDate defaultSelectedDate, OnDateSelectedListener listener) {
        this.weeks = weeks;
        this.selectedDate = defaultSelectedDate;
        this.listener = listener;
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Load layout của 1 TUẦN (item_week)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_week, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        List<LocalDate> daysInWeek = weeks.get(position);
        LocalDate today = LocalDate.now();

        // Lặp qua 7 ngày trong tuần và gắn dữ liệu vào 7 cái ô con
        for (int i = 0; i < 7; i++) {
            LocalDate date = daysInWeek.get(i);
            View dayView = holder.dayViews[i]; // Lấy view con tương ứng

            // Ánh xạ lại các thành phần bên trong view con (item_date)
            TextView tvDayOfWeek = dayView.findViewById(R.id.tvDayOfWeek);
            TextView tvDayOfMonth = dayView.findViewById(R.id.tvDayOfMonth);
            LinearLayout layoutPill = dayView.findViewById(R.id.layoutPill);
            ProgressBar pbDateProgress = dayView.findViewById(R.id.pbDateProgress);

            // Gắn dữ liệu
            tvDayOfWeek.setText(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH).substring(0, 2));
            tvDayOfMonth.setText(String.valueOf(date.getDayOfMonth()));

            // Tiến độ %
            pbDateProgress.setProgress(progressCache.getOrDefault(date, 0));

            // Hiệu ứng đánh dấu ngày đang chọn
            if (date.equals(selectedDate)) {
                // TRƯỜNG HỢP 1: Ngày ĐANG ĐƯỢC CHỌN
                layoutPill.setBackgroundResource(R.drawable.bg_date_pill);
                tvDayOfWeek.setTextColor(Color.parseColor("#000000"));

            } else if (date.equals(today)) {
                // TRƯỜNG HỢP 2: Ngày HÔM NAY (Nhưng user đang chọn ngày khác)
                layoutPill.setBackgroundResource(R.drawable.bg_date_today);

            } else {
                // TRƯỜNG HỢP 3: Ngày BÌNH THƯỜNG
                layoutPill.setBackground(null);
                tvDayOfWeek.setTextColor(Color.parseColor("#666666"));
            }

            // Bắt sự kiện click
            dayView.setOnClickListener(v -> {
                selectedDate = date;
                notifyDataSetChanged(); // vẽ lại toàn bộ danh sách để cập nhật đánh dấu ngày đã chọn
                listener.onDateSelected(date);
            });
        }
    }

    @Override
    public int getItemCount() {
        return weeks.size();
    }

    public void updateProgressForDate(LocalDate date, int percentage) {
        progressCache.put(date, percentage);
        notifyDataSetChanged();
    }

    public static class WeekViewHolder extends RecyclerView.ViewHolder {
        View[] dayViews = new View[7];

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            LinearLayout weekLayout = (LinearLayout) itemView;

            // Gen ra 7 item_date cho item_week
            for (int i = 0; i < 7; i++) {
                View dayView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.item_date, weekLayout, false);
                weekLayout.addView(dayView);
                dayViews[i] = dayView;
            }
        }
    }
}