package com.duominz.habittracker.ui.fragments;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import com.duominz.habittracker.ui.activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AddHabitFragment extends Fragment {

    private EditText etHabitName;
    private TextView tvWeeklySummary, tvMonthlySummary, tvSelectedTime;
    private ImageView ivHabitIcon;
    private TextView tvStartDateValue, tvEndDateValue;
    private View viewSelectedColorPreview;
    private LinearLayout layoutColorPickerTrigger, layoutWeeklySelection, layoutTimePickerTrigger;
    private LinearLayout layoutWeeklySelectionContainer, layoutMonthlySelectionContainer;
    private LinearLayout layoutStartDateTrigger, layoutEndDateTrigger;
    private Button btnSave, btnSelectAllMonthly;
    private Button btnFreqDaily, btnFreqWeekly, btnFreqMonthly;
    private RecyclerView rvMonthlyPicker;
    private SwitchCompat swReminder;

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
            // Thêm tên các drawable khác (max = 30)
    };

    private String selectedColor = COLOR_PALETTE[0];
    private String selectedFrequency = "Daily";
    private final List<Integer> selectedWeeklyDays = new ArrayList<>();
    private final List<Integer> selectedMonthlyDates = new ArrayList<>();
    private LocalDate selectedStartDate = LocalDate.now();
    private LocalDate selectedEndDate = null; 
    private String selectedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    private String selectedIconName = "ic_book";
    
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_habit, container, false);
        initViews(view);
        db = AppDatabase.getInstance(requireContext());
        setupFrequencyButtons();
        setupWeeklySelection();
        setupMonthlySelection();
        setupDateTimePickers();
        setupIconPicker();
        setupColorPicker();
        setupHabitTermPickers();
        btnSave.setOnClickListener(v -> saveHabit());
        selectFrequency("Daily");
        updateDynamicColors();
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
        tvWeeklySummary = view.findViewById(R.id.tvWeeklySummary);

        layoutMonthlySelectionContainer = view.findViewById(R.id.layoutMonthlySelectionContainer);
        rvMonthlyPicker = view.findViewById(R.id.rvMonthlyPicker);
        btnSelectAllMonthly = view.findViewById(R.id.btnSelectAllMonthly);
        tvMonthlySummary = view.findViewById(R.id.tvMonthlySummary);

        layoutTimePickerTrigger = view.findViewById(R.id.layoutTimePickerTrigger);
        tvSelectedTime = view.findViewById(R.id.tvSelectedTime);
        swReminder = view.findViewById(R.id.swReminder);

        layoutStartDateTrigger = view.findViewById(R.id.layoutStartDateTrigger);
        tvStartDateValue = view.findViewById(R.id.tvStartDateValue);
        layoutEndDateTrigger = view.findViewById(R.id.layoutEndDateTrigger);
        tvEndDateValue = view.findViewById(R.id.tvEndDateValue);

        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupIconPicker() {
        ivHabitIcon.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_emoji_picker, null);
            RecyclerView rvEmojiPicker = dialogView.findViewById(R.id.rvEmojiPicker);
            AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            }
            IconAdapter adapter = new IconAdapter(ICON_LIST, iconName -> {
                selectedIconName = iconName;
                int resId = getResources().getIdentifier(selectedIconName, "drawable", requireContext().getPackageName());
                ivHabitIcon.setImageResource(resId);
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

    private void updateDynamicColors() {
        int color = Color.parseColor(selectedColor);
        viewSelectedColorPreview.setBackgroundTintList(ColorStateList.valueOf(color));
        tvStartDateValue.setBackgroundTintList(ColorStateList.valueOf(color));
        tvEndDateValue.setBackgroundTintList(ColorStateList.valueOf(color));
        btnSave.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private void setupFrequencyButtons() {
        btnFreqDaily.setOnClickListener(v -> selectFrequency("Daily"));
        btnFreqWeekly.setOnClickListener(v -> selectFrequency("Weekly"));
        btnFreqMonthly.setOnClickListener(v -> selectFrequency("Monthly"));
    }

    private void selectFrequency(String freq) {
        selectedFrequency = freq;
        int activeColor = Color.parseColor(selectedColor);
        int inactiveColor = Color.parseColor("#F5F5F5");

        btnFreqDaily.setBackgroundTintList(ColorStateList.valueOf(freq.equals("Daily") ? activeColor : inactiveColor));
        btnFreqDaily.setTextColor(freq.equals("Daily") ? Color.WHITE : Color.BLACK);

        btnFreqWeekly.setBackgroundTintList(ColorStateList.valueOf(freq.equals("Weekly") ? activeColor : inactiveColor));
        btnFreqWeekly.setTextColor(freq.equals("Weekly") ? Color.WHITE : Color.BLACK);

        btnFreqMonthly.setBackgroundTintList(ColorStateList.valueOf(freq.equals("Monthly") ? activeColor : inactiveColor));
        btnFreqMonthly.setTextColor(freq.equals("Monthly") ? Color.WHITE : Color.BLACK);

        layoutWeeklySelectionContainer.setVisibility(freq.equals("Weekly") ? View.VISIBLE : View.GONE);
        layoutMonthlySelectionContainer.setVisibility(freq.equals("Monthly") ? View.VISIBLE : View.GONE);
        
        setupWeeklySelection();
        if(freq.equals("Monthly")) setupMonthlySelection();
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
            tvDay.setClickable(true);
            tvDay.setBackgroundResource(R.drawable.bg_color_circle);
            
            boolean isSelected = selectedWeeklyDays.contains(dayValue);
            tvDay.setBackgroundTintList(ColorStateList.valueOf(isSelected ? Color.parseColor(selectedColor) : Color.parseColor("#F5F5F5")));
            tvDay.setTextColor(isSelected ? Color.WHITE : Color.BLACK);

            tvDay.setOnClickListener(v -> {
                if (selectedWeeklyDays.contains(dayValue)) {
                    selectedWeeklyDays.remove(Integer.valueOf(dayValue));
                } else {
                    selectedWeeklyDays.add(dayValue);
                }
                setupWeeklySelection();
                updateWeeklySummary();
            });
            
            container.addView(tvDay);
            layoutWeeklySelection.addView(container);
        }
        updateWeeklySummary();
    }

    private void updateWeeklySummary() {
        if (selectedWeeklyDays.isEmpty()) {
            tvWeeklySummary.setText("*Task needs to be done every day");
        } else {
            String days = selectedWeeklyDays.stream()
                    .sorted()
                    .map(d -> {
                        switch(d){
                            case 1: return "MON"; case 2: return "TUE"; case 3: return "WED";
                            case 4: return "THU"; case 5: return "FRI"; case 6: return "SAT";
                            case 7: return "SUN"; default: return "";
                        }
                    }).collect(Collectors.joining(", "));
            tvWeeklySummary.setText("*Task needs to be done every " + days);
        }
    }

    private void setupMonthlySelection() {
        List<String> dates = new ArrayList<>();
        for (int i = 1; i <= 31; i++) dates.add(String.valueOf(i));

        MonthlyPickerAdapter adapter = new ColorDrawableAdapter(dates, date -> {
            int d = Integer.parseInt(date);
            if (selectedMonthlyDates.contains(d)) selectedMonthlyDates.remove(Integer.valueOf(d));
            else selectedMonthlyDates.add(d);
            updateMonthlySummary();
        });

        rvMonthlyPicker.setLayoutManager(new GridLayoutManager(getContext(), 7));
        rvMonthlyPicker.setAdapter(adapter);

        btnSelectAllMonthly.setOnClickListener(v -> {
            if (selectedMonthlyDates.size() == 31) selectedMonthlyDates.clear();
            else {
                selectedMonthlyDates.clear();
                for (int i = 1; i <= 31; i++) selectedMonthlyDates.add(i);
            }
            adapter.notifyDataSetChanged();
            updateMonthlySummary();
        });
        updateMonthlySummary();
    }

    private void updateMonthlySummary() {
        if (selectedMonthlyDates.isEmpty()) {
            tvMonthlySummary.setText("*Task needs to be done on specific dates");
        } else {
            String dates = selectedMonthlyDates.stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
            tvMonthlySummary.setText("*Task needs to be done on " + dates);
        }
    }

    private void setupDateTimePickers() {
        tvSelectedTime.setText(selectedTime);
        layoutTimePickerTrigger.setOnClickListener(v -> showTimePickerDialog());
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutTimePickerTrigger.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
        sheetView.findViewById(R.id.ivCloseTimePicker).setOnClickListener(v -> bottomSheetDialog.dismiss());
        sheetView.findViewById(R.id.btnConfirmTime).setOnClickListener(v -> {
            LocalTime time = LocalTime.of(timePicker.getHour(), timePicker.getMinute());
            selectedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));
            tvSelectedTime.setText(selectedTime);
            bottomSheetDialog.dismiss();
        });
        bottomSheetDialog.show();
    }

    private void setupHabitTermPickers() {
        tvStartDateValue.setText(selectedStartDate.toString());
        tvEndDateValue.setText("No End");
        updateDynamicColors();
        layoutStartDateTrigger.setOnClickListener(v -> showDatePickerDialog(true));
        layoutEndDateTrigger.setOnClickListener(v -> showDatePickerDialog(false));
    }

    private void showDatePickerDialog(boolean isStartDate) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.layout_bottom_sheet_date_picker, null);
        bottomSheetDialog.setContentView(sheetView);

        DatePicker datePicker = sheetView.findViewById(R.id.datePicker);
        Button btnSetNoEnd = sheetView.findViewById(R.id.btnSetNoEnd);
        Button btnConfirmDate = sheetView.findViewById(R.id.btnConfirmDate);
        
        if (!isStartDate) btnSetNoEnd.setVisibility(View.VISIBLE);

        LocalDate current = isStartDate ? selectedStartDate : (selectedEndDate != null ? selectedEndDate : LocalDate.now());
        datePicker.updateDate(current.getYear(), current.getMonthValue() - 1, current.getDayOfMonth());

        sheetView.findViewById(R.id.ivCloseDatePicker).setOnClickListener(v -> bottomSheetDialog.dismiss());
        
        btnSetNoEnd.setOnClickListener(v -> {
            selectedEndDate = null;
            tvEndDateValue.setText("No End");
            bottomSheetDialog.dismiss();
        });

        btnConfirmDate.setOnClickListener(v -> {
            LocalDate picked = LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
            
            if (isStartDate) {
                if (selectedEndDate != null && picked.isAfter(selectedEndDate)) {
                    Toast.makeText(getContext(), "Start date cannot be after end date!", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedStartDate = picked;
                tvStartDateValue.setText(selectedStartDate.toString());
            } else {
                if (picked.isBefore(selectedStartDate)) {
                    Toast.makeText(getContext(), "End date cannot be before start date!", Toast.LENGTH_SHORT).show();
                    return;
                }
                selectedEndDate = picked;
                tvEndDateValue.setText(selectedEndDate.toString());
            }
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void saveHabit() {
        String name = etHabitName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập tên thói quen!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedEndDate != null && selectedEndDate.isBefore(selectedStartDate)) {
             Toast.makeText(requireContext(), "Lỗi: Ngày kết thúc không hợp lệ!", Toast.LENGTH_SHORT).show();
             return;
        }

        String freqValue = "";
        if (selectedFrequency.equals("Weekly")) {
            if (selectedWeeklyDays.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày trong tuần!", Toast.LENGTH_SHORT).show();
                return;
            }
            freqValue = selectedWeeklyDays.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
        } else if (selectedFrequency.equals("Monthly")) {
            if (selectedMonthlyDates.isEmpty()) {
                Toast.makeText(requireContext(), "Vui lòng chọn ngày trong tháng!", Toast.LENGTH_SHORT).show();
                return;
            }
            freqValue = selectedMonthlyDates.stream().sorted().map(String::valueOf).collect(Collectors.joining(","));
        }

        Habit newHabit = new Habit();
        newHabit.habitName = name;
        newHabit.displayColor = selectedColor;
        newHabit.icon = selectedIconName;
        newHabit.startDate = selectedStartDate.toString();
        newHabit.endDate = selectedEndDate != null ? selectedEndDate.toString() : "2099-12-31";
        newHabit.frequencyType = selectedFrequency;
        newHabit.frequencyValue = freqValue;
        newHabit.isReminderEnabled = swReminder.isChecked();
        newHabit.reminderTime = swReminder.isChecked() ? selectedTime : null;
        newHabit.currentStreak = 0;
        newHabit.highestStreak = 0;
        newHabit.isArchived = false;

        Executors.newSingleThreadExecutor().execute(() -> {
            db.habitDao().insert(newHabit);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Đã thêm thói quen thành công!", Toast.LENGTH_SHORT).show();
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).switchToHomeTab();
                }
            });
        });
    }

    // --- ADAPTERS ---

    private static class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {
        private final String[] icons;
        private final OnIconClickListener listener;
        public interface OnIconClickListener { void onIconClick(String iconName); }
        public IconAdapter(String[] icons, OnIconClickListener listener) {
            this.icons = icons;
            this.listener = listener;
        }
        @NonNull
        @Override
        public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout container = new FrameLayout(parent.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (48 * parent.getContext().getResources().getDisplayMetrics().density)));
            ImageView iv = new ImageView(parent.getContext());
            int size = (int) (32 * parent.getContext().getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.gravity = android.view.Gravity.CENTER;
            iv.setLayoutParams(params);
            container.addView(iv);
            return new IconViewHolder(container, iv);
        }
        @Override
        public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
            String iconName = icons[position];
            int resId = holder.itemView.getContext().getResources().getIdentifier(iconName, "drawable", holder.itemView.getContext().getPackageName());
            holder.iv.setImageResource(resId);
            holder.itemView.setOnClickListener(v -> listener.onIconClick(iconName));
        }
        @Override
        public int getItemCount() { return icons.length; }
        static class IconViewHolder extends RecyclerView.ViewHolder {
            ImageView iv;
            public IconViewHolder(@NonNull View itemView, ImageView iv) { super(itemView); this.iv = iv; }
        }
    }

    private static class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ColorViewHolder> {
        private final String[] colors;
        private final OnColorClickListener listener;
        public interface OnColorClickListener { void onColorClick(String color); }
        public ColorPickerAdapter(String[] colors, OnColorClickListener listener) {
            this.colors = colors;
            this.listener = listener;
        }
        @NonNull
        @Override
        public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new View(parent.getContext());
            int size = (int) (40 * parent.getContext().getResources().getDisplayMetrics().density);
            int circleSize = (int) (28 * parent.getContext().getResources().getDisplayMetrics().density);
            LinearLayout container = new LinearLayout(parent.getContext());
            container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size));
            container.setGravity(android.view.Gravity.CENTER);
            view.setLayoutParams(new LinearLayout.LayoutParams(circleSize, circleSize));
            view.setBackgroundResource(R.drawable.bg_color_circle);
            container.addView(view);
            return new ColorViewHolder(container, view);
        }
        @Override
        public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
            String color = colors[position];
            holder.colorView.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(color)));
            holder.itemView.setOnClickListener(v -> listener.onColorClick(color));
        }
        @Override
        public int getItemCount() { return colors.length; }
        static class ColorViewHolder extends RecyclerView.ViewHolder {
            View colorView;
            public ColorViewHolder(@NonNull View itemView, View colorView) {
                super(itemView);
                this.colorView = colorView;
            }
        }
    }

    private interface OnDateClickListener { void onDateClick(String date); }

    private abstract static class MonthlyPickerAdapter extends RecyclerView.Adapter<MonthlyPickerAdapter.ViewHolder> {
        protected final List<String> items;
        protected final OnDateClickListener listener;
        public MonthlyPickerAdapter(List<String> items, OnDateClickListener listener) {
            this.items = items;
            this.listener = listener;
        }
        @Override
        public int getItemCount() { return items.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(@NonNull View itemView) { super(itemView); }
        }
    }

    private class ColorDrawableAdapter extends MonthlyPickerAdapter {
        public ColorDrawableAdapter(List<String> items, OnDateClickListener listener) { super(items, listener); }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            FrameLayout container = new FrameLayout(parent.getContext());
            int cellSize = ViewGroup.LayoutParams.MATCH_PARENT;
            container.setLayoutParams(new ViewGroup.LayoutParams(cellSize, (int) (46 * getResources().getDisplayMetrics().density)));
            TextView tv = new TextView(parent.getContext());
            int circleSize = (int) (38 * getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(circleSize, circleSize);
            params.gravity = android.view.Gravity.CENTER;
            tv.setLayoutParams(params);
            tv.setGravity(android.view.Gravity.CENTER);
            tv.setTextSize(13);
            tv.setBackgroundResource(R.drawable.bg_color_circle);
            container.addView(tv);
            return new ViewHolder(container);
        }
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String date = items.get(position);
            TextView tv = (TextView) ((FrameLayout) holder.itemView).getChildAt(0);
            tv.setText(date);
            boolean isSelected = selectedMonthlyDates.contains(Integer.parseInt(date));
            tv.setBackgroundTintList(ColorStateList.valueOf(isSelected ? Color.parseColor(selectedColor) : Color.parseColor("#F5F5F5")));
            tv.setTextColor(isSelected ? Color.WHITE : Color.BLACK);
            tv.setOnClickListener(v -> {
                listener.onDateClick(date);
                notifyItemChanged(position);
            });
        }
    }
}
