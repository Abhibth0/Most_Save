package com.example.mostsave.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.mostsave.R;
import com.example.mostsave.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup click listener for Appearance settings
        binding.textViewAppearanceSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_settings_to_nav_appearance);
        });

        binding.textViewSecuritySettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_settings_to_nav_security_settings);
        });

        binding.textViewAdvanceSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_settings_to_nav_advance_settings);
        });

        binding.textViewAboutMostSave.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_nav_settings_to_nav_about_most_save);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
