package com.example.mostsave.ui.fragments;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;
import com.example.mostsave.adapters.PasswordAnalysisAdapter;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.util.PasswordAnalyzer;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class PasswordAnalysisFragment extends Fragment {

    private PasswordViewModel passwordViewModel;
    // Security score views
    private TextView securityScoreText, securityStatusText, securityRecommendationText;
    private CircularProgressIndicator securityScoreProgress;
    // Existing views
    private TextView totalPasswordsText, weakPasswordsCountText, strongPasswordsCountText, needsUpdateCountText;
    private TextView noWeakPasswordsText, noNeedsUpdateText, noDuplicatePasswordsText, noMissingInfoText;
    private TextView weakPercentText, mediumPercentText, strongPercentText;
    private RecyclerView weakPasswordsRecycler, needsUpdateRecycler, duplicatePasswordsRecycler, missingInfoRecycler;
    private PasswordAnalysisAdapter weakPasswordsAdapter, needsUpdateAdapter, duplicatePasswordsAdapter, missingInfoAdapter;
    private PieChart passwordStrengthChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_password_analysis, container, false);

        initializeViews(root);
        setupRecyclerViews();
        setupChart();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordViewModel = new ViewModelProvider(requireActivity()).get(PasswordViewModel.class);
        observePasswords();
    }

    private void initializeViews(View root) {
        // Security score views
        securityScoreText = root.findViewById(R.id.text_security_score);
        securityStatusText = root.findViewById(R.id.text_security_status);
        securityRecommendationText = root.findViewById(R.id.text_security_recommendation);
        securityScoreProgress = root.findViewById(R.id.progress_security_score);

        // Summary views
        totalPasswordsText = root.findViewById(R.id.text_total_passwords);
        weakPasswordsCountText = root.findViewById(R.id.text_weak_passwords_count);
        strongPasswordsCountText = root.findViewById(R.id.text_strong_passwords_count);
        needsUpdateCountText = root.findViewById(R.id.text_needs_update_count);

        // Percentage views
        weakPercentText = root.findViewById(R.id.text_weak_percent);
        mediumPercentText = root.findViewById(R.id.text_medium_percent);
        strongPercentText = root.findViewById(R.id.text_strong_percent);

        // Chart
        passwordStrengthChart = root.findViewById(R.id.chart_password_strength);

        // RecyclerViews
        weakPasswordsRecycler = root.findViewById(R.id.recycler_weak_passwords);
        needsUpdateRecycler = root.findViewById(R.id.recycler_needs_update);
        duplicatePasswordsRecycler = root.findViewById(R.id.recycler_duplicate_passwords);
        missingInfoRecycler = root.findViewById(R.id.recycler_missing_info);

        // Empty state texts
        noWeakPasswordsText = root.findViewById(R.id.text_no_weak_passwords);
        noNeedsUpdateText = root.findViewById(R.id.text_no_needs_update);
        noDuplicatePasswordsText = root.findViewById(R.id.text_no_duplicate_passwords);
        noMissingInfoText = root.findViewById(R.id.text_no_missing_info);
    }

    private void setupRecyclerViews() {
        // Create adapters
        weakPasswordsAdapter = new PasswordAnalysisAdapter(password -> navigateToEditPassword(password.id));
        needsUpdateAdapter = new PasswordAnalysisAdapter(password -> navigateToEditPassword(password.id));
        duplicatePasswordsAdapter = new PasswordAnalysisAdapter(password -> navigateToEditPassword(password.id));
        missingInfoAdapter = new PasswordAnalysisAdapter(password -> navigateToEditPassword(password.id));

        // Setup RecyclerViews
        setupRecyclerView(weakPasswordsRecycler, weakPasswordsAdapter);
        setupRecyclerView(needsUpdateRecycler, needsUpdateAdapter);
        setupRecyclerView(duplicatePasswordsRecycler, duplicatePasswordsAdapter);
        setupRecyclerView(missingInfoRecycler, missingInfoAdapter);
    }

    private void setupRecyclerView(RecyclerView recyclerView, PasswordAnalysisAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void setupChart() {
        // Configure the pie chart appearance
        passwordStrengthChart.getDescription().setEnabled(false);
        passwordStrengthChart.setUsePercentValues(true);
        passwordStrengthChart.setDrawHoleEnabled(true);
        passwordStrengthChart.setHoleColor(Color.TRANSPARENT);
        passwordStrengthChart.setHoleRadius(40f);
        passwordStrengthChart.setTransparentCircleRadius(45f);
        passwordStrengthChart.setRotationEnabled(true);
        passwordStrengthChart.setHighlightPerTapEnabled(true);

        // Disable legend (we have our own custom legend below the chart)
        Legend legend = passwordStrengthChart.getLegend();
        legend.setEnabled(false);

        // Center text
        passwordStrengthChart.setDrawCenterText(true);
        passwordStrengthChart.setCenterText("Password\nStrength");
        passwordStrengthChart.setCenterTextSize(14f);

        // Set center text color based on theme
        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(android.R.attr.textColorPrimary, typedValue, true);
        int textColor = ContextCompat.getColor(requireContext(), typedValue.resourceId);
        passwordStrengthChart.setCenterTextColor(textColor);
    }

    private void observePasswords() {
        passwordViewModel.getAllPasswords().observe(getViewLifecycleOwner(), this::analyzePasswords);
    }

    private void analyzePasswords(List<Password> passwords) {
        if (passwords == null) return;

        int totalPasswords = passwords.size();

        // Find weak passwords
        List<Password> weakPasswords = PasswordAnalyzer.findWeakPasswords(passwords);

        // Find passwords needing update
        List<Password> needsUpdatePasswords = PasswordAnalyzer.findPasswordsNeedingUpdate(passwords);

        // Find duplicate passwords
        List<Password> duplicatePasswords = PasswordAnalyzer.findDuplicatePasswords(passwords);

        // Find passwords with missing information
        List<Password> missingInfoPasswords = PasswordAnalyzer.findPasswordsWithMissingInfo(passwords);

        // Count password strength distribution
        int strongPasswordsCount = 0;
        int mediumPasswordsCount = 0;
        int weakPasswordsCount = weakPasswords.size();

        for (Password password : passwords) {
            PasswordAnalyzer.PasswordStrength strength = PasswordAnalyzer.getPasswordStrength(password.password);
            if (strength == PasswordAnalyzer.PasswordStrength.STRONG) {
                strongPasswordsCount++;
            } else if (strength == PasswordAnalyzer.PasswordStrength.MEDIUM) {
                mediumPasswordsCount++;
            }
        }

        // Calculate overall security score
        calculateSecurityScore(totalPasswords, weakPasswordsCount, mediumPasswordsCount,
                              strongPasswordsCount, needsUpdatePasswords.size(),
                              duplicatePasswords.size(), missingInfoPasswords.size());

        // Update summary statistics
        totalPasswordsText.setText(String.valueOf(totalPasswords));
        weakPasswordsCountText.setText(String.valueOf(weakPasswordsCount));
        strongPasswordsCountText.setText(String.valueOf(strongPasswordsCount));
        needsUpdateCountText.setText(String.valueOf(needsUpdatePasswords.size()));

        // Update pie chart with strength distribution
        updateStrengthChart(weakPasswordsCount, mediumPasswordsCount, strongPasswordsCount, totalPasswords);

        // Update weak passwords list
        updateRecyclerView(weakPasswordsAdapter, weakPasswordsRecycler, weakPasswords, noWeakPasswordsText);

        // Update needs update list
        updateRecyclerView(needsUpdateAdapter, needsUpdateRecycler, needsUpdatePasswords, noNeedsUpdateText);

        // Update duplicate passwords list
        updateRecyclerView(duplicatePasswordsAdapter, duplicatePasswordsRecycler, duplicatePasswords, noDuplicatePasswordsText);

        // Update missing info list
        updateRecyclerView(missingInfoAdapter, missingInfoRecycler, missingInfoPasswords, noMissingInfoText);
    }

    /**
     * Calculate and display the overall security score based on password analysis
     */
    private void calculateSecurityScore(int totalPasswords, int weakCount, int mediumCount,
                                       int strongCount, int needsUpdateCount,
                                       int duplicatesCount, int missingInfoCount) {
        // Avoid division by zero
        if (totalPasswords == 0) {
            securityScoreText.setText("N/A");
            securityStatusText.setText("No data");
            securityRecommendationText.setText("Add passwords to see your security score");
            securityScoreProgress.setProgress(0);
            return;
        }

        // Calculate individual factor scores (0-100)
        float strengthScore = (float) (strongCount * 100 + mediumCount * 60) / totalPasswords;
        float updatedScore = 100 - ((float) needsUpdateCount / totalPasswords * 100);
        float duplicatesScore = 100 - ((float) duplicatesCount / totalPasswords * 100);
        float completeInfoScore = 100 - ((float) missingInfoCount / totalPasswords * 100);

        // Calculate weighted overall score (0-100)
        // Weight: Strength 40%, Age 25%, Duplicates 25%, Completeness 10%
        int overallScore = (int) (strengthScore * 0.4f + updatedScore * 0.25f +
                                duplicatesScore * 0.25f + completeInfoScore * 0.1f);

        // Ensure score is within 0-100 range
        overallScore = Math.max(0, Math.min(100, overallScore));

        // Update UI with animated progress
        securityScoreText.setText(overallScore + "%");
        animateProgressBar(securityScoreProgress, overallScore);

        // Set color based on score
        int color;
        String status;
        String recommendation;

        if (overallScore >= 80) {
            color = Color.rgb(76, 175, 80); // Green
            status = "Excellent";

            if (needsUpdateCount > 0) {
                recommendation = "Update old passwords to maintain security";
            } else if (duplicatesCount > 0) {
                recommendation = "Fix duplicate passwords to improve security";
            } else {
                recommendation = "Your passwords are well-maintained";
            }

        } else if (overallScore >= 60) {
            color = Color.rgb(255, 165, 0); // Orange
            status = "Good";

            if (weakCount > 0) {
                recommendation = "Replace weak passwords to improve security";
            } else if (needsUpdateCount > 0) {
                recommendation = "Update old passwords to improve security";
            } else {
                recommendation = "Fix duplicate passwords to improve security";
            }

        } else {
            color = Color.rgb(255, 69, 69); // Red
            status = "Poor";

            if (weakCount > 0) {
                recommendation = "Many weak passwords need to be replaced";
            } else if (duplicatesCount > 0) {
                recommendation = "Too many duplicate passwords are a security risk";
            } else {
                recommendation = "Update your passwords regularly for better security";
            }
        }

        securityScoreProgress.setIndicatorColor(color);
        securityStatusText.setText(status);
        securityRecommendationText.setText(recommendation);
    }

    /**
     * Animate progress bar filling smoothly
     */
    private void animateProgressBar(CircularProgressIndicator progressBar, int targetProgress) {
        ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress);
        animator.setDuration(1500); // 1.5 seconds animation
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void updateStrengthChart(int weakCount, int mediumCount, int strongCount, int totalCount) {
        // Skip if there are no passwords
        if (totalCount == 0) {
            passwordStrengthChart.setVisibility(View.GONE);
            return;
        }

        passwordStrengthChart.setVisibility(View.VISIBLE);

        // Calculate percentages for display labels
        float weakPercent = (float) weakCount / totalCount * 100;
        float mediumPercent = (float) mediumCount / totalCount * 100;
        float strongPercent = (float) strongCount / totalCount * 100;

        // Set percentage texts
        weakPercentText.setText(String.format("%.1f%%", weakPercent));
        mediumPercentText.setText(String.format("%.1f%%", mediumPercent));
        strongPercentText.setText(String.format("%.1f%%", strongPercent));

        // Prepare pie chart data
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Only add entries for non-zero counts to avoid empty segments
        if (weakCount > 0) {
            entries.add(new PieEntry(weakCount, getString(R.string.analysis_password_strength_low)));
            colors.add(Color.rgb(255, 69, 69)); // Red for weak
        }

        if (mediumCount > 0) {
            entries.add(new PieEntry(mediumCount, getString(R.string.analysis_password_strength_medium)));
            colors.add(Color.rgb(255, 165, 0)); // Orange for medium
        }

        if (strongCount > 0) {
            entries.add(new PieEntry(strongCount, getString(R.string.analysis_password_strength_high)));
            colors.add(Color.rgb(76, 175, 80)); // Green for strong
        }

        // Create dataset
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);

        dataSet.setValueTextSize(14f);

        // Determine if we're in dark mode
        int nightModeFlags = getResources().getConfiguration().uiMode &
                             android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // Set value text color based on theme - explicitly use black for light theme and white for dark
        dataSet.setValueTextColor(isDarkMode ? Color.WHITE : Color.BLACK);

        dataSet.setValueFormatter(new PercentFormatter(passwordStrengthChart));

        // Make sure labels are positioned properly so they're fully visible
        // Set text position based on whether it's inside or outside the slice
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

        dataSet.setSliceSpace(3f);

        // Create pie data and set it to chart
        PieData data = new PieData(dataSet);
        passwordStrengthChart.setData(data);

        // Refresh chart
        passwordStrengthChart.invalidate();
    }

    private void updateRecyclerView(PasswordAnalysisAdapter adapter, RecyclerView recyclerView,
                                   List<Password> passwords, TextView emptyText) {
        if (passwords.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            adapter.submitList(passwords);
        }
    }

    private void navigateToEditPassword(int passwordId) {
        // Navigate to the show password screen using the passwordId
        Bundle args = new Bundle();
        args.putInt("passwordId", passwordId);
        Navigation.findNavController(requireView()).navigate(R.id.nav_show_password, args);
    }
}
