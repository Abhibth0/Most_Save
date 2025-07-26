package com.example.mostsave.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;
import com.example.mostsave.ui.adapters.PasswordAdapter;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

public class FavoritesFragment extends Fragment {

    private PasswordViewModel passwordViewModel;
    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private TextView textViewNoFavorites;
    private View emptyStateLayout;
    private NavController navController;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_favorites);
        textViewNoFavorites = view.findViewById(R.id.text_view_no_favorites);
        emptyStateLayout = view.findViewById(R.id.layout_empty_state);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter with all required listeners
        adapter = new PasswordAdapter(
            // OnPasswordClickListener
            password -> {
                // Handle item click - navigate to password details
                Bundle bundle = new Bundle();
                bundle.putInt("passwordId", password.getId());
                navController.navigate(R.id.nav_show_password, bundle);
            },
            // OnCopyClickListener
            password -> {
                // Copy password to clipboard
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Password", password.getPassword());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(requireView(), R.string.password_generated_and_copied, Snackbar.LENGTH_SHORT).show();
            },
            // OnToggleVisibilityListener
            (password, position, isVisible) -> {
                // No additional action needed, adapter handles visibility state
            },
            // OnPasswordLongClickListener
            password -> {
                // Not implementing multi-select in Favorites for now
            },
            // OnPasswordSelectListener
            password -> {
                // Not implementing multi-select in Favorites for now
            }
        );

        recyclerView.setAdapter(adapter);

        // Set up ViewModel
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);

        // Observe favorite passwords
        passwordViewModel.getFilteredFavoritePasswords().observe(getViewLifecycleOwner(), passwords -> {
            adapter.submitList(passwords);

            // Show/hide empty state message
            if (passwords.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        // Setup menu provider (replacing deprecated setHasOptionsMenu)
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu); // Using home_menu which contains the main menu options

                MenuItem searchItem = menu.findItem(R.id.action_search);
                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

                if (searchView != null) {
                    searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return false;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            passwordViewModel.setFavoriteSearchQuery(newText);
                            return true;
                        }
                    });
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if (id == R.id.action_sort) {
                    // Show sort options as a popup or dialog
                    showSortOptions();
                    return true;
                } else if (id == R.id.action_search) {
                    // Handle search action if needed
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showSortOptions() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View bottomSheetView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_sort_options, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetView.findViewById(R.id.sort_a_to_z).setOnClickListener(v -> {
            passwordViewModel.setSortOrder(PasswordViewModel.SortOrder.A_TO_Z);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.sort_z_to_a).setOnClickListener(v -> {
            passwordViewModel.setSortOrder(PasswordViewModel.SortOrder.Z_TO_A);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.sort_newest_to_oldest).setOnClickListener(v -> {
            passwordViewModel.setSortOrder(PasswordViewModel.SortOrder.NEWEST_TO_OLDEST);
            bottomSheetDialog.dismiss();
        });

        bottomSheetView.findViewById(R.id.sort_oldest_to_newest).setOnClickListener(v -> {
            passwordViewModel.setSortOrder(PasswordViewModel.SortOrder.OLDEST_TO_NEWEST);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
