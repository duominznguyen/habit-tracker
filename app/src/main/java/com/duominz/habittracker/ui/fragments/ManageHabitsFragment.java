package com.duominz.habittracker.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.database.AppDatabase;
import com.duominz.habittracker.data.models.Habit;
import com.duominz.habittracker.ui.adapters.ManageHabitsAdapter;

import java.util.List;
import java.util.concurrent.Executors;

public class ManageHabitsFragment extends Fragment implements ManageHabitsAdapter.OnHabitActionListener {

    private RecyclerView rvManageHabits;
    private ManageHabitsAdapter adapter;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_habits, container, false);

        rvManageHabits = view.findViewById(R.id.rvManageHabits);
        db = AppDatabase.getInstance(requireContext());

        setupRecyclerView();
        loadData();

        return view;
    }

    private void setupRecyclerView() {
        rvManageHabits.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManageHabitsAdapter();
        rvManageHabits.setAdapter(adapter);
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Habit> activeHabits = db.habitDao().getActiveHabits();
            List<Habit> archivedHabits = db.habitDao().getArchivedHabits();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    adapter.setData(activeHabits, archivedHabits, this);
                });
            }
        });
    }

    @Override
    public void onEdit(Habit habit) {
        EditHabitFragment editFragment = EditHabitFragment.newInstance(habit.id);
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, editFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onArchive(Habit habit) {
        String title = habit.isArchived ? "Khôi phục" : "Lưu trữ";
        String message = habit.isArchived ? 
            "Bạn có muốn bỏ lưu trữ thói quen này không?" :
            "Bạn muốn lưu trữ thói quen này không? Nó sẽ không được hiện thị trong màn hình chính nữa.";

        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        habit.isArchived = !habit.isArchived;
                        db.habitDao().update(habit);
                        loadData();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onDelete(Habit habit) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa thói quen")
                .setMessage("Bạn chắc chắn muốn xóa thói quen này chứ? Điều này sẽ xóa hết lịch sử liên quan đến thói quen \"" + habit.habitName + "\". Hành động này không thể khôi phục.")
                .setPositiveButton("Xác nhận xóa", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        db.habitDao().delete(habit);
                        loadData();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
