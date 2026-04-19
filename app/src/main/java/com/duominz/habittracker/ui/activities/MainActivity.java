package com.duominz.habittracker.ui.activities; // Đã chốt đúng package

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.duominz.habittracker.R;
import com.duominz.habittracker.ui.fragments.AddHabitFragment;
import com.duominz.habittracker.ui.fragments.HomeFragment;
// import com.duominz.habittracker.ui.fragments.StatsFragment;
// import com.duominz.habittracker.ui.fragments.SettingsFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private boolean isAtHome = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // 1. Mặc định mở HomeFragment khi vừa bật app
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());
            updateHomeButton();
        }

        // 2. Lắng nghe sự kiện bấm Bottom Navigation
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (isAtHome) {
                    // Đang ở Home -> Mở màn Add Habit
                    replaceFragment(new AddHabitFragment());
                    isAtHome = false;
                    updateHomeButton();
                } else {
                    switchToHomeTab();
                }
                return true;
            }
            else if (itemId == R.id.nav_stats) {
                // replaceFragment(new StatsFragment());
                isAtHome = false;
                updateHomeButton();
                return true;
            }
            else if (itemId == R.id.nav_settings) {
                // replaceFragment(new SettingsFragment());
                isAtHome = false;
                updateHomeButton();
                return true;
            }

            return false;
        });
    }


    private void updateHomeButton() {
        Menu menu = bottomNav.getMenu();
        MenuItem homeItem = menu.findItem(R.id.nav_home);

        if (isAtHome) {
            // Nếu đang ở Home -> Nút thành ADD
            homeItem.setIcon(R.drawable.ic_add);
            homeItem.setTitle("Add");
        } else {
            // đang ở trang khác -> Nút thành HOME
            homeItem.setIcon(R.drawable.ic_home);
            homeItem.setTitle("");
        }
    }

    // Hàm đổi Fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }

    // Hàm để các Fragment con gọi ra lệnh quay về Home
    public void switchToHomeTab() {
        replaceFragment(new HomeFragment());
        isAtHome = true;
        updateHomeButton();

        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
}