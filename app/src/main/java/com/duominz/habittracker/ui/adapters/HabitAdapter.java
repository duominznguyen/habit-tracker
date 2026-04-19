package com.duominz.habittracker.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.data.models.HabitLog;
import com.duominz.habittracker.data.repositories.HabitRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private List<Habit> habitList = new ArrayList<>();
    private HabitRepository repository;
    private OnHabitActionListener actionListener;

    public interface OnHabitActionListener {
        void onRefreshRequest();
    }

    private String selectedDateStr = LocalDate.now().toString();

    public void setHabits(List<Habit> habits, String dateStr) {
        this.habitList = habits;
        this.selectedDateStr = dateStr;
        notifyDataSetChanged();
    }

    public void setRepository(HabitRepository repository) {
        this.repository = repository;
    }

    public void setActionListener(OnHabitActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit, parent, false);
        HabitViewHolder holder = new HabitViewHolder(view);

        DisplayMetrics metrics = parent.getContext().getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int paddingPx = (int) (32 * metrics.density);
        holder.mainCard.getLayoutParams().width = screenWidth - paddingPx;

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvHabitName.setText(habit.habitName);
        
        // hiển thị icon(drawable) tương ứng với tên icon trong db
        if (habit.icon != null && !habit.icon.isEmpty()) {
            int resId = context.getResources().getIdentifier(habit.icon, "drawable", context.getPackageName());
            if (resId != 0) {
                holder.ivHabitIcon.setImageResource(resId);
            } else {
                holder.ivHabitIcon.setImageResource(R.drawable.ic_book); // Fallback nếu không tìm thấy
            }
        } else {
            holder.ivHabitIcon.setImageResource(R.drawable.ic_book); 
        }

        // chir hiển thị streak nếu là ngày hôm nay
        String todayStr = LocalDate.now().toString();
        if (habit.currentStreak > 0 && selectedDateStr.equals(todayStr)) {
            holder.tvStreak.setVisibility(View.VISIBLE);
            holder.tvStreak.setText("🔥 " + habit.currentStreak);
        } else {
            holder.tvStreak.setVisibility(View.GONE);
        }

        boolean isDone = habit.isCompletedOnSelectedDate || habit.isSkippedOnSelectedDate;
        boolean isFutureDate = LocalDate.parse(selectedDateStr).isAfter(LocalDate.now());

        String baseColor = habit.displayColor != null ? habit.displayColor : "#E3F2FD";
        holder.mainCard.setBackgroundResource(R.drawable.bg_habit_card);
        android.graphics.drawable.GradientDrawable drawable =
                (android.graphics.drawable.GradientDrawable) holder.mainCard.getBackground().mutate();

        if (isDone) {
            drawable.setColor(Color.parseColor(baseColor));
        } else {
            String lightColor = "#14" + baseColor.replace("#", "");
            drawable.setColor(Color.parseColor(lightColor));
        }

        holder.hsvContainer.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                holder.hsvContainer.post(() -> {
                    int scrollX = holder.hsvContainer.getScrollX();
                    int menuWidth = holder.menuLayout.getWidth();
                    if (scrollX > menuWidth / 2) {
                        holder.hsvContainer.smoothScrollTo(menuWidth, 0);
                    } else {
                        holder.hsvContainer.smoothScrollTo(0, 0);
                    }
                });
                return true;
            }
            return false;
        });

        holder.btnSkip.setOnClickListener(v -> {
            holder.hsvContainer.smoothScrollTo(0, 0);
            if (isFutureDate) {
                Toast.makeText(v.getContext(), "Không thể thao tác với tương lai!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isDone && repository != null) {
                repository.insertLog(new HabitLog(habit.id, selectedDateStr, true), () -> {
                    if (actionListener != null) actionListener.onRefreshRequest();
                });
            }
        });

        holder.btnReset.setOnClickListener(v -> {
            holder.hsvContainer.smoothScrollTo(0, 0);
            if (isFutureDate) {
                Toast.makeText(v.getContext(), "Không thể thao tác với tương lai!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isDone && repository != null) {
                repository.deleteLog(habit.id, selectedDateStr, () -> {
                    if (actionListener != null) actionListener.onRefreshRequest();
                });
            }
        });

        holder.mainCard.setOnClickListener(v -> {
            if (isFutureDate) {
                Toast.makeText(v.getContext(), "Không thể thao tác với tương lai!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isDone && repository != null) {
                repository.insertLog(new HabitLog(habit.id, selectedDateStr, false), () -> {
                    if (actionListener != null) actionListener.onRefreshRequest();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        HorizontalScrollView hsvContainer;
        LinearLayout mainCard, menuLayout, btnSkip, btnReset;
        TextView tvHabitName, tvStreak;
        ImageView ivHabitIcon;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            hsvContainer = itemView.findViewById(R.id.hsvContainer);
            mainCard = itemView.findViewById(R.id.main_card);
            menuLayout = itemView.findViewById(R.id.menu_layout);
            btnSkip = itemView.findViewById(R.id.btnSkip);
            btnReset = itemView.findViewById(R.id.btnReset);
            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvStreak = itemView.findViewById(R.id.tvStreak);
            ivHabitIcon = itemView.findViewById(R.id.ivHabitIcon);
        }
    }
}
