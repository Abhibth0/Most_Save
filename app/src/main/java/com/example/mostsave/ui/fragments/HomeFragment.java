package com.example.mostsave.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.mostsave.MainActivity;
import com.example.mostsave.R;
import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.ui.adapters.PasswordAdapter;
import com.example.mostsave.viewmodel.CategoryViewModel;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements PasswordAdapter.OnPasswordClickListener, PasswordAdapter.OnCopyClickListener, PasswordAdapter.OnToggleVisibilityListener, PasswordAdapter.OnPasswordLongClickListener, PasswordAdapter.OnPasswordSelectListener {

    private PasswordViewModel passwordViewModel;
    private CategoryViewModel categoryViewModel;
    private RecyclerView recyclerViewPasswords;
    private PasswordAdapter passwordAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavController navController;
    private ChipGroup chipGroupCategoriesFilter;
    private ImageView imageViewNoPasswords;
    private int selectedCategoryId = -1; // -1 for "All" categories
    private String currentSearchQuery = "";
    private PasswordViewModel.SortOrder currentSortOrder = PasswordViewModel.SortOrder.NONE;
    private boolean isMultiSelectMode = false;
    private final List<Password> selectedPasswords = new ArrayList<>();
    private OnBackPressedCallback onBackPressedCallback;
    private FloatingActionButton fabAddPassword;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        passwordViewModel.setSortOrder(currentSortOrder); // Initial sort order
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getActivity() instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.menu_home));
            }
        }

        navController = Navigation.findNavController(view);
        recyclerViewPasswords = view.findViewById(R.id.recycler_view_passwords);
        fabAddPassword = view.findViewById(R.id.fab_add_password);
        chipGroupCategoriesFilter = view.findViewById(R.id.chip_group_categories_filter);
        imageViewNoPasswords = view.findViewById(R.id.image_view_no_passwords);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // Show highlight on + button for first-time users
        SharedPreferences prefs = requireContext().getSharedPreferences("walkthrough_prefs", Context.MODE_PRIVATE);
        boolean showFabHighlight = prefs.getBoolean("show_fab_highlight", true);
        if (showFabHighlight && fabAddPassword.getVisibility() == View.VISIBLE) {
            fabAddPassword.bringToFront();
            fabAddPassword.postDelayed(() -> TapTargetView.showFor(
                requireActivity(),
                TapTarget.forView(fabAddPassword, "Add your first password", "Tap the + button to securely add a new password.")
                    .outerCircleColor(R.color.primary_color)
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextSize(22)
                    .titleTextColor(android.R.color.white)
                    .descriptionTextSize(16)
                    .descriptionTextColor(android.R.color.white)
                    .textColor(android.R.color.white)
                    .dimColor(android.R.color.black)
                    .drawShadow(true)
                    .cancelable(true)
                    .tintTarget(true)
                    .transparentTarget(true),
                new TapTargetView.Listener() {
                    @Override
                    public void onTargetDismissed(TapTargetView view, boolean userInitiated) {
                        prefs.edit().putBoolean("show_fab_highlight", false).apply();
                    }
                }
            ), 350); // 350ms delay for smoother UI
        }

        setupRecyclerView();
        setupCategoryFilters(); // Setup filters first
        loadPasswordsForCurrentFilter(); // Then load data which will observe LiveData

        fabAddPassword.setOnClickListener(v -> navController.navigate(R.id.nav_add_password));

        onBackPressedCallback = new OnBackPressedCallback(false) { // Initially disabled
            @Override
            public void handleOnBackPressed() {
                if (isMultiSelectMode) {
                    exitMultiSelectMode();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), onBackPressedCallback);
        setupMenu();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh logic here
            swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
        });
    }

    private void setupRecyclerView() {
        recyclerViewPasswords.setLayoutManager(new LinearLayoutManager(getContext()));
        passwordAdapter = new PasswordAdapter(this, this, this, this, this);
        recyclerViewPasswords.setAdapter(passwordAdapter);

        // Hide/show FAB on scroll
        recyclerViewPasswords.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && fabAddPassword.getVisibility() == View.VISIBLE) {
                    fabAddPassword.hide();
                } else if (dy < 0 && fabAddPassword.getVisibility() != View.VISIBLE) {
                    fabAddPassword.show();
                }
            }
        });
    }

    // Renamed from setupViewModelObservers to loadPasswordsForCurrentFilter for clarity
    private void loadPasswordsForCurrentFilter() {
        // The ViewModel's LiveData (getAllPasswords, getPasswordsByCategoryId, searchPasswords)
        // are now designed to respect the sortOrder set in the ViewModel.
        // They also handle their own transformations for sorting.

        // Remove previous observers to avoid multiple subscriptions if this method is called again
        passwordViewModel.getAllPasswords().removeObservers(getViewLifecycleOwner());
        // Potentially need to remove observers from getPasswordsByCategoryId if it was structured with a dynamic LiveData object per ID.
        // However, with the current ViewModel structure, getPasswordsByCategoryId and searchPasswords return new LiveData instances
        // or Transformations, so observing them anew is generally fine.

        if (!currentSearchQuery.isEmpty()) {
            passwordViewModel.searchPasswords(currentSearchQuery).observe(getViewLifecycleOwner(), this::updatePasswordListUI);
        } else if (selectedCategoryId == -1) {
            passwordViewModel.getAllPasswords().observe(getViewLifecycleOwner(), this::updatePasswordListUI);
        } else {
            passwordViewModel.getPasswordsByCategoryId(selectedCategoryId).observe(getViewLifecycleOwner(), this::updatePasswordListUI);
        }
    }

    // Renamed from filterAndSubmitPasswords to updatePasswordListUI
    private void updatePasswordListUI(List<Password> passwords) {
        // Passwords list from ViewModel is already filtered (by search/category) and sorted.
        if (passwords == null || passwords.isEmpty()) {
            passwordAdapter.submitList(new ArrayList<>());
            recyclerViewPasswords.setVisibility(View.GONE);
            imageViewNoPasswords.setVisibility(View.VISIBLE);
        } else {
            passwordAdapter.submitList(new ArrayList<>(passwords)); // Submit a new list for DiffUtil
            recyclerViewPasswords.setVisibility(View.VISIBLE);
            imageViewNoPasswords.setVisibility(View.GONE);
        }
    }

    private void setupCategoryFilters() {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            chipGroupCategoriesFilter.removeAllViews();

            Chip allChip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.item_category_chip, chipGroupCategoriesFilter, false);
            allChip.setText(R.string.chip_all_categories);
            allChip.setId(View.generateViewId()); // Ensure unique ID
            allChip.setCheckable(true);

            allChip.setOnClickListener(v -> {
                if (selectedCategoryId != -1 || !currentSearchQuery.isEmpty() || !allChip.isChecked()) {
                    selectedCategoryId = -1;
                    currentSearchQuery = ""; // Clear search when "All" is clicked
                    updateSearchViewQuery();
                    clearSearchViewFocus();
                    hideKeyboard();
                    chipGroupCategoriesFilter.check(allChip.getId());
                    loadPasswordsForCurrentFilter();
                }
            });
            chipGroupCategoriesFilter.addView(allChip);

            if (categories != null) {
                for (Category category : categories) {
                    Chip categoryChip = (Chip) LayoutInflater.from(requireContext()).inflate(R.layout.item_category_chip, chipGroupCategoriesFilter, false);
                    categoryChip.setText(category.getName());
                    categoryChip.setId(View.generateViewId()); // Ensure unique ID
                    categoryChip.setTag(category.id);
                    categoryChip.setCheckable(true);

                    categoryChip.setOnClickListener(v -> {
                        int categoryIdFromTag = (int) v.getTag();
                        if (selectedCategoryId != categoryIdFromTag || !currentSearchQuery.isEmpty() || !categoryChip.isChecked()) {
                            selectedCategoryId = categoryIdFromTag;
                            currentSearchQuery = ""; // Clear search when a category is clicked
                            updateSearchViewQuery();
                            clearSearchViewFocus();
                            hideKeyboard();
                            chipGroupCategoriesFilter.check(categoryChip.getId());
                            loadPasswordsForCurrentFilter();
                        }
                    });
                    chipGroupCategoriesFilter.addView(categoryChip);
                }
            }
            // After adding all chips, set the correct one as checked based on current state
            if (selectedCategoryId == -1) {
                chipGroupCategoriesFilter.check(allChip.getId());
            } else {
                for (int i = 0; i < chipGroupCategoriesFilter.getChildCount(); i++) {
                    View child = chipGroupCategoriesFilter.getChildAt(i);
                    if (child instanceof Chip && child.getTag() != null && (int)child.getTag() == selectedCategoryId) {
                        chipGroupCategoriesFilter.check(child.getId());
                        break;
                    }
                }
            }
        });
    }

    private void updateSearchViewQuery() {
        if (getActivity() != null) {
            androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar); // Assuming toolbar ID is 'toolbar'
            if (toolbar != null) {
                Menu menu = toolbar.getMenu();
                MenuItem searchItem = menu.findItem(R.id.action_search);
                if (searchItem != null) {
                    SearchView searchView = (SearchView) searchItem.getActionView();
                    if (searchView != null) {
                        searchView.setQuery("", false); // Don't submit query, let onQueryTextChange handle it
                    }
                }
            }
        }
    }

    private void clearSearchViewFocus() {
        if (getActivity() != null) {
            androidx.appcompat.widget.Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                Menu menu = toolbar.getMenu();
                MenuItem searchItem = menu.findItem(R.id.action_search);
                if (searchItem != null) {
                    SearchView searchView = (SearchView) searchItem.getActionView();
                    if (searchView != null) {
                        searchView.clearFocus();
                    }
                }
            }
        }
    }

    private void hideKeyboard() {
        View view = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onPasswordClick(Password password) {
        if (isMultiSelectMode) {
            toggleSelection(password);
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt("passwordId", password.id);
            navController.navigate(R.id.nav_show_password, bundle);
        }
    }

    @Override
    public void onPasswordLongClick(Password password) {
        if (!isMultiSelectMode) {
            isMultiSelectMode = true;
            fabAddPassword.setVisibility(View.GONE);
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setMultiSelect(true);
            }
            onBackPressedCallback.setEnabled(true);
            passwordAdapter.setMultiSelectMode(true);
            requireActivity().invalidateOptionsMenu();
            toggleSelection(password);
        }
    }

    @Override
    public void onPasswordSelect(Password password) {
        toggleSelection(password);
    }

    private void toggleSelection(Password password) {
        if (selectedPasswords.contains(password)) {
            selectedPasswords.remove(password);
        } else {
            selectedPasswords.add(password);
        }
        passwordAdapter.setSelectedItems(selectedPasswords);

        if (selectedPasswords.isEmpty()) {
            exitMultiSelectMode();
        } else {
            requireActivity().invalidateOptionsMenu();
        }
    }

    private void selectAll() {
        selectedPasswords.clear();
        selectedPasswords.addAll(passwordAdapter.getCurrentList());
        passwordAdapter.setSelectedItems(selectedPasswords);
        requireActivity().invalidateOptionsMenu();
    }

    private void deselectAll() {
        selectedPasswords.clear();
        passwordAdapter.setSelectedItems(selectedPasswords);
        requireActivity().invalidateOptionsMenu();
    }

    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        fabAddPassword.setVisibility(View.VISIBLE);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMultiSelect(false);
        }
        onBackPressedCallback.setEnabled(false);
        selectedPasswords.clear();
        passwordAdapter.setMultiSelectMode(false);
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void onCopyClick(Password password) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Password", password.getPassword());
        clipboard.setPrimaryClip(clip);
        Snackbar.make(requireView(), R.string.password_generated_and_copied, Snackbar.LENGTH_SHORT).show(); // Used a more generic copied string
    }

    @Override
    public void onToggleVisibilityClick(Password password, int position, boolean isVisible) {
        // Adapter handles UI, additional logic can go here if needed.
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuItem searchItem = menu.findItem(R.id.action_search);
                MenuItem sortItem = menu.findItem(R.id.action_sort);
                MenuItem deleteItem = menu.findItem(R.id.action_delete);
                MenuItem selectAllItem = menu.findItem(R.id.action_select_all);

                if (isMultiSelectMode) {
                    searchItem.setVisible(false);
                    sortItem.setVisible(false);
                    deleteItem.setVisible(true);
                    selectAllItem.setVisible(true);

                    // Check if all items are selected
                    boolean allSelected = selectedPasswords.size() == passwordAdapter.getItemCount();
                    selectAllItem.setTitle(allSelected ? R.string.deselect_all : R.string.select_all);
                    selectAllItem.setChecked(allSelected);

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setToolbarTitleVisibility(View.GONE);
                    }
                    if (getActivity() instanceof AppCompatActivity) {
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayShowTitleEnabled(true);
                            actionBar.setTitle(selectedPasswords.size() + " selected");
                        }
                    }
                } else {
                    searchItem.setVisible(true);
                    sortItem.setVisible(true);
                    deleteItem.setVisible(false);
                    selectAllItem.setVisible(false);

                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setToolbarTitleVisibility(View.VISIBLE);
                    }
                    if (getActivity() instanceof AppCompatActivity) {
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayShowTitleEnabled(false);
                        }
                    }
                }
            }

            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();

                if (searchView != null) {
                    if (!currentSearchQuery.isEmpty()) {
                        searchItem.expandActionView();
                        searchView.setQuery(currentSearchQuery, true); // Submit to restore state
                        searchView.clearFocus();
                    }

                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            hideKeyboard();
                            return true; // Indicate query was handled
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            String newQuery = newText.trim();
                            if (!currentSearchQuery.equals(newQuery)) {
                                currentSearchQuery = newQuery;
                                loadPasswordsForCurrentFilter();
                            }
                            return true;
                        }
                    });
                }

                searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                        // Can be used to adjust UI elements if needed when search expands
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                        if (!currentSearchQuery.isEmpty()) {
                            currentSearchQuery = "";
                            loadPasswordsForCurrentFilter();
                        }
                        hideKeyboard();
                        return true;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.action_sort) {
                    showSortOptionsDialog();
                    return true;
                } else if (id == R.id.action_delete) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.confirm_delete_password_title)
                            .setMessage(getString(R.string.confirm_delete_password_message_multiple, selectedPasswords.size()))
                            .setPositiveButton(R.string.button_delete, (dialog, which) -> {
                                passwordViewModel.movePasswordsToRecycleBin(selectedPasswords);
                                exitMultiSelectMode();
                                Snackbar.make(requireView(), R.string.password_deleted_successfully, Snackbar.LENGTH_SHORT).show();
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    return true;
                } else if (id == R.id.action_select_all) {
                    if (menuItem.isChecked()) {
                        deselectAll();
                    } else {
                        selectAll();
                    }
                    menuItem.setChecked(!menuItem.isChecked());
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showSortOptionsDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View sheetView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sort_options, bottomSheetDialog.findViewById(com.google.android.material.R.id.container), false);
        bottomSheetDialog.setContentView(sheetView);

        TextView sortAtoZ = sheetView.findViewById(R.id.sort_a_to_z);
        TextView sortZtoA = sheetView.findViewById(R.id.sort_z_to_a);
        TextView sortNewestToOldest = sheetView.findViewById(R.id.sort_newest_to_oldest);
        TextView sortOldestToNewest = sheetView.findViewById(R.id.sort_oldest_to_newest);

        View.OnClickListener listener = v -> {
            PasswordViewModel.SortOrder newSortOrder = currentSortOrder;
            int viewId = v.getId();
            if (viewId == R.id.sort_a_to_z) {
                newSortOrder = PasswordViewModel.SortOrder.A_TO_Z;
            } else if (viewId == R.id.sort_z_to_a) {
                newSortOrder = PasswordViewModel.SortOrder.Z_TO_A;
            } else if (viewId == R.id.sort_newest_to_oldest) {
                newSortOrder = PasswordViewModel.SortOrder.NEWEST_TO_OLDEST;
            } else if (viewId == R.id.sort_oldest_to_newest) {
                newSortOrder = PasswordViewModel.SortOrder.OLDEST_TO_NEWEST;
            }

            if (currentSortOrder != newSortOrder) {
                currentSortOrder = newSortOrder;
                passwordViewModel.setSortOrder(currentSortOrder);
                // loadPasswordsForCurrentFilter(); // ViewModel LiveData will auto-update due to sortOrder change if mAllPasswords observes it.
                                                // The existing observers in loadPasswordsForCurrentFilter will get the new sorted list.
            }
            bottomSheetDialog.dismiss();
        };

        sortAtoZ.setOnClickListener(listener);
        sortZtoA.setOnClickListener(listener);
        sortNewestToOldest.setOnClickListener(listener);
        sortOldestToNewest.setOnClickListener(listener);

        bottomSheetDialog.show();
    }
}
