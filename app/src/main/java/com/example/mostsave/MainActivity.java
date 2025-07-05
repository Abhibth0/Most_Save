package com.example.mostsave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mostsave.utils.SecurityManager;
import com.google.android.material.navigation.NavigationView;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawerLayout;
    private NavController navController;
    private TextView toolbarTitleTextView;
    private Toolbar toolbar;
    private SecurityManager securityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before UI is created
        SharedPreferences themePrefs = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int theme = themePrefs.getInt("theme", R.style.Theme_MostSave);
        setTheme(theme);

        if (themePrefs.getBoolean("dark_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);

        // Initialize SecurityManager
        securityManager = SecurityManager.getInstance(this);

        // Set screenshot flag based on preferences
        SharedPreferences sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("allow_screenshots", false)) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        }

        setContentView(R.layout.activity_main);

        // Check and show Biometric prompt if app lock is enabled and it's a fresh start
        if (savedInstanceState == null && sharedPreferences.getBoolean("app_lock", false)) {
            showBiometricPrompt();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitleTextView = findViewById(R.id.toolbar_title);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_favorites, R.id.nav_categories, R.id.nav_recycle_bin,
                R.id.nav_settings, R.id.nav_history, R.id.nav_analyze_passwords, R.id.nav_suggest_idea)
                .setOpenableLayout(drawerLayout)
                .build();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (toolbarTitleTextView != null && getSupportActionBar() != null) {
                    if (destination.getId() == R.id.nav_home) {
                        toolbarTitleTextView.setText(R.string.app_name);
                        toolbarTitleTextView.setVisibility(View.VISIBLE);
                        getSupportActionBar().setDisplayShowTitleEnabled(false);
                    } else {
                        toolbarTitleTextView.setVisibility(View.GONE);
                        getSupportActionBar().setDisplayShowTitleEnabled(true);
                    }
                }
            });
        } else {
            Log.e("MainActivity", "NavHostFragment not found! Cannot setup NavigationUI.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // SecurityManager will automatically handle clipboard functionality
    }

    @Override
    protected void onPause() {
        super.onPause();
        // SecurityManager will automatically handle clipboard functionality
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (securityManager != null) {
            securityManager.cleanup();
        }
    }

    public void setMultiSelect(boolean active) {
        if (active) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            toolbar.setNavigationIcon(null);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        }
    }

    public void setToolbarTitleVisibility(int visibility) {
        if (toolbarTitleTextView != null) {
            toolbarTitleTextView.setVisibility(visibility);
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        getString(R.string.biometric_auth_error_prefix) + errString, Toast.LENGTH_SHORT)
                        .show();
                // If user cancels or there's an error, close the app.
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // Authentication Succeeded, user can proceed.
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // Authentication failed
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_auth_title))
                .setSubtitle(getString(R.string.biometric_auth_subtitle))
                .setDescription(getString(R.string.biometric_auth_description))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo);
        } else {
            Toast.makeText(this, R.string.biometric_auth_error_none_enrolled, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (navController == null) {
            return super.onSupportNavigateUp();
        }
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.START)) {
            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
