package com.example.mostsave.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SecurityManager {

    private static final String TAG = "SecurityManager";
    private static SecurityManager instance;

    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final Handler handler;

    // Clipboard related
    private Runnable clipboardClearRunnable;
    private long clipboardTimer = 60000L; // Default 60 seconds
    private boolean isClipboardClearEnabled = false;
    private final ClipboardManager clipboardManager;

    private SecurityManager(Context context) {
        this.context = context.getApplicationContext();
        this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        this.handler = new Handler(Looper.getMainLooper());
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

        // Load saved preferences
        loadPreferences();
    }

    public static synchronized SecurityManager getInstance(Context context) {
        if (instance == null) {
            instance = new SecurityManager(context);
        }
        return instance;
    }

    private void loadPreferences() {
        isClipboardClearEnabled = sharedPreferences.getBoolean("auto_clear_clipboard", false);
        clipboardTimer = sharedPreferences.getLong("clipboard_timer", 60000L);
    }

    // Clipboard functionality
    public void startClipboardClearTimer() {
        if (!isClipboardClearEnabled || clipboardTimer == -1L) return; // -1 means "Never"

        stopClipboardClearTimer(); // Stop any existing timer

        clipboardClearRunnable = () -> {
            Log.d(TAG, "Clipboard clear timer triggered");
            clearClipboard();
        };

        handler.postDelayed(clipboardClearRunnable, clipboardTimer);
    }

    public void stopClipboardClearTimer() {
        if (clipboardClearRunnable != null) {
            handler.removeCallbacks(clipboardClearRunnable);
            clipboardClearRunnable = null;
        }
    }

    public void updateClipboardTimer(long timer) {
        this.clipboardTimer = timer;
        if (isClipboardClearEnabled) {
            startClipboardClearTimer(); // Restart with new timer
        }
    }

    public void onPasswordCopied() {
        Log.d(TAG, "Password copied - starting clipboard clear timer");
        if (isClipboardClearEnabled) {
            startClipboardClearTimer();
        }
    }

    private void clearClipboard() {
        try {
            if (clipboardManager != null) {
                ClipData clipData = ClipData.newPlainText("", "");
                clipboardManager.setPrimaryClip(clipData);
                Log.d(TAG, "Clipboard cleared successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error clearing clipboard: " + e.getMessage());
        }
    }

    // Settings update methods
    public void setClipboardClearEnabled(boolean enabled) {
        this.isClipboardClearEnabled = enabled;
        sharedPreferences.edit().putBoolean("auto_clear_clipboard", enabled).apply();
        if (!enabled) {
            stopClipboardClearTimer();
        }
    }

    // Utility methods
    public boolean isClipboardClearEnabled() {
        return isClipboardClearEnabled;
    }

    public long getClipboardTimer() {
        return clipboardTimer;
    }

    public void cleanup() {
        stopClipboardClearTimer();
    }
}
