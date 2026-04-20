package com.duominz.habittracker.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.models.Habit;

import java.util.ArrayList;
import java.util.List;

public class ManageHabitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_HABIT = 1;

    private List<Object> items = new ArrayList<>();
    private OnHabitActionListener listener;

    public interface OnHabitActionListener {
        void onEdit(Habit habit);
        void onArchive(Habit habit);
        void onDelete(Habit habit);
    }

    public void setData(List<Habit> activeHabits, List<Habit> archivedHabits, OnHabitActionListener listener) {
        this.listener = listener;
        items.clear();
        items.addAll(activeHabits);
        if (!archivedHabits.isEmpty()) {
            items.add("Đã lưu trữ"); // String as Header
            items.addAll(archivedHabits);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_HABIT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_habit, parent, false);
            return new HabitViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvHeaderTitle.setText((String) items.get(position));
        } else if (holder instanceof HabitViewHolder) {
            Habit habit = (Habit) items.get(position);
            ((HabitViewHolder) holder).bind(habit, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon, ivMore;
        TextView tvName;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivHabitIcon);
            tvName = itemView.findViewById(R.id.tvHabitName);
            ivMore = itemView.findViewById(R.id.ivMoreMenu);
        }

        public void bind(Habit habit, OnHabitActionListener listener) {
            tvName.setText(habit.habitName);
            Context context = itemView.getContext();
            int resId = context.getResources().getIdentifier(habit.icon, "drawable", context.getPackageName());
            ivIcon.setImageResource(resId != 0 ? resId : R.drawable.ic_book);

            ivMore.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(context, ivMore);
                popup.getMenu().add("Chỉnh sửa");
                popup.getMenu().add(habit.isArchived ? "Khôi phục" : "Lưu trữ");
                popup.getMenu().add("Xóa");

                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Chỉnh sửa")) {
                        listener.onEdit(habit);
                    } else if (item.getTitle().equals("Lưu trữ") || item.getTitle().equals("Khôi phục")) {
                        listener.onArchive(habit);
                    } else if (item.getTitle().equals("Xóa")) {
                        listener.onDelete(habit);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }
}
