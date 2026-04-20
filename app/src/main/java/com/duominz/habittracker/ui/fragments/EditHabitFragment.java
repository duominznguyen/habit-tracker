package com.duominz.habittracker.ui.fragments;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duominz.habittracker.R;
import com.duominz.habittracker.data.database.AppDatabase;
import com.duominz.habittracker.data.models.Habit;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class EditHabitFragment extends Fragment {

    private static final String ARG_HABIT_ID = "habit_id";

    private EditText etHabitName;
    private ImageView ivHabitIcon;
    private TextView tvStartDateValue, tvEndDateValue, tvSelectedTime;
    private View viewSelectedColorPreview;
    private LinearLayout layoutColorPickerTrigger, layoutWeeklySelection, layoutTimePickerTrigger;
    private LinearLayout layoutWeeklySelectionContainer, layoutMonthlySelectionContainer;
    private LinearLayout layoutStartDateTrigger, layoutEndDateTrigger;
    private Button btnSave, btnSelectAllMonthly;
    private Button btnFreqDaily, btnFreqWeekly, btnFreqMonthly;
    private RecyclerView rvMonthlyPicker;
    private SwitchCompat swReminder;
    private TextView tvLockedWarning;

    private AppDatabase db;
    private Habit habitToEdit;
    private int habitId;
    private boolean isDataLocked = false;

    private final String[] COLOR_PALETTE = {
            "#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF",
            "#40C4FF", "#18FFFF", "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41",
            "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40", "#A1887F", "#BDBDBD",
            "#90A4AE", "#36656B", "#FF1744", "#F50057", "#D500F9", "#651FFF",
            "#3D5AFE", "#2979FF", "#00B0FF", "#00E5FF", "#1DE9B6", "#00E676"
    };

    private final String[] ICON_LIST = {
            "ic_book", "ic_apple", "ic_water", "ic_briefcase", "ic_broom", "ic_brush",
            "ic_camera", "ic_code", "ic_coffee", "ic_dog", "ic_money", "ic_food",
            "ic_family_home", "ic_fitness", "ic_heart", "ic_laptop", "ic_music", "ic_phone",
            "ic_pill", "ic_run", "ic_bedtime", "ic_sprout", "ic_study", "ic_sun",
            "ic_walk", "ic_ridebike", "ic_explore", "ic_laundry", "ic_flight", "ic_camping"
    };

    private String selectedColor;
    private String selectedFrequency;
    private final List<Integer> selectedWeeklyDays = new ArrayList<>();
    private final List<Integer> selectedMonthlyDates = new ArrayList<>();
    private LocalDate selectedStartDate;
    private LocalDate selectedEndDate;
    private String selectedTime;
    private String selectedIconName;

    public static EditHabitFragment newInstance(int habitId) {
        EditHabitFragment fragment = new EditHabitFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_HABIT_ID, habitId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            habitId = getArguments().getInt(ARG_HABIT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_habit, container, false);
        db = AppDatabase.getInstance(requireContext());
        initViews(view);
        loadHabitData();
        return view;
    }

    private void initViews(View view) {
        ivHabitIcon = view.findViewById(R.id.ivHabitIcon);
        etHabitName = view.findViewById(R.id.etHabitName);
        layoutColorPickerTrigger = view.findViewById(R.id.layoutColorPickerTrigger);
        viewSelectedColorPreview = view.findViewById(R.id.viewSelectedColorPreview);
        btnFreqDaily = view.findViewById(R.id.btnFreqDaily);
        btnFreqWeekly = view.findViewById(R.id.btnFreqWeekly);
        btnFreqMonthly = view.findViewById(R.id.btnFreqMonthly);
        layoutWeeklySelectionContainer = view.findViewById(R.id.layoutWeeklySelectionContainer);
        layoutWeeklySelection = view.findViewById(R.id.layoutWeeklySelection);
        layoutMonthlySelectionContainer = view.findViewById(R.id.layoutMonthlySelectionContainer);
        rvMonthlyPicker = view.findViewById(R.id.rvMonthlyPicker);
        btnSelectAllMonthly = view.findViewById(R.id.btnSelectAllMonthly);
        layoutTimePickerTrigger = view.findViewById(R.id.layoutTimePickerTrigger);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        swReminder = view.findViewById(R.id.swReminder);
        layoutStartDateTrigger = view.findViewById(R.id.layoutStartDateTrigger);
        tvStartDateValue = view.findViewById(R.id.tvStartDateValue);
        layoutEndDateTrigger = view.findViewById(R.id.layoutEndDateTrigger);
        tvEndDateValue = view.findViewById(R.id.tvEndDateValue);
        btnSave = view.findViewById(R.id.btnSave);
        tvLockedWarning = view.findViewById(R.id.tvLockedWarning);

        btnSave.setOnClickListener(v -> saveHabit());
    }

    private void loadHabitData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            habitToEdit = db.habitDao().getHabitById(habitId);
            int logCount = db.habitLogDao().getTotalCompletedDays(habitId);
            isDataLocked = logCount > 0;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (habitToEdit != null) bindDataToViews();
                    else getParentFragmentManager().popBackStack();
                });
            }
        });
    }

    private void bindDataToViews() {
        etHabitName.setText(habitToEdit.habitName);
        selectedColor = habitToEdit.displayColor;
        selectedIconName = habitToEdit.icon;
        selectedFrequency = habitToEdit.frequencyType;
        selectedStartDate = LocalDate.parse(habitToEdit.startDate);
        selectedEndDate = (habitToEdit.endDate == null || habitToEdit.endDate.equals("2099-12-31")) ? null : LocalDate.parse(habitToEdit.endDate);
        selectedTime = habitToEdit.reminderTime != null ? habitToEdit.reminderTime : "08:00";

        int resId = getResources().getIdentifier(selectedIconName, "drawable", requireContext().getPackageName());
        ivHabitIcon.setImageResource(resId != 0 ? resId : R.drawable.ic_book);
        
        parseFrequencyValue();
        updateUIState();
        setupInteractions();
    }

    private void parseFrequencyValue() {
        selectedWeeklyDays.clear();
        selectedMonthlyDates.clear();
        if (habitToEdit.frequencyValue != null && !habitToEdit.frequencyValue.isEmpty()) {
            String[] values = habitToEdit.frequencyValue.split(",");
            if ("Weekly".equals(selectedFrequency)) {
                for (String v : values) selectedWeeklyDays.add(Integer.parseInt(v.trim()));
            } else if ("Monthly".equals(selectedFrequency)) {
                for (String v : values) selectedMonthlyDates.add(Integer.parseInt(v.trim()));
            }
        }
    }

    private void updateUIState() {
        updateDynamicColors();
        selectFrequency(selectedFrequency);
        tvStartDateValue.setText(selectedStartDate.toString());
        tvEndDateValue.setText(selectedEndDate == null ? "Không" : selectedEndDate.toString());
        tvSelectedTime.setText(selectedTime);
        swReminder.setChecked(habitToEdit.isReminderEnabled);
        layoutTimePickerTrigger.setVisibility(habitToEdit.isReminderEnabled ? View.VISIBLE : View.GONE);

        if (isDataLocked) {
            btnFreqDaily.setEnabled(false);
            btnFreqWeekly.setEnabled(false);
            btnFreqMonthly.setEnabled(false);
            layoutStartDateTrigger.setEnabled(false);
            layoutEndDateTrigger.setEnabled(false);
            tvLockedWarning.setVisibility(View.VISIBLE);
            float alpha = 0.5f;
            btnFreqDaily.setAlpha(alpha);
            btnFreqWeekly.setAlpha(alpha);
            btnFreqMonthly.setAlpha(alpha);
            layoutStartDateTrigger.setAlpha(alpha);
            layoutEndDateTrigger.setAlpha(alpha);
        }
    }

    private void setupInteractions() {
        setupIconPicker();
        setupColorPicker();
        if (!isDataLocked) {
            btnFreqDaily.setOnClickListener(v -> selectFrequency("Daily"));
            btnFreqWeekly.setOnClickListener(v -> selectFrequency("Weekly"));
            btnFreqMonthly.setOnClickListener(v -> selectFrequency("Monthly"));
            layoutStartDateTrigger.setOnClickListener(v -> showDatePickerDialog(true));
            layoutEndDateTrigger.setOnClickListener(v -> showDatePickerDialog(false));
        }
        layoutTimePickerTrigger.setOnClickListener(v -> showTimePickerDialog());
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutTimePickerTrigger.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void updateDynamicColors() {
        int color = Color.parseColor(selectedColor);
        viewSelectedColorPreview.setBackgroundTintList(ColorStateList.valueOf(color));
        tvStartDateValue.setBackgroundTintList(ColorStateList.valueOf(color));
        tvEndDateValue.setBackgroundTintList(ColorStateList.valueOf(color));
        btnSave.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void selectFrequency(String freq) {
        selectedFrequency = freq;
        int activeColor = Color.parseColor(selectedColor);
        int inactiveColor = Color.parseColor("#F5F5F5");

        btnFreqDaily.setBackgroundTintList(ColorStateList.valueOf("Daily".equals(freq) ? activeColor : inactiveColor));
        btnFreqDaily.setTextColor("Daily".equals(freq) ? Color.WHITE : Color.BLACK);
        btnFreqWeekly.setBackgroundTintList(ColorStateList.valueOf("Weekly".equals(freq) ? activeColor : inactiveColor));
        btnFreqWeekly.setTextColor("Weekly".equals(freq) ? Color.WHITE : Color.BLACK);
        btnFreqMonthly.setBackgroundTintList(ColorStateList.valueOf("Monthly".equals(freq) ? activeColor : inactiveColor));
        btnFreqMonthly.setTextColor("Monthly".equals(freq) ? Color.WHITE : Color.BLACK);

        layoutWeeklySelectionContainer.setVisibility("Weekly".equals(freq) ? View.VISIBLE : View.GONE);
        layoutMonthlySelectionContainer.setVisibility("Monthly".equals(freq) ? View.VISIBLE : View.GONE);
        
        setupWeeklySelection();
        if("Monthly".equals(freq)) setupMonthlySelection();
    }

    private void setupWeeklySelection() {
        layoutWeeklySelection.removeAllViews();
        String[] days = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < days.length; i++) {
            final int dayValue = i + 1;
            FrameLayout container = new FrameLayout(requireContext());
            container.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            TextView tvDay = new TextView(requireContext());
            int size = (int) (42 * density);
            FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(size, size);
            tvParams.gravity = android.view.Gravity.CENTER;
            tvDay.setLayoutParams(tvParams);
            tvDay.setText(days[i]);
            tvDay.setGravity(android.view.Gravity.CENTER);
            tvDay.setTextSize(9);
            tvDay.setBackgroundResource(R.drawable.bg_color_circle);
            boolean isSelected = selectedWeeklyDays.contains(dayValue);
            tvDay.setBackgroundTintList(ColorStateList.valueOf(isSelected ? Color.parseColor(selectedColor) : Color.parseColor("#F5F5F5")));
            tvDay.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
            if (!isDataLocked) {
                tvDay.setOnClickListener(v -> {
                    if (selectedWeeklyDays.contains(dayValue)) selectedWeeklyDays.remove(Integer.valueOf(dayValue));
                    else selectedWeeklyDays.add(dayValue);
                    setupWeeklySelection();
                });
            } else tvDay.setAlpha(0.7f);
            container.addView(tvDay);
            layoutWeeklySelection.addView(container);
        }
    }

    private void setupMonthlySelection() {
        List<String> dates = new ArrayList<>();
        for (int i = 1; i <= 31; i++) dates.add(String.valueOf(i));
        MonthlyPickerAdapter adapter = new MonthlyPickerAdapter(dates, selectedMonthlyDates, selectedColor, date -> {
            if (isDataLocked) return;
            int d = Integer.parseInt(date);
            if (selectedMonthlyDates.contains(d)) selectedMonthlyDates.remove(Integer.valueOf(d));
            else selectedMonthlyDates.add(d);
        });
        rvMonthlyPicker.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvMonthlyPicker.setAdapter(adapter);
        if (isDataLocked) btnSelectAllMonthly.setVisibility(View.GONE);
        else {
            btnSelectAllMonthly.setOnClickListener(v -> {
                if (selectedMonthlyDates.size() == 31) selectedMonthlyDates.clear();
                else {
                    selectedMonthlyDates.clear();
                    for (int i = 1; i <= 31; i++) selectedMonthlyDates.add(i);
                }
                adapter.notifyDataSetChanged();
            });
        }
    }

    private void setupIconPicker() {
        ivHabitIcon.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_emoji_picker, null);
            RecyclerView rvEmojiPicker = dialogView.findViewById(R.id.rvEmojiPicker);
            AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
            if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            IconAdapter adapter = new IconAdapter(ICON_LIST, iconName -> {
                selectedIconName = iconName;
                ivHabitIcon.setImageResource(getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName()));
                dialog.dismiss();
            });
            rvEmojiPicker.setLayoutManager(new GridLayoutManager(getContext(), 6));
            rvEmojiPicker.setAdapter(adapter);
            dialog.show();
        });
    }

    private void setupColorPicker() {
        layoutColorPickerTrigger.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_color_picker, null);
            RecyclerView rvColorPicker = (RecyclerView) dialogView;
            AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }
            ColorPickerAdapter adapter = new ColorPickerAdapter(COLOR_PALETTE, color -> {
                selectedColor = color;
                updateDynamicColors();
                selectFrequency(selectedFrequency);
                dialog.dismiss();
            });
            rvColorPicker.setLayoutManager(new GridLayoutManager(getContext(), 6));
            rvColorPicker.setAdapter(adapter);
            dialog.show();
        });
    }

    private void showTimePickerDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_time_picker, null);
        bottomSheetDialog.setContentView(sheetView);
        TimePicker timePicker = sheetView.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        String[] parts = selectedTime.split(":");
        timePicker.setHour(Integer.parseInt(parts[0]));
        timePicker.setMinute(Integer.parseInt(parts[1]));
        sheetView.findViewById(R.id.btnConfirmTime).setOnClickListener(v -> {
            selectedTime = String.format("%02d:%02d", timePicker.getHour(), timePicker.getMinute());
            tvSelectedTime.setText(selectedTime);
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void showDatePickerDialog(boolean isStartDate) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_date_picker, null);
        bottomSheetDialog.setContentView(sheetView);
        DatePicker datePicker = sheetView.findViewById(R.id.datePicker);
        Button btnSetNoEnd = sheetView.findViewById(R.id.btnSetNoEnd);
        if (!isStartDate) btnSetNoEnd.setVisibility(View.VISIBLE);
        LocalDate current = isStartDate ? selectedStartDate : (selectedEndDate != null ? selectedEndDate : LocalDate.now());
        datePicker.updateDate(current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());
        btnSetNoEnd.setOnClickListener(v -> {
            selectedEndDate = null;
            tvEndDateValue.setText("Không");
            bottomSheetDialog.dismiss();
        });
        sheetView.findViewById(R.id.btnConfirmDate).setOnClickListener(v -> {
            LocalDate picked = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
            if (isStartDate) {
                selectedStartDate = picked;
                tvStartDateValue.setText(selectedStartDate.toString());
            } else {
                selectedEndDate = picked;
                tvEndDateValue.setText(selectedEndDate.toString());
            }
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void saveHabit() {
        String name = etHabitName.getText().toString().trim();
        if (name.isEmpty()) return;
        habitToEdit.habitName = name;
        habitToEdit.displayColor = selectedColor;
        habitToEdit.icon = selectedIconName;
        habitToEdit.isReminderEnabled = swReminder.isChecked();
        habitToEdit.reminderTime = swReminder.isChecked() ? selectedTime : null;
        if (!isDataLocked) {
            habitToEdit.frequencyType = selectedFrequency;
            habitToEdit.startDate = selectedStartDate.toString();
            habitToEdit.endDate = selectedEndDate != null ? selectedEndDate.toString() : "2099-12-31";
            String freqValue = "";
            if ("Weekly".equals(selectedFrequency)) freqValue = selectedWeeklyDays.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
            else if ("Monthly".equals(selectedFrequency)) freqValue = selectedMonthlyDates.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
            habitToEdit.frequencyValue = freqValue;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            db.habitDao().update(habitToEdit);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
            }
        });
    }

    private static class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {
        private final String[] icons;
        private final OnIconClickListener listener;
        public interface OnIconClickListener { void onIconClick(String iconName); }
        public IconAdapter(String[] icons, OnIconClickListener listener) { this.icons = icons; this.listener = listener; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            FrameLayout container = new FrameLayout(p.getContext());
            float density = p.getContext().getResources().getDisplayMetrics().density;
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * density)));
            ImageView iv = new ImageView(p.getContext());
            int size = (int) (32 * density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.gravity = android.view.Gravity.CENTER;
            iv.setLayoutParams(params);
            container.addView(iv);
            return new ViewHolder(container, iv);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            int resId = h.itemView.getContext().getResources().getIdentifier(icons[p], "drawable", h.itemView.getContext().getPackageName());
            h.iv.setImageResource(resId);
            h.itemView.setOnClickListener(v -> listener.onIconClick(icons[p]));
        }
        @Override public int getItemCount() { return icons.length; }
        static class ViewHolder extends RecyclerView.ViewHolder { 
            ImageView iv;
            public ViewHolder(@NonNull View v, ImageView iv) { super(v); this.iv = iv; } 
        }
    }

    private static class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {
        private final String[] colors;
        private final OnColorClickListener listener;
        public interface OnColorClickListener { void onColorClick(String color); }
        public ColorPickerAdapter(String[] colors, OnColorClickListener listener) { this.colors = colors; this.listener = listener; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            float density = p.getContext().getResources().getDisplayMetrics().density;
            LinearLayout container = new LinearLayout(p.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (40 * density)));
            container.setGravity(android.view.Gravity.CENTER);
            View v = new View(p.getContext());
            v.setLayoutParams(new LinearLayout.LayoutParams((int) (28 * density), (int) (28 * density)));
            v.setBackgroundResource(R.drawable.bg_color_circle);
            container.addView(v);
            return new ViewHolder(container, v);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            h.colorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(colors[p])));
            h.itemView.setOnClickListener(v -> listener.onColorClick(colors[p]));
        }
        @Override public int getItemCount() { return colors.length; }
        static class ViewHolder extends RecyclerView.ViewHolder { 
            View colorView;
            public ViewHolder(@NonNull View v, View colorView) { super(v); this.colorView = colorView; } 
        }
    }

    private static class MonthlyPickerAdapter extends RecyclerView.Adapter<MonthlyPickerAdapter.ViewHolder> {
        private final List<String> items;
        private final List<Integer> selectedMonthlyDates;
        private final String selectedColor;
        private final OnDateClickListener listener;
        public interface OnDateClickListener { void onDateClick(String date); }
        public MonthlyPickerAdapter(List<String> items, List<Integer> selectedMonthlyDates, String selectedColor, OnDateClickListener listener) {
            this.items = items;
            this.selectedMonthlyDates = selectedMonthlyDates;
            this.selectedColor = selectedColor;
            this.listener = listener;
        }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int t) {
            float density = p.getContext().getResources().getDisplayMetrics().density;
            FrameLayout container = new FrameLayout(p.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (46 * density)));
            TextView tv = new TextView(p.getContext());
            int size = (int) (38 * density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.gravity = android.view.Gravity.CENTER;
            tv.setLayoutParams(params);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setBackgroundResource(R.drawable.bg_color_circle);
            container.addView(tv);
            return new ViewHolder(container, tv);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int p) {
            String date = items.get(p);
            h.tv.setText(date);
            boolean isSelected = selectedMonthlyDates.contains(Integer.parseInt(date));
            h.tv.setBackgroundTintList(ColorStateList.valueOf(isSelected ? Color.parseColor(selectedColor) : Color.parseColor("#F5F5F5")));
            h.tv.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
            h.tv.setOnClickListener(v -> {
                listener.onDateClick(date);
                notifyItemChanged(p);
            });
        }
        @Override public int getItemCount() { return items.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder { 
            TextView tv;
            public ViewHolder(@NonNull View v, TextView tv) { super(v); this.tv = tv; } 
        }
    }
}
