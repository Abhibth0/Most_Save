package com.example.mostsave.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mostsave.R;
import com.example.mostsave.databinding.FragmentSecuritySettingsBinding;
import com.example.mostsave.utils.SecurityManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SecuritySettingsFragmentFixed extends Fragment {

    private static final String TAG = "SecuritySettings";
    private FragmentSecuritySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private SecurityManager securityManager;

    // Clipboard timer options
    private final String[] clipboardTimerOptions = {
        "15 seconds", "30 seconds", "60 seconds", "2 minutes", "5 minutes", "Never"
    };
    private final long[] clipboardTimerValues = {
        15000L, 30000L, 60000L, 120000L, 300000L, -1L
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSecuritySettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "SecuritySettingsFragment - onViewCreated started");

        try {
            sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);
            securityManager = SecurityManager.getInstance(requireContext());

            Log.d(TAG, "SharedPreferences and SecurityManager initialized");

            initializeViews();
            setupListeners();

            Log.d(TAG, "Views and listeners setup completed");
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "Error initializing security settings", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeViews() {
        try {
            // App Lock Switch (basic functionality without timeout)
            boolean appLockEnabled = sharedPreferences.getBoolean("app_lock", false);
            Log.d(TAG, "App lock enabled: " + appLockEnabled);
            binding.switchAppLock.setChecked(appLockEnabled);

            // Screenshot Switch
            boolean screenshotsEnabled = sharedPreferences.getBoolean("allow_screenshots", false);
            Log.d(TAG, "Screenshots enabled: " + screenshotsEnabled);
            binding.switchAllowScreenshots.setChecked(screenshotsEnabled);

            // Clipboard Switch
            boolean clipboardEnabled = sharedPreferences.getBoolean("auto_clear_clipboard", false);
            Log.d(TAG, "Clipboard clear enabled: " + clipboardEnabled);
            SwitchMaterial switchClipboard = binding.getRoot().findViewById(R.id.switch_auto_clear_clipboard);
            if (switchClipboard != null) {
                switchClipboard.setChecked(clipboardEnabled);
                updateClipboardTimerVisibility(clipboardEnabled);
            }

            // Clipboard Timer Value
            long clipboardTimer = sharedPreferences.getLong("clipboard_timer", 60000L);
            Log.d(TAG, "Clipboard timer: " + clipboardTimer);
            TextView tvClipboardTimer = binding.getRoot().findViewById(R.id.tv_clipboard_timer_value);
            if (tvClipboardTimer != null) {
                tvClipboardTimer.setText(getTimeoutDisplayText(clipboardTimer, clipboardTimerOptions, clipboardTimerValues));
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in initializeViews: " + e.getMessage(), e);
        }
    }

    private void setupListeners() {
        try {
            // App Lock Switch Listener (basic toggle only)
            binding.switchAppLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "App lock switch changed: " + isChecked);
                sharedPreferences.edit().putBoolean("app_lock", isChecked).apply();

                Toast.makeText(requireContext(),
                    "App Lock " + (isChecked ? "Enabled" : "Disabled"),
                    Toast.LENGTH_SHORT).show();
            });

            // Screenshot Switch Listener
            binding.switchAllowScreenshots.setOnCheckedChangeListener((buttonView, isChecked) -> {
                Log.d(TAG, "Screenshot switch changed: " + isChecked);
                if (isChecked) {
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Enable Screenshots?")
                            .setMessage("Enabling screenshots may expose sensitive information. Are you sure?")
                            .setPositiveButton("Got it", (dialog, which) -> {
                                setAllowScreenshots(true);
                                requireActivity().recreate();
                            })
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> binding.switchAllowScreenshots.setChecked(false))
                            .show();
                } else {
                    setAllowScreenshots(false);
                    requireActivity().recreate();
                }
            });

            // Clipboard Switch Listener
            SwitchMaterial switchClipboard = binding.getRoot().findViewById(R.id.switch_auto_clear_clipboard);
            if (switchClipboard != null) {
                switchClipboard.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    Log.d(TAG, "Clipboard switch changed: " + isChecked);
                    sharedPreferences.edit().putBoolean("auto_clear_clipboard", isChecked).apply();
                    updateClipboardTimerVisibility(isChecked);

                    securityManager.setClipboardClearEnabled(isChecked);

                    Toast.makeText(requireContext(),
                        "Auto-clear Clipboard " + (isChecked ? "Enabled" : "Disabled"),
                        Toast.LENGTH_SHORT).show();
                });
            }

            // Clipboard Timer Click Listener
            LinearLayout layoutClipboardTimer = binding.getRoot().findViewById(R.id.layout_clipboard_timer);
            if (layoutClipboardTimer != null) {
                layoutClipboardTimer.setOnClickListener(v -> {
                    Log.d(TAG, "Clipboard timer clicked");
                    showClipboardTimerDialog();
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in setupListeners: " + e.getMessage(), e);
        }
    }

    private void updateClipboardTimerVisibility(boolean visible) {
        try {
            LinearLayout layoutClipboardTimer = binding.getRoot().findViewById(R.id.layout_clipboard_timer);
            if (layoutClipboardTimer != null) {
                layoutClipboardTimer.setVisibility(visible ? View.VISIBLE : View.GONE);
            }

            Log.d(TAG, "Clipboard timer visibility: " + visible);
        } catch (Exception e) {
            Log.e(TAG, "Error updating clipboard timer visibility: " + e.getMessage(), e);
        }
    }

    private void showClipboardTimerDialog() {
        try {
            long currentTimer = sharedPreferences.getLong("clipboard_timer", 60000L);
            int selectedIndex = getSelectedIndex(currentTimer, clipboardTimerValues);

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Auto-clear Clipboard")
                    .setSingleChoiceItems(clipboardTimerOptions, selectedIndex, (dialog, which) -> {
                        long selectedTimer = clipboardTimerValues[which];
                        sharedPreferences.edit().putLong("clipboard_timer", selectedTimer).apply();

                        TextView tvClipboardTimer = binding.getRoot().findViewById(R.id.tv_clipboard_timer_value);
                        if (tvClipboardTimer != null) {
                            tvClipboardTimer.setText(clipboardTimerOptions[which]);
                        }

                        securityManager.updateClipboardTimer(selectedTimer);

                        Log.d(TAG, "Clipboard timer updated: " + selectedTimer);
                        Toast.makeText(requireContext(), "Clipboard timer: " + clipboardTimerOptions[which], Toast.LENGTH_SHORT).show();

                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing clipboard timer dialog: " + e.getMessage(), e);
        }
    }

    private int getSelectedIndex(long currentValue, long[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                return i;
            }
        }
        return 0;
    }

    private String getTimeoutDisplayText(long timeout, String[] options, long[] values) {
        int index = getSelectedIndex(timeout, values);
        return options[index];
    }

    private void setAllowScreenshots(boolean allow) {
        sharedPreferences.edit().putBoolean("allow_screenshots", allow).apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
