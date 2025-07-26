package com.example.mostsave.ui.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.example.mostsave.R;
import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.ui.adapters.RecycleBinAdapter;
import com.example.mostsave.viewmodel.CategoryViewModel;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RecycleBinFragment extends Fragment implements RecycleBinAdapter.OnRestoreListener, RecycleBinAdapter.OnDeletePermanentlyListener {

    private PasswordViewModel passwordViewModel;
    private CategoryViewModel categoryViewModel;
    private RecyclerView recyclerViewDeletedPasswords;
    private RecycleBinAdapter recycleBinAdapter;
    private ImageView imageViewEmptyRecycleBin;
    private MenuItem clearAllMenuItem;

    public RecycleBinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recycle_bin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewDeletedPasswords = view.findViewById(R.id.recycler_view_deleted_passwords);
        imageViewEmptyRecycleBin = view.findViewById(R.id.image_view_empty_recycle_bin);

        setupRecyclerView();
        observeDeletedPasswords();
        setupMenu();
    }

    private void setupRecyclerView() {
        recyclerViewDeletedPasswords.setLayoutManager(new LinearLayoutManager(getContext()));
        recycleBinAdapter = new RecycleBinAdapter(this, this);
        recyclerViewDeletedPasswords.setAdapter(recycleBinAdapter);
    }

    private void observeDeletedPasswords() {
        passwordViewModel.getDeletedPasswords().observe(getViewLifecycleOwner(), passwords -> {
            if (passwords == null || passwords.isEmpty()) {
                imageViewEmptyRecycleBin.setVisibility(View.VISIBLE);
                recyclerViewDeletedPasswords.setVisibility(View.GONE);
                recycleBinAdapter.submitList(new ArrayList<>());
                if (clearAllMenuItem != null) {
                    clearAllMenuItem.setVisible(false);
                }
            } else {
                imageViewEmptyRecycleBin.setVisibility(View.GONE);
                recyclerViewDeletedPasswords.setVisibility(View.VISIBLE);
                List<RecycleBinAdapter.RecycledPasswordDisplayItem> displayItems = new ArrayList<>();
                AtomicInteger itemsToProcess = new AtomicInteger(passwords.size());

                for (Password p : passwords) {
                    if (p.getCategoryId() != null) {
                        LiveData<Category> categoryLiveData = categoryViewModel.getCategoryById(p.getCategoryId());
                        categoryLiveData.observe(getViewLifecycleOwner(), category -> {
                            categoryLiveData.removeObservers(getViewLifecycleOwner());
                            String categoryName = (category != null) ? category.getName() : getString(R.string.no_category);
                            synchronized (displayItems) {
                                displayItems.add(new RecycleBinAdapter.RecycledPasswordDisplayItem(p, categoryName));
                            }
                            if (itemsToProcess.decrementAndGet() == 0) {
                                recycleBinAdapter.submitList(new ArrayList<>(displayItems));
                            }
                        });
                    } else {
                        synchronized (displayItems) {
                           displayItems.add(new RecycleBinAdapter.RecycledPasswordDisplayItem(p, getString(R.string.no_category)));
                        }
                        if (itemsToProcess.decrementAndGet() == 0) {
                            recycleBinAdapter.submitList(new ArrayList<>(displayItems));
                        }
                    }
                }
                if (passwords.stream().allMatch(p -> p.getCategoryId() == null)) {
                    if (itemsToProcess.get() == 0 || passwords.isEmpty()) {
                         recycleBinAdapter.submitList(new ArrayList<>(displayItems));
                    }
                }
                if (clearAllMenuItem != null) {
                    clearAllMenuItem.setVisible(passwords.size() > 1);
                }
            }
        });
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.recycle_bin_menu, menu);
                clearAllMenuItem = menu.findItem(R.id.action_clear_all);
                if (recycleBinAdapter != null && clearAllMenuItem != null) {
                    clearAllMenuItem.setVisible(recycleBinAdapter.getItemCount() > 1);
                } else if (clearAllMenuItem != null) {
                    clearAllMenuItem.setVisible(false);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_clear_all) {
                    showClearAllConfirmationDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showClearAllConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_clear_all_title)
                .setMessage(R.string.confirm_clear_all_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Animate all items before clearing
                    animateAllItemsAndClear();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_delete_forever)
                .show();
    }

    /**
     * Animate all items deletion and then clear database
     */
    private void animateAllItemsAndClear() {
        List<RecycleBinAdapter.RecycledPasswordDisplayItem> currentItems = recycleBinAdapter.getCurrentList();
        if (currentItems.isEmpty()) {
            passwordViewModel.deleteAllPermanently();
            Snackbar.make(requireView(), R.string.recycle_bin_cleared_successfully, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Animate each item with delay
        for (int i = 0; i < currentItems.size(); i++) {
            final int position = i;
            recyclerViewDeletedPasswords.postDelayed(() -> {
                if (position < recycleBinAdapter.getItemCount()) {
                    RecyclerView.ViewHolder viewHolder =
                        recyclerViewDeletedPasswords.findViewHolderForAdapterPosition(position);
                    if (viewHolder != null) {
                        recycleBinAdapter.animateItemClearAll(viewHolder.itemView, position,
                            currentItems.size(), () -> {
                            // After last animation, clear database
                            if (position == currentItems.size() - 1) {
                                passwordViewModel.deleteAllPermanently();
                                Snackbar.make(requireView(), R.string.recycle_bin_cleared_successfully,
                                    Snackbar.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }, i * 50); // 50ms delay between each item animation
        }
    }

    @Override
    public void onRestoreClicked(Password password) {
        passwordViewModel.restorePassword(password.id);
        Snackbar.make(requireView(), R.string.password_restored_successfully, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDeletePermanentlyClicked(Password password) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_permanently_title)
                .setMessage(R.string.confirm_delete_permanently_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // Find the view for this password and animate it first
                    animateItemBeforeDelete(password, () -> {
                        passwordViewModel.deletePermanently(password.id);
                        Snackbar.make(requireView(), R.string.password_deleted_permanently_successfully, Snackbar.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_delete_forever)
                .show();
    }

    /**
     * Find and animate the specific item before deleting
     */
    private void animateItemBeforeDelete(Password password, Runnable onAnimationEnd) {
        // Find the position of this password in the current list
        List<RecycleBinAdapter.RecycledPasswordDisplayItem> currentItems = recycleBinAdapter.getCurrentList();
        int position = -1;
        for (int i = 0; i < currentItems.size(); i++) {
            if (currentItems.get(i).password.id == password.id) {
                position = i;
                break;
            }
        }

        if (position != -1) {
            RecyclerView.ViewHolder viewHolder =
                recyclerViewDeletedPasswords.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                // Animate the specific item
                animateDeleteItem(viewHolder.itemView, onAnimationEnd);
            } else {
                // If view holder not found, just execute the callback
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        } else {
            // If position not found, just execute the callback
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        }
    }

    /**
     * Animate single item deletion with slide out effect
     */
    private void animateDeleteItem(View itemView, Runnable onAnimationEnd) {
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(itemView, "translationX", 0f, -itemView.getWidth());
        slideOut.setDuration(300);
        slideOut.setInterpolator(new android.view.animation.DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f);
        fadeOut.setDuration(300);

        slideOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        slideOut.start();
        fadeOut.start();
    }
}
