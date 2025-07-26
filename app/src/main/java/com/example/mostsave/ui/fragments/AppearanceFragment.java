package com.example.mostsave.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.mostsave.R;
import com.example.mostsave.databinding.FragmentAppearanceBinding;

public class AppearanceFragment extends Fragment {

    private FragmentAppearanceBinding binding;
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppearanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireActivity().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);

        setupDarkModeSwitch();
        setupCustomColorSwitch();
        setupColorSelection();
    }

    private void setupCustomColorSwitch() {
        boolean useCustomColor = sharedPreferences.getBoolean("use_custom_color", false);
        binding.switchUseCustomColor.setChecked(useCustomColor);
        binding.colorOptionsContainer.setVisibility(useCustomColor ? View.VISIBLE : View.GONE);

        binding.switchUseCustomColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean("use_custom_color", isChecked).apply();
            binding.colorOptionsContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                // If custom colors are turned off, revert to the default theme
                selectDefaultTheme();
            }
        });
    }

    private void setupDarkModeSwitch() {
        binding.switchThemeMode.setChecked(isDarkModeEnabled());

        binding.switchThemeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply();
        });
    }

    private boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean("dark_mode", false);
    }

    private void setupColorSelection() {
        binding.colorDefault.setOnClickListener(v -> selectDefaultTheme());
        binding.colorBlue.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Blue));
        binding.colorOrange.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Orange));
        binding.colorRed.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Red));
        binding.colorPink.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Pink));
        binding.colorTeal.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Teal));
        binding.colorGreen.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Green));
        binding.colorLightGreen.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_LightGreen));
        binding.colorLime.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Lime));
        binding.colorYellow.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Yellow));
        binding.colorAmber.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Amber));
        binding.colorDeepOrange.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_DeepOrange));
        binding.colorBrown.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Brown));
        binding.colorGrey.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Grey));
        binding.colorBlueGrey.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_BlueGrey));
        binding.colorIndigo.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Indigo));
        binding.colorPurple.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Purple));
        binding.colorDeepPurple.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_DeepPurple));
        binding.colorCyan.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_Cyan));
        binding.colorLightBlue.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_LightBlue));
        binding.colorLightPink.setOnClickListener(v -> selectColor(R.style.Theme_MostSave_LightPink));
        updateSelectedColorUI();
    }

    private void selectDefaultTheme() {
        sharedPreferences.edit().remove("theme").apply();
        requireActivity().recreate();
    }

    private void updateSelectedColorUI() {
        int themeId = sharedPreferences.getInt("theme", -1);
        int strokeWidthInPx = (int) (2 * requireContext().getResources().getDisplayMetrics().density);

        // Reset all selections
        binding.iconDefaultSelected.setVisibility(View.GONE);
        binding.colorDefault.setStrokeWidth(0);
        binding.iconBlueSelected.setVisibility(View.GONE);
        binding.colorBlue.setStrokeWidth(0);
        binding.iconOrangeSelected.setVisibility(View.GONE);
        binding.colorOrange.setStrokeWidth(0);
        binding.iconRedSelected.setVisibility(View.GONE);
        binding.colorRed.setStrokeWidth(0);
        binding.iconPinkSelected.setVisibility(View.GONE);
        binding.colorPink.setStrokeWidth(0);
        binding.iconTealSelected.setVisibility(View.GONE);
        binding.colorTeal.setStrokeWidth(0);
        binding.iconGreenSelected.setVisibility(View.GONE);
        binding.colorGreen.setStrokeWidth(0);
        binding.iconLightGreenSelected.setVisibility(View.GONE);
        binding.colorLightGreen.setStrokeWidth(0);
        binding.iconLimeSelected.setVisibility(View.GONE);
        binding.colorLime.setStrokeWidth(0);
        binding.iconYellowSelected.setVisibility(View.GONE);
        binding.colorYellow.setStrokeWidth(0);
        binding.iconAmberSelected.setVisibility(View.GONE);
        binding.colorAmber.setStrokeWidth(0);
        binding.iconDeepOrangeSelected.setVisibility(View.GONE);
        binding.colorDeepOrange.setStrokeWidth(0);
        binding.iconBrownSelected.setVisibility(View.GONE);
        binding.colorBrown.setStrokeWidth(0);
        binding.iconGreySelected.setVisibility(View.GONE);
        binding.colorGrey.setStrokeWidth(0);
        binding.iconBlueGreySelected.setVisibility(View.GONE);
        binding.colorBlueGrey.setStrokeWidth(0);
        binding.iconIndigoSelected.setVisibility(View.GONE);
        binding.colorIndigo.setStrokeWidth(0);
        binding.iconPurpleSelected.setVisibility(View.GONE);
        binding.colorPurple.setStrokeWidth(0);
        binding.iconDeepPurpleSelected.setVisibility(View.GONE);
        binding.colorDeepPurple.setStrokeWidth(0);
        binding.iconCyanSelected.setVisibility(View.GONE);
        binding.colorCyan.setStrokeWidth(0);
        binding.iconLightBlueSelected.setVisibility(View.GONE);
        binding.colorLightBlue.setStrokeWidth(0);
        binding.iconLightPinkSelected.setVisibility(View.GONE);
        binding.colorLightPink.setStrokeWidth(0);

        if (themeId == -1) {
            binding.iconDefaultSelected.setVisibility(View.VISIBLE);
            binding.colorDefault.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Blue) {
            binding.iconBlueSelected.setVisibility(View.VISIBLE);
            binding.colorBlue.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Orange) {
            binding.iconOrangeSelected.setVisibility(View.VISIBLE);
            binding.colorOrange.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Red) {
            binding.iconRedSelected.setVisibility(View.VISIBLE);
            binding.colorRed.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Pink) {
            binding.iconPinkSelected.setVisibility(View.VISIBLE);
            binding.colorPink.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Teal) {
            binding.iconTealSelected.setVisibility(View.VISIBLE);
            binding.colorTeal.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Green) {
            binding.iconGreenSelected.setVisibility(View.VISIBLE);
            binding.colorGreen.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_LightGreen) {
            binding.iconLightGreenSelected.setVisibility(View.VISIBLE);
            binding.colorLightGreen.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Lime) {
            binding.iconLimeSelected.setVisibility(View.VISIBLE);
            binding.colorLime.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Yellow) {
            binding.iconYellowSelected.setVisibility(View.VISIBLE);
            binding.colorYellow.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Amber) {
            binding.iconAmberSelected.setVisibility(View.VISIBLE);
            binding.colorAmber.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_DeepOrange) {
            binding.iconDeepOrangeSelected.setVisibility(View.VISIBLE);
            binding.colorDeepOrange.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Brown) {
            binding.iconBrownSelected.setVisibility(View.VISIBLE);
            binding.colorBrown.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Grey) {
            binding.iconGreySelected.setVisibility(View.VISIBLE);
            binding.colorGrey.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_BlueGrey) {
            binding.iconBlueGreySelected.setVisibility(View.VISIBLE);
            binding.colorBlueGrey.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Indigo) {
            binding.iconIndigoSelected.setVisibility(View.VISIBLE);
            binding.colorIndigo.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Purple) {
            binding.iconPurpleSelected.setVisibility(View.VISIBLE);
            binding.colorPurple.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_DeepPurple) {
            binding.iconDeepPurpleSelected.setVisibility(View.VISIBLE);
            binding.colorDeepPurple.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_Cyan) {
            binding.iconCyanSelected.setVisibility(View.VISIBLE);
            binding.colorCyan.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_LightBlue) {
            binding.iconLightBlueSelected.setVisibility(View.VISIBLE);
            binding.colorLightBlue.setStrokeWidth(strokeWidthInPx);
        } else if (themeId == R.style.Theme_MostSave_LightPink) {
            binding.iconLightPinkSelected.setVisibility(View.VISIBLE);
            binding.colorLightPink.setStrokeWidth(strokeWidthInPx);
        }
    }

    private void selectColor(int themeId) {
        sharedPreferences.edit().putInt("theme", themeId).apply();
        requireActivity().recreate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
