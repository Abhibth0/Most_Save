package com.example.mostsave.ui.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import com.example.mostsave.R;
import com.example.mostsave.data.database.AppDatabase; // For background executor
import com.example.mostsave.data.model.Category;
import com.example.mostsave.ui.adapters.CategoryAdapter;
import com.example.mostsave.viewmodel.CategoryViewModel;
import com.example.mostsave.viewmodel.PasswordViewModel; // To check if category is in use
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import java.util.List;

public class CategoriesFragment extends Fragment implements CategoryAdapter.OnCategoryRenameListener, CategoryAdapter.OnCategoryDeleteListener, CategoryAdapter.OnStartDragListener {

    private CategoryViewModel categoryViewModel;
    private PasswordViewModel passwordViewModel; // To check if category is in use before deletion
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private FloatingActionButton fabAddCategory;
    private ItemTouchHelper itemTouchHelper;

    private List<Category> currentCategories = null;
    private List<com.example.mostsave.data.model.Password> currentPasswords = null;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewCategories = view.findViewById(R.id.recycler_view_categories);
        fabAddCategory = view.findViewById(R.id.fab_add_category);

        setupRecyclerView();
        setupViewModelObservers();

        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog(null)); // Pass null for new category

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                categoryAdapter.onItemMove(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                updateCategoryOrder();
            }
        };

        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerViewCategories);
    }

    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryAdapter = new CategoryAdapter(this, this, this);
        recyclerViewCategories.setAdapter(categoryAdapter);
    }

    private void setupViewModelObservers() {
        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            currentCategories = categories;
            categoryAdapter.submitList(categories);
            updateCategoryPasswordCounts();
        });
        passwordViewModel.getAllPasswords().observe(getViewLifecycleOwner(), passwords -> {
            currentPasswords = passwords;
            updateCategoryPasswordCounts();
        });
    }

    private void showAddCategoryDialog(@Nullable Category categoryToRename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        boolean isRenameMode = categoryToRename != null;
        builder.setTitle(isRenameMode ? R.string.dialog_title_rename_category : R.string.dialog_title_add_category);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, (ViewGroup) getView(), false);
        final EditText inputCategoryName = dialogView.findViewById(R.id.edit_text_new_category_name);

        if (isRenameMode) {
            inputCategoryName.setText(categoryToRename.getName());
            inputCategoryName.setSelection(categoryToRename.getName().length());
        }

        builder.setView(dialogView);

        builder.setPositiveButton(isRenameMode ? R.string.button_update : R.string.button_add, (dialog, which) -> {
            String categoryName = inputCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(categoryName)) {
                Toast.makeText(getContext(), R.string.error_category_name_required, Toast.LENGTH_SHORT).show();
                return;
            }

            AppDatabase.databaseWriteExecutor.execute(() -> {
                Category existingCategory = categoryViewModel.getCategoryByName(categoryName);
                requireActivity().runOnUiThread(() -> {
                    if (existingCategory != null && (!isRenameMode || existingCategory.id != categoryToRename.id)) {
                        Toast.makeText(getContext(), R.string.error_category_name_exists, Toast.LENGTH_SHORT).show();
                    } else {
                        if (isRenameMode) {
                            categoryToRename.setName(categoryName);
                            categoryViewModel.update(categoryToRename);
                            Snackbar.make(requireView(), R.string.category_renamed_successfully, Snackbar.LENGTH_SHORT).show();
                        } else {
                            // Provide a default displayOrder for user-added categories
                            // Using 100 to place them after predefined categories (1-4)
                            Category newCategory = new Category(categoryName, 100);
                            categoryViewModel.insert(newCategory);
                            Snackbar.make(requireView(), R.string.category_added_successfully, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void updateCategoryPasswordCounts() {
        if (currentCategories == null || currentPasswords == null) return;
        java.util.Map<Integer, Integer> countMap = new java.util.HashMap<>();
        for (Category category : currentCategories) {
            countMap.put(category.id, 0);
        }
        for (com.example.mostsave.data.model.Password password : currentPasswords) {
            if (password.categoryId != null && countMap.containsKey(password.categoryId)) {
                countMap.put(password.categoryId, countMap.get(password.categoryId) + 1);
            }
        }
        categoryAdapter.setPasswordCountMap(countMap);
    }

    @Override
    public void onRenameCategory(Category category) {
        showAddCategoryDialog(category); // Pass category to prefill and indicate rename mode
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }

    private void updateCategoryOrder() {
        List<Category> categories = categoryAdapter.getCurrentList();
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (category.getDisplayOrder() != i) {
                category.setDisplayOrder(i);
                categoryViewModel.update(category);
            }
        }
    }

    @Override
    public void onDeleteCategory(Category category) {
        // Check if any password uses this category before deleting
        passwordViewModel.getPasswordsByCategoryId(category.id).observe(getViewLifecycleOwner(), passwordsInCategory -> {
            if (passwordsInCategory != null && !passwordsInCategory.isEmpty()) {
                // Category is in use, show a message or prevent deletion based on desired UX
                // For now, we allow deletion as per ON DELETE SET NULL
                // Snackbar.make(requireView(), R.string.category_in_use_message, Snackbar.LENGTH_LONG).show();
                // return;
                showDeleteConfirmationDialog(category, true);
            } else {
                showDeleteConfirmationDialog(category, false);
            }
            // Important: Remove observer after first check to avoid re-triggering dialog on unrelated DB changes
            passwordViewModel.getPasswordsByCategoryId(category.id).removeObservers(getViewLifecycleOwner());
        });
    }

    private void showDeleteConfirmationDialog(Category category, boolean isInUse) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_category_title)
                .setMessage(getString(R.string.confirm_delete_category_message) + (isInUse ? "\n\nWarning: This category is currently assigned to one or more passwords." : ""))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    categoryViewModel.delete(category);
                    Snackbar.make(requireView(), R.string.category_deleted_successfully, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }
}
