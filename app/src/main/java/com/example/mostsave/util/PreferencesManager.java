package com.example.mostsave.util;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class PreferencesManager {

    private static final String PREFS_NAME = "MostSavePrefs";
    private static final String KEY_THEME = "theme_preference";
    private static final String KEY_APP_LOCK = "app_lock_preference";

    private final SharedPreferences sharedPreferences;

    public PreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Theme Preferences
    public void setThemeMode(int mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_THEME, mode);
        editor.apply();
    }

    public int getThemeMode() {
        // Default to system theme if no preference is set
        return sharedPreferences.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    // App Lock Preferences
    public void setAppLockEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_APP_LOCK, enabled);
        editor.apply();
    }

    public boolean isAppLockEnabled() {
        return sharedPreferences.getBoolean(KEY_APP_LOCK, false); // Default to false (app lock disabled)
    }
}

