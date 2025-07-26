package com.example.mostsave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;
    private Button btnNext;
    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.onboarding_viewpager);
        btnNext = findViewById(R.id.btn_next);
        btnGetStarted = findViewById(R.id.btn_get_started);

        List<OnboardingPage> pages = new ArrayList<>();
        pages.add(new OnboardingPage(
                "Welcome to Most Save!",
                "Easily and securely store all your passwords in one place. No more forgetting or losing your important credentials. Your privacy is always protected with us",
                R.drawable.logo)); // Set logo for first page
        pages.add(new OnboardingPage(
                "Powerful Organization",
                "Group your passwords by categories, mark favorites, and quickly find what you need. Use biometric lock for extra security and peace of mind",
                0));
        pages.add(new OnboardingPage(
                "Get Started Now",
                "Tap 'Get Started' to begin using Most Save. Enjoy a simple, safe, and smart way to manage your passwords!",
                0));

        adapter = new OnboardingAdapter(pages);
        viewPager.setAdapter(adapter);

        btnNext.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() < pages.size() - 1) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });

        btnGetStarted.setOnClickListener(v -> {
            SharedPreferences walkthroughPrefs = getSharedPreferences("walkthrough_prefs", MODE_PRIVATE);
            walkthroughPrefs.edit().putBoolean("is_first_launch", false).apply();
            startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
            finish();
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == pages.size() - 1) {
                    btnNext.setVisibility(View.GONE);
                    btnGetStarted.setVisibility(View.VISIBLE);
                } else {
                    btnNext.setVisibility(View.VISIBLE);
                    btnGetStarted.setVisibility(View.GONE);
                }
            }
        });
    }
}
