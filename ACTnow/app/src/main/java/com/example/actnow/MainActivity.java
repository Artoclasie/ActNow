package com.example.actnow;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private static final String THEME_PREF = "theme_preferences";
    private static final String THEME_MODE_KEY = "theme_mode";

    FirebaseAuth mAuth;
    TabLayout apptabs;
    ViewPager2 pager;
    ViewPagerFragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Применяем сохраненную тему перед установкой контента
        applySavedTheme();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initvar();
        settabs();

        setupTransparentStatusBar();
    }

    private void initvar() {
        mAuth = FirebaseAuth.getInstance();
        apptabs = findViewById(R.id.apptabs);
        pager = findViewById(R.id.pager);
        adapter = new ViewPagerFragmentAdapter(this, apptabs.getTabCount());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(5);
    }

    private void setupTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void applySavedTheme() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
        int savedTheme = prefs.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedTheme);
    }

    public void toggleTheme() {
        int currentTheme = getCurrentTheme();
        int newTheme = (currentTheme == AppCompatDelegate.MODE_NIGHT_YES)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        // Сохраняем новый выбор темы
        getSharedPreferences(THEME_PREF, MODE_PRIVATE)
                .edit()
                .putInt(THEME_MODE_KEY, newTheme)
                .apply();

        // Применяем новую тему
        AppCompatDelegate.setDefaultNightMode(newTheme);

        // Пересоздаем активность с анимацией
        recreate();
    }

    public int getCurrentTheme() {
        return getSharedPreferences(THEME_PREF, MODE_PRIVATE)
                .getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    private void settabs() {
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                apptabs.selectTab(apptabs.getTabAt(position));
            }
        });

        apptabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.parseColor("#223542"), PorterDuff.Mode.SRC_IN);
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.parseColor("#4F87A2"), PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        TabLayout.Tab tab = apptabs.getTabAt(0);
        if (tab != null) {
            tab.getIcon().setColorFilter(Color.parseColor("#223542"), PorterDuff.Mode.SRC_IN);
            apptabs.selectTab(tab);
            pager.setCurrentItem(0);
        }
    }

    public static class ViewPagerFragmentAdapter extends FragmentStateAdapter {
        int size;

        public ViewPagerFragmentAdapter(@NonNull FragmentActivity fragmentActivity, int size) {
            super(fragmentActivity);
            this.size = size;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new CreateFragment();
                case 2:
                    return new ChatsFragment();
                case 3:
                    return new ProfileFragment();
                default:
                    return new AboutFragment();
            }
        }

        @Override
        public int getItemCount() {
            return size;
        }
    }
}