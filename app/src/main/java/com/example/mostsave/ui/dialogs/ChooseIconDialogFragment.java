package com.example.mostsave.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.mostsave.R;
import com.example.mostsave.databinding.DialogChooseIconBinding;
import com.example.mostsave.ui.adapters.IconAdapter;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChooseIconDialogFragment extends DialogFragment {

    private DialogChooseIconBinding binding;
    private OnIconSelectedListener onIconSelectedListener;
    private IconAdapter iconAdapter;
    private List<IconItem> allIcons;
    private List<IconItem> filteredIcons;
    private int selectedIconResId = 0;
    private String selectedIconName = "";
    private IconCategory currentCategory = IconCategory.ALL;

    public enum IconCategory {
        ALL, BUSINESS, SOCIAL, ENTERTAINMENT, UTILITY, CUSTOM
    }

    public static class IconItem {
        public int iconResId;
        public String iconName;
        public IconCategory category;

        public IconItem(int iconResId, String iconName, IconCategory category) {
            this.iconResId = iconResId;
            this.iconName = iconName;
            this.category = category;
        }
    }

    public interface OnIconSelectedListener {
        void onIconSelected(int iconResId, String iconName);
        void onUploadCustomIcon();
    }

    public static ChooseIconDialogFragment newInstance() {
        return new ChooseIconDialogFragment();
    }

    public void setOnIconSelectedListener(OnIconSelectedListener listener) {
        this.onIconSelectedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogChooseIconBinding.inflate(getLayoutInflater());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(binding.getRoot());

        setupViews();
        setupRecyclerView();
        setupSearchFunctionality();
        setupCategoryChips();
        setupActionButtons();
        loadIcons();

        return builder.create();
    }

    private void setupViews() {
        // Initially hide selected preview
        binding.layoutSelectedPreview.setVisibility(View.GONE);

        // Show loading state
        showLoadingState();
    }

    private void setupRecyclerView() {
        binding.recyclerViewIcons.setLayoutManager(new GridLayoutManager(getContext(), 3));

        iconAdapter = new IconAdapter(new ArrayList<>(), new IconAdapter.OnIconClickListener() {
            @Override
            public void onIconClick(IconItem iconItem) {
                selectIcon(iconItem);
            }
        });

        binding.recyclerViewIcons.setAdapter(iconAdapter);
    }

    private void setupSearchFunctionality() {
        binding.editSearchIcons.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterIcons(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategoryChips() {
        binding.chipAll.setOnClickListener(v -> filterByCategory(IconCategory.ALL));
        binding.chipBusiness.setOnClickListener(v -> filterByCategory(IconCategory.BUSINESS));
        binding.chipSocial.setOnClickListener(v -> filterByCategory(IconCategory.SOCIAL));
        binding.chipEntertainment.setOnClickListener(v -> filterByCategory(IconCategory.ENTERTAINMENT));
        binding.chipUtility.setOnClickListener(v -> filterByCategory(IconCategory.UTILITY));
        binding.chipCustom.setOnClickListener(v -> filterByCategory(IconCategory.CUSTOM));
    }

    private void setupActionButtons() {
        binding.btnCloseDialog.setOnClickListener(v -> dismiss());

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnSelectIcon.setOnClickListener(v -> {
            if (selectedIconResId != 0 && onIconSelectedListener != null) {
                onIconSelectedListener.onIconSelected(selectedIconResId, selectedIconName);
                dismiss();
            }
        });

        binding.btnUploadCustom.setOnClickListener(v -> {
            if (onIconSelectedListener != null) {
                onIconSelectedListener.onUploadCustomIcon();
                dismiss();
            }
        });

        binding.btnRetryLoading.setOnClickListener(v -> {
            loadIcons();
        });
    }

    private void loadIcons() {
        showLoadingState();

        // Simulate loading delay
        binding.getRoot().postDelayed(() -> {
            try {
                allIcons = generateIconList();
                filteredIcons = new ArrayList<>(allIcons);

                if (allIcons.isEmpty()) {
                    showEmptyState();
                } else {
                    showIconsState();
                    iconAdapter.updateIcons(filteredIcons);
                }
            } catch (Exception e) {
                showErrorState();
            }
        }, 500);
    }

    private List<IconItem> generateIconList() {
        List<IconItem> icons = new ArrayList<>();

        // Business/Technology icons
        icons.add(new IconItem(R.drawable.microsoft, "Microsoft", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.apple, "Apple", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.paypal, "PayPal", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.wordpress, "WordPress", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.samsung, "Samsung", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.wikipedia, "Wikipedia", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.ebay, "eBay", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.github, "GitHub", IconCategory.BUSINESS));
        icons.add(new IconItem(R.drawable.binance, "Binance", IconCategory.BUSINESS));

        // Social Media icons
        icons.add(new IconItem(R.drawable.facebook, "Facebook", IconCategory.SOCIAL));
        icons.add(new IconItem(R.drawable.instagram, "Instagram", IconCategory.SOCIAL));
        icons.add(new IconItem(R.drawable.twitter, "Twitter", IconCategory.SOCIAL));
        icons.add(new IconItem(R.drawable.linkedin, "LinkedIn", IconCategory.SOCIAL));
        icons.add(new IconItem(R.drawable.snapchat, "Snapchat", IconCategory.SOCIAL));
        icons.add(new IconItem(R.drawable.reddit, "Reddit", IconCategory.SOCIAL));

        // Entertainment icons
        icons.add(new IconItem(R.drawable.netflix, "Netflix", IconCategory.ENTERTAINMENT));

        // Gaming/Utility icons
        icons.add(new IconItem(R.drawable.xbox, "Xbox", IconCategory.UTILITY));
        icons.add(new IconItem(R.drawable.playstation, "PlayStation", IconCategory.UTILITY));
        icons.add(new IconItem(R.drawable.gmail, "Gmail", IconCategory.UTILITY));
        icons.add(new IconItem(R.drawable.chatgpt, "ChatGPT", IconCategory.UTILITY));

        return icons;
    }

    private int getIconResourceId(String iconName) {
        try {
            return getResources().getIdentifier(iconName, "drawable", requireContext().getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }

    private void selectIcon(IconItem iconItem) {
        selectedIconResId = iconItem.iconResId;
        selectedIconName = iconItem.iconName;

        // Update selected preview
        binding.imgSelectedIconPreview.setImageResource(iconItem.iconResId);
        binding.txtSelectedIconName.setText(iconItem.iconName);
        binding.layoutSelectedPreview.setVisibility(View.VISIBLE);

        // Enable select button and change its background to theme primary color
        binding.btnSelectIcon.setEnabled(true);

        // Use TypedValue to get theme primary color properly
        android.util.TypedValue typedValue = new android.util.TypedValue();
        requireContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        binding.btnSelectIcon.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primaryColor));

        // Set text color to white when enabled
        binding.btnSelectIcon.setTextColor(android.graphics.Color.WHITE);

        // Update adapter selection
        iconAdapter.setSelectedIcon(iconItem);
    }

    private void filterIcons(String query) {
        if (allIcons == null) return;

        filteredIcons = allIcons.stream()
            .filter(icon -> {
                boolean matchesQuery = query.isEmpty() ||
                    icon.iconName.toLowerCase().contains(query.toLowerCase());
                boolean matchesCategory = currentCategory == IconCategory.ALL ||
                    icon.category == currentCategory;
                return matchesQuery && matchesCategory;
            })
            .collect(Collectors.toList());

        if (filteredIcons.isEmpty()) {
            showEmptyState();
        } else {
            showIconsState();
            iconAdapter.updateIcons(filteredIcons);
        }
    }

    private void filterByCategory(IconCategory category) {
        currentCategory = category;
        filterIcons(binding.editSearchIcons.getText().toString());
    }

    private void showLoadingState() {
        binding.recyclerViewIcons.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.layoutLoading.setVisibility(View.VISIBLE);
    }

    private void showIconsState() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.recyclerViewIcons.setVisibility(View.VISIBLE);
    }

    private void showEmptyState() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerViewIcons.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void showErrorState() {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.recyclerViewIcons.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
        binding.layoutErrorState.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
