package com.example.actnow;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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

import com.example.actnow.Fragments.ChatsFragment;
import com.example.actnow.Fragments.HomeFragment;
import com.example.actnow.Fragments.NotificationFragment;
import com.example.actnow.Fragments.PostFragment;
import com.example.actnow.Fragments.ProfileFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final String THEME_PREF = "theme_preferences";
    private static final String THEME_MODE_KEY = "theme_mode";
    private boolean isThemeApplied = false;

    FirebaseAuth mAuth;
    TabLayout apptabs;
    ViewPager2 pager;
    ViewPagerFragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();

        // Применяем тему до создания активности
        applyInitialTheme();

        // Блокируем отображение до готовности
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Скрываем UI до полной готовности
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        findViewById(R.id.cardViewContainer).setVisibility(View.GONE);
        findViewById(R.id.pager).setVisibility(View.GONE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        applySavedTheme();
        initvar();
        settabs();
        setupTransparentStatusBar();

        // Показываем UI только после применения темы
        if (isThemeApplied) {
            showUI();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void applyInitialTheme() {
        SharedPreferences prefs = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
        int savedTheme = prefs.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setTheme(getThemeResId(savedTheme)); // Применяем тему до onCreate
    }

    private int getThemeResId(int mode) {
        switch (mode) {
            case AppCompatDelegate.MODE_NIGHT_YES:
                return R.style.ActNow_Dark;
            case AppCompatDelegate.MODE_NIGHT_NO:
                return R.style.ActNow_Light;
            default:
                return R.style.ActNow_Light;
        }
    }

    private void initvar() {
        apptabs = findViewById(R.id.apptabs);
        pager = findViewById(R.id.pager);
        adapter = new ViewPagerFragmentAdapter(this, apptabs.getTabCount());
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(5);
        pager.setUserInputEnabled(false);
    }

    private void setupTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void applySavedTheme() {
        if (isThemeApplied) return;

        SharedPreferences prefs = getSharedPreferences(THEME_PREF, MODE_PRIVATE);
        int savedTheme = prefs.getInt(THEME_MODE_KEY, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // Применяем локальную тему сразу
        applyTheme(savedTheme);

        if (mAuth.getCurrentUser() != null) {
            String currentUserId = mAuth.getCurrentUser().getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("Profiles").document(currentUserId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Long themeMode = documentSnapshot.getLong("themeMode");
                            int mode = themeMode != null ? themeMode.intValue() : savedTheme;
                            if (mode != savedTheme) {
                                applyTheme(mode);
                                prefs.edit().putInt(THEME_MODE_KEY, mode).apply();
                            }
                        }
                        showUI();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error loading theme: " + e.getMessage());
                        showUI();
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    });
        } else {
            showUI();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void applyTheme(int mode) {
        AppCompatDelegate.setDefaultNightMode(mode);
        getDelegate().applyDayNight();
        isThemeApplied = true;
    }

    private void showUI() {
        if (isThemeApplied) {
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            findViewById(R.id.cardViewContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.pager).setVisibility(View.VISIBLE);
        }
    }

    public void toggleTheme() {
        int currentTheme = getCurrentTheme();
        int newTheme = (currentTheme == AppCompatDelegate.MODE_NIGHT_YES)
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;
        Log.d("MainActivity", "Toggling theme from " + currentTheme + " to " + newTheme);

        getSharedPreferences(THEME_PREF, MODE_PRIVATE)
                .edit()
                .putInt(THEME_MODE_KEY, newTheme)
                .apply();

        // Скрываем UI перед переключением
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        findViewById(R.id.cardViewContainer).setVisibility(View.GONE);
        findViewById(R.id.pager).setVisibility(View.GONE);

        if (mAuth.getCurrentUser() != null) {
            String currentUserId = mAuth.getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("Profiles")
                    .document(currentUserId)
                    .update("themeMode", newTheme)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("MainActivity", "Theme updated in Firestore to " + newTheme);
                        applyTheme(newTheme);
                        showUI();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Error updating theme: " + e.getMessage());
                        applyTheme(newTheme);
                        showUI();
                    });
        } else {
            applyTheme(newTheme);
            showUI();
        }
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
                    return new NotificationFragment();
                case 2:
                    return new PostFragment();
                case 3:
                    return new ChatsFragment();
                default:
                    return new ProfileFragment();
            }
        }

        @Override
        public int getItemCount() {
            return size;
        }
    }
}