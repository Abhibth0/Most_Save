package com.example.mostsave.ui.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.mostsave.R;
import com.example.mostsave.data.database.AppDatabase;
import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.ui.dialogs.ChooseIconDialogFragment;
import com.example.mostsave.viewmodel.CategoryViewModel;
import com.example.mostsave.viewmodel.PasswordViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class AddPasswordFragment extends Fragment implements ChooseIconDialogFragment.OnIconSelectedListener {

    private PasswordViewModel passwordViewModel;
    private CategoryViewModel categoryViewModel;

    private TextInputEditText editTextPasswordTitle;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextPassword;
    private TextInputEditText editTextUrl; // Added for URL input
    private AutoCompleteTextView autoCompleteTextViewCategory;
    private TextInputEditText editTextNote;
    private ImageView imageViewSelectedIcon;
    // private Button buttonSavePassword; // Remove old save button
    private ProgressBar passwordStrengthBar;
    private TextView passwordStrengthText;

    private List<Category> categoryList = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private Category selectedCategory = null;
    private int currentPasswordId = -1; // To store the ID of the password being edited
    private Password existingPassword = null; // To store the loaded password in edit mode
    private int selectedIconResId = 0;
    private String selectedIconPath = null;

    public AddPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        passwordViewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        if (getArguments() != null) {
            currentPasswordId = getArguments().getInt("passwordId", -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editTextPasswordTitle = view.findViewById(R.id.edit_text_password_title);
        editTextUrl = view.findViewById(R.id.edit_text_url); // Initialize URL EditText
        editTextUsername = view.findViewById(R.id.edit_text_username);
        editTextPassword = view.findViewById(R.id.edit_text_password);
        autoCompleteTextViewCategory = view.findViewById(R.id.autocomplete_text_view_category);
        editTextNote = view.findViewById(R.id.edit_text_note);
        imageViewSelectedIcon = view.findViewById(R.id.image_view_selected_icon);
        ImageButton buttonChooseIcon = view.findViewById(R.id.button_choose_icon);
        ImageButton buttonAddCategory = view.findViewById(R.id.button_add_category);
        ImageButton buttonGeneratePassword = view.findViewById(R.id.button_generate_password); // Initialize the new button
        passwordStrengthBar = view.findViewById(R.id.password_strength_bar);
        passwordStrengthText = view.findViewById(R.id.password_strength_text);

        setupMenu();
        setupCategoryDropdown();
        setupPasswordStrengthIndicator(); // Add this call

        if (currentPasswordId != -1) {
            loadExistingPasswordData();
            // buttonSavePassword.setText(R.string.button_update); // Remove text update for old button
            // Change screen title
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.title_edit_password);
            }
        } else {
            // Ensure title is "Add New Password" for add mode
            ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.title_add_password);
            }
        }

        buttonAddCategory.setOnClickListener(v -> showAddCategoryDialog());
        buttonChooseIcon.setOnClickListener(v -> showChooseIconDialog());
        // buttonSavePassword.setOnClickListener(v -> savePassword()); // Remove click listener for old button
        buttonGeneratePassword.setOnClickListener(v -> showGeneratePasswordDialog()); // Set click listener
    }

    private void setupMenu() {
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.add_password_menu, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_save_password) {
                    savePassword();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void showChooseIconDialog() {
        ChooseIconDialogFragment dialogFragment = new ChooseIconDialogFragment();
        dialogFragment.setOnIconSelectedListener(this);
        dialogFragment.show(getParentFragmentManager(), "ChooseIconDialogFragment");
    }

    @Override
    public void onIconSelected(int iconResId, String iconName) {
        selectedIconResId = iconResId;
        selectedIconPath = null;
        if (iconResId != 0) {
            imageViewSelectedIcon.setImageResource(iconResId);
            imageViewSelectedIcon.setVisibility(View.VISIBLE);
            // Show toast with selected icon name
            Toast.makeText(getContext(), "Selected: " + iconName, Toast.LENGTH_SHORT).show();
        } else {
            imageViewSelectedIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public void onUploadCustomIcon() {
        // Handle custom icon upload
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && getActivity() != null && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            String imagePath = saveImageToInternalStorage(imageUri);
            if (imagePath != null) {
                selectedIconPath = imagePath;
                selectedIconResId = 0;
                imageViewSelectedIcon.setImageURI(Uri.fromFile(new File(imagePath)));
                imageViewSelectedIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private String saveImageToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return null;
            }

            // Create a file in the app's private directory
            String fileName = "icon_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupPasswordStrengthIndicator() {
        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePasswordStrengthIndicator(s.toString());
            }
        });
        // Initialize with empty password
        updatePasswordStrengthIndicator("");
    }

    private void updatePasswordStrengthIndicator(String password) {
        int strengthScore = calculatePasswordStrengthScore(password);
        int progressColor;
        String strengthText;

        if (password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthText.setText("");
            // Optionally set a default color or make it transparent
            passwordStrengthBar.getProgressDrawable().setColorFilter(null); // Clears any previous color filter
            passwordStrengthText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray)); // Default text color
            return;
        }

        if (strengthScore < 33) { // Weak
            passwordStrengthBar.setProgress(strengthScore); // Or a fixed value like 25
            progressColor = ContextCompat.getColor(requireContext(), R.color.password_strength_weak);
            strengthText = getString(R.string.password_strength_weak); // Assuming you add this string resource
            passwordStrengthText.setTextColor(progressColor);
        } else if (strengthScore < 66) { // Good
            passwordStrengthBar.setProgress(strengthScore); // Or a fixed value like 60
            progressColor = ContextCompat.getColor(requireContext(), R.color.password_strength_good);
            strengthText = getString(R.string.password_strength_good); // Assuming you add this string resource
            passwordStrengthText.setTextColor(progressColor);
        } else { // Strongest
            passwordStrengthBar.setProgress(Math.min(strengthScore, 100)); // Cap at 100
            progressColor = ContextCompat.getColor(requireContext(), R.color.password_strength_strongest);
            strengthText = getString(R.string.password_strength_strongest); // Assuming you add this string resource
            passwordStrengthText.setTextColor(progressColor);
        }

        passwordStrengthBar.getProgressDrawable().setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN);
        passwordStrengthText.setText(strengthText);
    }

    private int calculatePasswordStrengthScore(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length criteria
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 15; // Additional points for longer passwords

        // Character type criteria
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[^a-zA-Z0-9].*");

        if (hasLower) score += 15;
        if (hasUpper) score += 15;
        if (hasDigit) score += 15;
        if (hasSpecial) score += 15;

        // Bonus for multiple character types
        int typesCount = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        if (typesCount >= 3) score += 10;
        if (typesCount == 4) score += 5; // Extra for all types

        return Math.min(score, 100); // Cap score at 100
    }

    private void loadExistingPasswordData() {
        passwordViewModel.getPasswordById(currentPasswordId).observe(getViewLifecycleOwner(), password -> {
            if (password != null) {
                existingPassword = password;
                editTextPasswordTitle.setText(password.getTitle());
                editTextUrl.setText(password.getUrl()); // Load URL
                editTextUsername.setText(password.getUsername());
                editTextPassword.setText(password.getPassword());
                editTextNote.setText(password.getNote());

                if (password.getCategoryId() != null) {
                    categoryViewModel.getCategoryById(password.getCategoryId()).observe(getViewLifecycleOwner(), category -> {
                        if (category != null) {
                            selectedCategory = category;
                            autoCompleteTextViewCategory.setText(category.getName(), false);
                        }
                    });
                }

                if (password.getIconResId() != 0) {
                    selectedIconResId = password.getIconResId();
                    imageViewSelectedIcon.setImageResource(selectedIconResId);
                    imageViewSelectedIcon.setVisibility(View.VISIBLE);
                } else if (password.getIconPath() != null) {
                    selectedIconPath = password.getIconPath();
                    File file = new File(selectedIconPath);
                    if(file.exists()){
                        imageViewSelectedIcon.setImageURI(Uri.fromFile(file));
                        imageViewSelectedIcon.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void setupCategoryDropdown() {
        categoryAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        autoCompleteTextViewCategory.setAdapter(categoryAdapter);

        categoryViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                categoryList = categories;
                List<String> categoryNames = categories.stream().map(Category::getName).collect(Collectors.toList());
                categoryAdapter.clear();
                categoryAdapter.addAll(categoryNames);
                categoryAdapter.notifyDataSetChanged();

                // If editing, and existing password's category is loaded, set it in dropdown
                if (existingPassword != null && existingPassword.getCategoryId() != null && selectedCategory != null) {
                    autoCompleteTextViewCategory.setText(selectedCategory.getName(), false);
                }
            }
        });

        autoCompleteTextViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            for (Category cat : categoryList) {
                if (cat.getName().equals(selectedName)) {
                    selectedCategory = cat;
                    break;
                }
            }
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.dialog_title_add_category);

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, (ViewGroup) getView(), false);
        final EditText inputCategoryName = dialogView.findViewById(R.id.edit_text_new_category_name);
        // Ensure R.id.edit_text_new_category_name exists in dialog_add_category.xml

        builder.setView(dialogView);

        builder.setPositiveButton(R.string.button_add, (dialog, which) -> {
            String categoryName = inputCategoryName.getText().toString().trim();
            if (TextUtils.isEmpty(categoryName)) {
                Toast.makeText(getContext(), R.string.error_category_name_required, Toast.LENGTH_SHORT).show();
                return;
            }
            // Check for duplicates on a background thread
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Category existingCategoryCheck = categoryViewModel.getCategoryByName(categoryName);
                // Ensure UI updates are on the main thread
                requireActivity().runOnUiThread(() -> {
                    if (existingCategoryCheck != null) {
                        Toast.makeText(getContext(), R.string.error_category_name_exists, Toast.LENGTH_SHORT).show();
                    } else {
                        // Provide a default displayOrder for user-added categories
                        // Using 100 to place them after predefined categories (1-4)
                        Category newCategoryToInsert = new Category(categoryName, 100);
                        categoryViewModel.insert(newCategoryToInsert);
                        Snackbar.make(requireView(), R.string.category_added_successfully, Snackbar.LENGTH_SHORT).show();
                        autoCompleteTextViewCategory.setText(categoryName, false);
                        // This is tricky: the newCategoryToInsert won't have an ID immediately if insert is async.
                        // For robust selection, it's better to re-query or observe until the category appears in categoryList
                        // and then find it to set selectedCategory. For simplicity now, we might lose selection temporarily.
                        // A quick fix could be to find it by name from the updated categoryList after a short delay or re-observation cycle.
                        // For now, we set selectedCategory to null and let user re-select or rely on text being set.
                        selectedCategory = null;
                        // To make it more robust, you might need to observe the category list again or get the inserted category with ID.
                    }
                });
            });
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showGeneratePasswordDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_generate_password, (ViewGroup) getView(), false);
        bottomSheetDialog.setContentView(dialogView);

        com.google.android.material.slider.Slider sliderPasswordLength = dialogView.findViewById(R.id.seekbar_password_length);
        TextView textViewPasswordLength = dialogView.findViewById(R.id.textview_password_length);
        MaterialCheckBox checkBoxUppercase = dialogView.findViewById(R.id.checkbox_uppercase_letters);
        MaterialCheckBox checkBoxLowercase = dialogView.findViewById(R.id.checkbox_lowercase_letters);
        MaterialCheckBox checkBoxNumbers = dialogView.findViewById(R.id.checkbox_numbers);
        MaterialCheckBox checkBoxSpecialChars = dialogView.findViewById(R.id.checkbox_special_characters);
        Button buttonDialogGenerate = dialogView.findViewById(R.id.button_dialog_generate);

        // Initialize Slider and TextView
        sliderPasswordLength.addOnChangeListener((slider, value, fromUser) -> {
            textViewPasswordLength.setText(String.valueOf((int) value));
        });

        // Set initial value
        sliderPasswordLength.setValue(16);
        textViewPasswordLength.setText("16");

        buttonDialogGenerate.setOnClickListener(v -> {
            int length = (int) sliderPasswordLength.getValue();
            boolean includeUppercase = checkBoxUppercase.isChecked();
            boolean includeLowercase = checkBoxLowercase.isChecked();
            boolean includeNumbers = checkBoxNumbers.isChecked();
            boolean includeSpecialChars = checkBoxSpecialChars.isChecked();

            if (!includeUppercase && !includeLowercase && !includeNumbers && !includeSpecialChars) {
                Toast.makeText(getContext(), "Please select at least one character type.", Toast.LENGTH_SHORT).show();
                return;
            }

            String generatedPassword = generatePasswordString(length, includeUppercase, includeLowercase, includeNumbers, includeSpecialChars);
            editTextPassword.setText(generatedPassword);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private String generatePasswordString(int length, boolean includeUppercase, boolean includeLowercase, boolean includeNumbers, boolean includeSpecialChars) {
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();
        List<Character> charPool = new ArrayList<>();

        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "!@#$%^&*()_+-=[]{}|;:',.<>/?";

        if (includeUppercase) {
            for (char c : upperCaseChars.toCharArray()) charPool.add(c);
        }
        if (includeLowercase) {
            for (char c : lowerCaseChars.toCharArray()) charPool.add(c);
        }
        if (includeNumbers) {
            for (char c : numberChars.toCharArray()) charPool.add(c);
        }
        if (includeSpecialChars) {
            for (char c : specialChars.toCharArray()) charPool.add(c);
        }

        if (charPool.isEmpty()) {
            return ""; // Should be handled by the check in showGeneratePasswordDialog
        }

        // Ensure at least one of each selected type is included if length allows
        List<Character> guaranteedChars = new ArrayList<>();
        if (includeUppercase) guaranteedChars.add(upperCaseChars.charAt(random.nextInt(upperCaseChars.length())));
        if (includeLowercase) guaranteedChars.add(lowerCaseChars.charAt(random.nextInt(lowerCaseChars.length())));
        if (includeNumbers) guaranteedChars.add(numberChars.charAt(random.nextInt(numberChars.length())));
        if (includeSpecialChars) guaranteedChars.add(specialChars.charAt(random.nextInt(specialChars.length())));

        // Shuffle guaranteed characters and add them first
        Collections.shuffle(guaranteedChars);
        for (char c : guaranteedChars) {
            if (password.length() < length) {
                password.append(c);
            }
        }

        // Fill the rest of the password length from the pool
        for (int i = password.length(); i < length; i++) {
            password.append(charPool.get(random.nextInt(charPool.size())));
        }

        // Shuffle the final password to ensure randomness of guaranteed characters' positions
        List<Character> passwordChars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) {
            passwordChars.add(c);
        }
        Collections.shuffle(passwordChars);

        StringBuilder finalPassword = new StringBuilder(length);
        for (char c : passwordChars) {
            finalPassword.append(c);
        }

        // Trim if somehow it became longer (e.g. many guaranteed chars for small length)
        if (finalPassword.length() > length) {
            return finalPassword.substring(0, length);
        }

        return finalPassword.toString();
    }

    private void savePassword() {
        String title = Objects.requireNonNull(editTextPasswordTitle.getText()).toString().trim();
        String url = Objects.requireNonNull(editTextUrl.getText()).toString().trim(); // Get URL text
        String username = Objects.requireNonNull(editTextUsername.getText()).toString().trim();
        String passwordValue = Objects.requireNonNull(editTextPassword.getText()).toString().trim();
        String note = Objects.requireNonNull(editTextNote.getText()).toString().trim();
        Integer categoryId = (selectedCategory != null) ? selectedCategory.id : null;

        if (TextUtils.isEmpty(title)) {
            editTextPasswordTitle.setError(getString(R.string.error_title_required));
            editTextPasswordTitle.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(passwordValue)) {
            editTextPassword.setError(getString(R.string.error_password_required));
            editTextPassword.requestFocus();
            return;
        }

        if (currentPasswordId != -1 && existingPassword != null) { // Edit mode
            Password passwordToUpdate = new Password(title, username, passwordValue, note, categoryId, url);
            passwordToUpdate.id = currentPasswordId; // Set the ID of the existing password
            passwordToUpdate.setDeleted(existingPassword.isDeleted()); // Preserve deleted status if any
            passwordToUpdate.setIconResId(selectedIconResId);
            passwordToUpdate.setIconPath(selectedIconPath);
            passwordViewModel.update(passwordToUpdate);
            Snackbar.make(requireView(), R.string.password_updated_successfully, Snackbar.LENGTH_SHORT).show();
        } else { // Add mode
            Password newPassword = new Password(title, username, passwordValue, note, categoryId, url);
            newPassword.setIconResId(selectedIconResId);
            newPassword.setIconPath(selectedIconPath);
            passwordViewModel.insert(newPassword);
            Snackbar.make(requireView(), R.string.password_saved_successfully, Snackbar.LENGTH_SHORT).show();
        }

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigateUp(); // Go back
    }
}
