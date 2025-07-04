package com.example.mostsave.ui.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostsave.R;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.utils.SecurityManager;
import com.mostsave.data.model.History;
import com.mostsave.data.repository.HistoryRepository;
import com.example.mostsave.viewmodel.CategoryViewModel;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowPasswordFragment extends Fragment {

    private PasswordViewModel passwordViewModel;
    private CategoryViewModel categoryViewModel;
    private HistoryRepository historyRepository;
    private NavController navController;
    private SecurityManager securityManager;

    private TextView textViewTitleValue, textViewUsernameValue, textViewPasswordValue, textViewCategoryValue, textViewNoteValue, textViewUrlValue, textViewUrlLabel, textViewShowLastUpdatedValue, textViewNoteLabel, textViewTitleLabel;
    private ImageView imageViewShowPasswordIcon;
    private MenuItem favoriteMenuItem;

    private int currentPasswordId = -1;
    private Password currentPassword;
    private boolean isPasswordVisible = false;

    public ShowPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentPasswordId = getArguments().getInt("passwordId", -1);
        }
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        historyRepository = new HistoryRepository(requireActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view);

        // Initialize SecurityManager
        securityManager = SecurityManager.getInstance(requireContext());

        textViewTitleValue = view.findViewById(R.id.text_view_show_title_value);
        textViewTitleLabel = view.findViewById(R.id.text_view_show_title_label);
        imageViewShowPasswordIcon = view.findViewById(R.id.image_view_show_password_icon);
        textViewUrlLabel = view.findViewById(R.id.text_view_show_url_label);
        textViewUrlValue = view.findViewById(R.id.text_view_show_url_value);
        textViewUsernameValue = view.findViewById(R.id.text_view_show_username_value);
        textViewPasswordValue = view.findViewById(R.id.text_view_show_password_value);
        textViewCategoryValue = view.findViewById(R.id.text_view_show_category_value);
        textViewNoteValue = view.findViewById(R.id.text_view_show_note_value);
        textViewNoteLabel = view.findViewById(R.id.text_view_show_note_label);
        ImageButton buttonShowHidePassword = view.findViewById(R.id.button_show_hide_detail_password);
        ImageButton buttonCopyPassword = view.findViewById(R.id.button_copy_detail_password);
        ImageButton buttonCopyUsername = view.findViewById(R.id.button_copy_detail_username);
        Button buttonEditPassword = view.findViewById(R.id.button_edit_password);
        Button buttonDeletePassword = view.findViewById(R.id.button_delete_password);
        textViewShowLastUpdatedValue = view.findViewById(R.id.text_view_show_last_updated_value);

        setupMenu();

        if (currentPasswordId != -1) {
            loadPasswordDetails();
        }

        buttonShowHidePassword.setOnClickListener(v -> togglePasswordVisibility());
        buttonCopyPassword.setOnClickListener(v -> copyPasswordToClipboard());
        buttonCopyUsername.setOnClickListener(v -> copyUsernameToClipboard());
        buttonEditPassword.setOnClickListener(v -> editPassword());
        buttonDeletePassword.setOnClickListener(v -> confirmDeletePassword());

        textViewUrlValue.setOnClickListener(v -> openUrl());
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_show_password, menu);
                favoriteMenuItem = menu.findItem(R.id.action_favorite);

                if (currentPassword != null) {
                    updateFavoriteMenuIcon(currentPassword.isFavorite());
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_favorite) {
                    toggleFavorite();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void loadPasswordDetails() {
        passwordViewModel.getPasswordById(currentPasswordId).observe(getViewLifecycleOwner(), password -> {
            if (password != null) {
                currentPassword = password;

                updateFavoriteMenuIcon(password.isFavorite());

                History history = new History();
                history.passwordId = currentPassword.getId();
                history.passwordTitle = currentPassword.getTitle();
                history.viewedTimestamp = System.currentTimeMillis();
                historyRepository.insert(history);

                boolean hasIcon = false;
                if (password.getIconPath() != null && !password.getIconPath().isEmpty()) {
                    File file = new File(password.getIconPath());
                    if(file.exists()){
                        imageViewShowPasswordIcon.setImageURI(Uri.fromFile(file));
                        hasIcon = true;
                    }
                } else if (password.getIconResId() != 0) {
                    imageViewShowPasswordIcon.setImageResource(password.getIconResId());
                    hasIcon = true;
                }

                if (hasIcon) {
                    imageViewShowPasswordIcon.setVisibility(View.VISIBLE);
                    textViewTitleLabel.setVisibility(View.GONE);
                    textViewTitleValue.setText(password.getTitle());

                    // Make title text larger and bold when icon is visible
                    textViewTitleValue.setTextSize(18); // Larger text size
                    textViewTitleValue.setTypeface(textViewTitleValue.getTypeface(), android.graphics.Typeface.BOLD);

                    // Dynamically change constraints - position title next to the icon
                    androidx.constraintlayout.widget.ConstraintSet constraintSet = new androidx.constraintlayout.widget.ConstraintSet();
                    androidx.constraintlayout.widget.ConstraintLayout layout =
                        (androidx.constraintlayout.widget.ConstraintLayout) textViewTitleValue.getParent();
                    constraintSet.clone(layout);

                    // Clear existing constraints
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.START);
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.TOP);
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.BASELINE);

                    // Set new constraints
                    constraintSet.connect(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.START,
                        imageViewShowPasswordIcon.getId(), androidx.constraintlayout.widget.ConstraintSet.END,
                        (int) getResources().getDimension(R.dimen.margin_medium));
                    constraintSet.connect(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.TOP,
                        imageViewShowPasswordIcon.getId(), androidx.constraintlayout.widget.ConstraintSet.TOP, 0);
                    constraintSet.connect(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.BOTTOM,
                        imageViewShowPasswordIcon.getId(), androidx.constraintlayout.widget.ConstraintSet.BOTTOM, 0);

                    constraintSet.applyTo(layout);
                } else {
                    imageViewShowPasswordIcon.setVisibility(View.GONE);
                    textViewTitleLabel.setVisibility(View.VISIBLE);
                    textViewTitleValue.setText(password.getTitle());

                    // Reset title text to normal style when no icon
                    textViewTitleValue.setTextSize(16); // Default text size
                    textViewTitleValue.setTypeface(null, android.graphics.Typeface.NORMAL);

                    // Reset constraints to original position - next to the label
                    androidx.constraintlayout.widget.ConstraintSet constraintSet = new androidx.constraintlayout.widget.ConstraintSet();
                    androidx.constraintlayout.widget.ConstraintLayout layout =
                        (androidx.constraintlayout.widget.ConstraintLayout) textViewTitleValue.getParent();
                    constraintSet.clone(layout);

                    // Clear existing constraints
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.START);
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.TOP);
                    constraintSet.clear(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.BOTTOM);

                    // Set original constraints
                    constraintSet.connect(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.START,
                        textViewTitleLabel.getId(), androidx.constraintlayout.widget.ConstraintSet.END,
                        (int) getResources().getDimension(R.dimen.margin_small));
                    constraintSet.connect(textViewTitleValue.getId(), androidx.constraintlayout.widget.ConstraintSet.BASELINE,
                        textViewTitleLabel.getId(), androidx.constraintlayout.widget.ConstraintSet.BASELINE, 0);

                    constraintSet.applyTo(layout);
                }

                if (password.getUrl() != null && !password.getUrl().isEmpty()) {
                    textViewUrlLabel.setVisibility(View.VISIBLE); // Show URL label
                    textViewUrlValue.setText(R.string.text_open_url);
                    textViewUrlValue.setVisibility(View.VISIBLE); // Show URL value
                } else {
                    textViewUrlLabel.setVisibility(View.GONE); // Hide URL label
                    textViewUrlValue.setVisibility(View.GONE); // Hide URL value
                }

                textViewUsernameValue.setText(password.getUsername() != null ? password.getUsername() : "");

                if (password.getNote() != null && !password.getNote().isEmpty()) {
                    textViewNoteValue.setText(password.getNote());
                    textViewNoteLabel.setVisibility(View.VISIBLE);
                    textViewNoteValue.setVisibility(View.VISIBLE);
                } else {
                    textViewNoteLabel.setVisibility(View.GONE);
                    textViewNoteValue.setVisibility(View.GONE);
                }

                updatePasswordTextView();

                if (password.getLastUpdated() > 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault());
                    Date lastUpdatedDate = new Date(password.getLastUpdated());
                    textViewShowLastUpdatedValue.setText(sdf.format(lastUpdatedDate));
                } else {
                    textViewShowLastUpdatedValue.setText("N/A"); // Or some default text
                }

                if (password.getCategoryId() != null) {
                    categoryViewModel.getCategoryById(password.getCategoryId()).observe(getViewLifecycleOwner(), category -> {
                        if (category != null) {
                            textViewCategoryValue.setText(category.getName());
                        } else {
                            textViewCategoryValue.setText(R.string.no_category);
                        }
                    });
                } else {
                    textViewCategoryValue.setText(R.string.no_category);
                }
            }
        });
    }

    private void updateFavoriteMenuIcon(boolean isFavorite) {
        if (favoriteMenuItem != null) {
            favoriteMenuItem.setIcon(isFavorite ?
                R.drawable.ic_favorite :
                R.drawable.ic_favorite_border);
        }
    }

    private void toggleFavorite() {
        if (currentPassword != null) {
            boolean newFavoriteState = !currentPassword.isFavorite();
            passwordViewModel.updateFavoriteStatus(currentPassword.getId(), newFavoriteState);
            updateFavoriteMenuIcon(newFavoriteState);

            // Show feedback to user
            String message = newFavoriteState ?
                getString(R.string.added_to_favorites) :
                getString(R.string.removed_from_favorites);
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void togglePasswordVisibility() {
        if (currentPassword == null) return;
        isPasswordVisible = !isPasswordVisible;
        updatePasswordTextView();
    }

    private void updatePasswordTextView() {
        if (currentPassword == null) return;
        ImageButton buttonShowHidePassword = requireView().findViewById(R.id.button_show_hide_detail_password);
        if (isPasswordVisible) {
            textViewPasswordValue.setTransformationMethod(null);
            buttonShowHidePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            textViewPasswordValue.setTransformationMethod(PasswordTransformationMethod.getInstance());
            buttonShowHidePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        textViewPasswordValue.setText(currentPassword.getPassword());
    }

    private void copyPasswordToClipboard() {
        if (currentPassword != null) {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Password", currentPassword.getPassword());
            clipboard.setPrimaryClip(clip);

            // Notify SecurityManager that password was copied
            securityManager.onPasswordCopied();

            Snackbar.make(requireView(), R.string.desc_copy_password, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(requireView(), "No password to copy", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void copyUsernameToClipboard() {
        if (currentPassword != null && currentPassword.getUsername() != null) {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Username", currentPassword.getUsername());
            clipboard.setPrimaryClip(clip);

            // Note: Username copy doesn't trigger clipboard clear timer (only passwords do)

            Snackbar.make(requireView(), R.string.desc_copy_username, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(requireView(), "No username to copy", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void editPassword() {
        if (currentPasswordId != -1) {
            // Navigate to AddPasswordFragment in "edit mode"
            // This requires AddPasswordFragment to handle an optional passwordId argument
            // and load existing data if present.
            Bundle bundle = new Bundle();
            bundle.putInt("passwordId", currentPasswordId);
            // Assuming nav_graph has an action from show_password to add_password
            // or AddPasswordFragment is reused for editing.
            // For now, let's navigate to nav_add_password, which needs to be adapted for editing.
            navController.navigate(R.id.nav_add_password, bundle);
        }
    }

    private void confirmDeletePassword() {
        if (currentPassword == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_delete_password_title)
                .setMessage(R.string.confirm_delete_password_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    passwordViewModel.softDelete(currentPassword.id); // Use currentPassword.id directly
                    Snackbar.make(requireView(), R.string.password_deleted_successfully, Snackbar.LENGTH_SHORT).show();
                    navController.navigateUp();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    // Method to open URL
    private void openUrl() {
        if (currentPassword != null && currentPassword.getUrl() != null && !currentPassword.getUrl().isEmpty()) {
            String url = currentPassword.getUrl();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Could not open URL", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "No URL to open", Toast.LENGTH_SHORT).show();
        }
    }
}
