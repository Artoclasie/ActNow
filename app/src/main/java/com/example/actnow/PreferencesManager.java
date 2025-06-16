package com.example.actnow;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesManager {
    private static final String PREF_THEME_MODE = "theme_mode";
    private static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String TAG = "PreferencesManager";
    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public int getThemeMode() {
        int mode = prefs.getInt(PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        Log.d(TAG, "Retrieved theme mode: " + mode);
        return mode;
    }

    public void setThemeMode(int mode) {
        Log.d(TAG, "Setting theme mode to: " + mode);
        prefs.edit().putInt(PREF_THEME_MODE, mode).apply();
        Log.d(TAG, "Theme mode saved: " + prefs.getInt(PREF_THEME_MODE, -1));
    }

    public boolean getNotificationsEnabled() {
        return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }
}