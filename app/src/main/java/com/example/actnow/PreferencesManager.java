package com.example.actnow;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesManager {
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        return prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    public void setThemeMode(int mode) {
        prefs.edit().putInt(PREF_THEME_MODE, mode).apply();
    }

    public boolean getNotificationsEnabled() {
        return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    // Новый метод для сохранения строки
    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    // Новый метод для получения строки
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
}