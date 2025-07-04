package com.example.mostsave.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mostsave.R;
import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.data.repository.CategoryRepository;
import com.example.mostsave.data.repository.PasswordRepository;
import com.example.mostsave.data.security.EncryptionHelper;
import com.example.mostsave.databinding.FragmentAdvanceSettingsBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class AdvanceSettingsFragment extends Fragment {

    private static final String TAG = "AdvanceSettingsFragment";
    private FragmentAdvanceSettingsBinding binding;
    private PasswordRepository passwordRepository;
    private CategoryRepository categoryRepository; // Add CategoryRepository
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;
    private List<Password> allPasswords = new ArrayList<>();
    private String currentExportKey;
    private String currentImportKey;
    private AlertDialog activeDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdvanceSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        passwordRepository = new PasswordRepository(requireActivity().getApplication());
        categoryRepository = new CategoryRepository(requireActivity().getApplication()); // Initialize CategoryRepository
        passwordRepository.getAllPasswords().observe(getViewLifecycleOwner(), passwords -> allPasswords = passwords);

        exportLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData() != null ? result.getData().getData() : null;
                        if (uri != null) {
                            if (activeDialog != null) {
                                activeDialog.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
                                activeDialog.findViewById(R.id.buttonExport).setEnabled(false);
                                activeDialog.findViewById(R.id.buttonCopy).setEnabled(false);
                                activeDialog.findViewById(R.id.buttonCancel).setEnabled(false);
                            }
                            exportPasswords(uri, currentExportKey);
                        } else {
                            Toast.makeText(requireContext(), R.string.no_file_selected, Toast.LENGTH_SHORT).show();
                            if (activeDialog != null) activeDialog.dismiss();
                        }
                    } else {
                        if (activeDialog != null) activeDialog.dismiss();
                    }
                });

        importLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri uri = result.getData() != null ? result.getData().getData() : null;
                        if (uri != null && currentImportKey != null) {
                            if (activeDialog != null) {
                                activeDialog.findViewById(R.id.progressLayout).setVisibility(View.VISIBLE);
                                activeDialog.findViewById(R.id.buttonImport).setEnabled(false);
                                activeDialog.findViewById(R.id.buttonCancel).setEnabled(false);
                            }
                            importPasswords(uri, currentImportKey);
                        } else {
                            Toast.makeText(requireContext(), R.string.no_file_selected, Toast.LENGTH_SHORT).show();
                            if (activeDialog != null) activeDialog.dismiss();
                        }
                    } else {
                        if (activeDialog != null) activeDialog.dismiss();
                    }
                });

        binding.textViewExport.setOnClickListener(v -> showExportDialog());
        binding.textViewImport.setOnClickListener(v -> showImportDialog());
    }

    private void showExportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_export_key, null);
        builder.setView(dialogView);

        final TextView textViewKey = dialogView.findViewById(R.id.textViewKey);
        final Button buttonCopy = dialogView.findViewById(R.id.buttonCopy);
        final Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        final Button buttonExport = dialogView.findViewById(R.id.buttonExport);

        currentExportKey = UUID.randomUUID().toString().replaceAll("-", "");
        textViewKey.setText(currentExportKey);

        final AlertDialog dialog = builder.create();
        activeDialog = dialog;
        dialog.setOnDismissListener(d -> activeDialog = null);

        buttonCopy.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Backup Key", currentExportKey);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(requireContext(), "Key copied to clipboard", Toast.LENGTH_SHORT).show();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonExport.setOnClickListener(v -> openDocumentTreeForExport());

        dialog.show();
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_import_key, null);
        builder.setView(dialogView);

        final EditText editTextKey = dialogView.findViewById(R.id.editTextKey);
        final Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        final Button buttonImport = dialogView.findViewById(R.id.buttonImport);

        final AlertDialog dialog = builder.create();
        activeDialog = dialog;
        dialog.setOnDismissListener(d -> activeDialog = null);

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonImport.setOnClickListener(v -> {
            String key = editTextKey.getText().toString().trim();
            if (key.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a backup key", Toast.LENGTH_SHORT).show();
                return;
            }
            currentImportKey = key;
            openDocumentForImport();
        });

        dialog.show();
    }

    private void openDocumentTreeForExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "most_save_passwords.mostsave");
        exportLauncher.launch(intent);
    }

    private void openDocumentForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        importLauncher.launch(intent);
    }

    private void updateProgress(int progress) {
        if (activeDialog != null) {
            ProgressBar progressBar = activeDialog.findViewById(R.id.progressBar);
            TextView progressText = activeDialog.findViewById(R.id.progressText);
            if (progressBar != null && progressText != null) {
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
            }
        }
    }

    private void exportPasswords(Uri uri, String key) {
        new Thread(() -> {
            try {
                if (allPasswords.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "No passwords to export", Toast.LENGTH_SHORT).show();
                        if (activeDialog != null) activeDialog.dismiss();
                    });
                    return;
                }

                requireActivity().runOnUiThread(() -> updateProgress(25));

                // First, populate the categoryName field for each password
                List<Password> passwordsToExport = new ArrayList<>();
                for (Password password : allPasswords) {
                    Password passwordCopy = new Password(
                            password.getTitle(),
                            password.getUsername(),
                            password.getPassword(),
                            password.getNote(),
                            password.getCategoryId(),
                            password.getUrl()
                    );
                    passwordCopy.setId(password.getId());
                    passwordCopy.setIconResId(password.getIconResId());
                    passwordCopy.setDeleted(password.isDeleted());
                    passwordCopy.setFavorite(password.isFavorite());

                    // Add the category name if the password has a category
                    if (password.getCategoryId() != null) {
                        Category category = categoryRepository.getCategoryByIdSync(password.getCategoryId());
                        if (category != null) {
                            passwordCopy.setCategoryName(category.getName());
                        }
                    }

                    passwordsToExport.add(passwordCopy);
                }

                requireActivity().runOnUiThread(() -> updateProgress(50));

                Gson gson = new Gson();
                String json = gson.toJson(passwordsToExport);

                requireActivity().runOnUiThread(() -> updateProgress(75));

                String encryptedData = EncryptionHelper.encrypt(json, key);

                requireActivity().runOnUiThread(() -> updateProgress(85));

                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(
                                requireContext().getContentResolver().openOutputStream(uri)))) {
                    writer.write(encryptedData);
                    requireActivity().runOnUiThread(() -> {
                        updateProgress(100);
                        Toast.makeText(requireContext(), R.string.export_success, Toast.LENGTH_SHORT).show();
                        if (activeDialog != null) activeDialog.dismiss();
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error exporting passwords", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), R.string.export_failed, Toast.LENGTH_SHORT).show();
                    if (activeDialog != null) activeDialog.dismiss();
                });
            }
        }).start();
    }

    private void importPasswords(Uri uri, String key) {
        new Thread(() -> {
            try {
                StringBuilder stringBuilder = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                requireContext().getContentResolver().openInputStream(uri)))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                }

                String encryptedData = stringBuilder.toString();
                String json = EncryptionHelper.decrypt(encryptedData, key);

                requireActivity().runOnUiThread(() -> updateProgress(25));

                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<Password>>() {
                }.getType();
                List<Password> importedPasswords = gson.fromJson(json, listType);

                requireActivity().runOnUiThread(() -> updateProgress(40));

                if (importedPasswords == null || importedPasswords.isEmpty()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "No passwords found in the file or key is invalid.", Toast.LENGTH_SHORT).show();
                        if (activeDialog != null) activeDialog.dismiss();
                    });
                    return;
                }

                // Get all existing categories
                List<Category> existingCategories = categoryRepository.getAllCategoriesSync();
                Map<Integer, Integer> categoryIdMapping = new HashMap<>();
                Map<String, Integer> categoryNameMapping = new HashMap<>();

                // Create a map of existing category names to their IDs for easy lookup
                for (Category category : existingCategories) {
                    categoryNameMapping.put(category.getName().toLowerCase(), category.getId());
                }

                // Create a map of category IDs that need to be created
                Map<Integer, String> categoriesToCreate = new HashMap<>();

                // Check for missing categories in imported passwords
                for (Password importedPassword : importedPasswords) {
                    if (importedPassword.getCategoryId() != null) {
                        boolean categoryExists = false;
                        Integer originalCategoryId = importedPassword.getCategoryId();

                        // If we've already created or mapped this category, skip checking
                        if (categoryIdMapping.containsKey(originalCategoryId)) {
                            continue;
                        }

                        // First, check if we can find the category by ID
                        for (Category existingCategory : existingCategories) {
                            if (existingCategory.getId() == originalCategoryId) {
                                categoryExists = true;
                                categoryIdMapping.put(originalCategoryId, existingCategory.getId());
                                break;
                            }
                        }

                        // If not found by ID but we have a category name, try to find by name
                        if (!categoryExists && importedPassword.getCategoryName() != null) {
                            String normalizedCategoryName = importedPassword.getCategoryName().toLowerCase();
                            if (categoryNameMapping.containsKey(normalizedCategoryName)) {
                                // Found a category with the same name
                                categoryExists = true;
                                categoryIdMapping.put(originalCategoryId, categoryNameMapping.get(normalizedCategoryName));
                            } else {
                                // Category name doesn't exist, schedule it for creation
                                categoriesToCreate.put(originalCategoryId, importedPassword.getCategoryName());
                            }
                        }

                        // If still not found and no name available, use a placeholder name
                        if (!categoryExists && importedPassword.getCategoryName() == null) {
                            String categoryName = "Imported Category " + originalCategoryId;
                            categoriesToCreate.put(originalCategoryId, categoryName);
                        }
                    }
                }

                // Create missing categories
                if (!categoriesToCreate.isEmpty()) {
                    Log.d(TAG, "Creating " + categoriesToCreate.size() + " missing categories");
                    for (Map.Entry<Integer, String> entry : categoriesToCreate.entrySet()) {
                        Integer originalId = entry.getKey();
                        String categoryName = entry.getValue();

                        // Create a new category with the next available display order
                        int maxDisplayOrder = 0;
                        for (Category existingCategory : existingCategories) {
                            if (existingCategory.getDisplayOrder() > maxDisplayOrder) {
                                maxDisplayOrder = existingCategory.getDisplayOrder();
                            }
                        }

                        Category newCategory = new Category(categoryName, maxDisplayOrder + 1);
                        long newCategoryId = categoryRepository.insertSync(newCategory);

                        // Map the original category ID to the new category ID
                        categoryIdMapping.put(originalId, (int) newCategoryId);

                        // Add the new category to existing categories for future reference
                        newCategory.setId((int) newCategoryId);
                        existingCategories.add(newCategory);

                        // Also add to the name mapping
                        categoryNameMapping.put(categoryName.toLowerCase(), (int) newCategoryId);
                    }
                }

                requireActivity().runOnUiThread(() -> updateProgress(50));

                int totalPasswords = importedPasswords.size();
                int importedCount = 0;
                for (int i = 0; i < totalPasswords; i++) {
                    Password importedPassword = importedPasswords.get(i);

                    // Update the category ID if it's in our mapping
                    if (importedPassword.getCategoryId() != null &&
                        categoryIdMapping.containsKey(importedPassword.getCategoryId())) {
                        importedPassword.setCategoryId(categoryIdMapping.get(importedPassword.getCategoryId()));
                    }

                    boolean isDuplicate = false;
                    for (Password existingPassword : allPasswords) {
                        if (Objects.equals(importedPassword.getTitle(), existingPassword.getTitle()) &&
                                Objects.equals(importedPassword.getUsername(), existingPassword.getUsername()) &&
                                Objects.equals(importedPassword.getPassword(), existingPassword.getPassword()) &&
                                Objects.equals(importedPassword.getUrl(), existingPassword.getUrl()) &&
                                Objects.equals(importedPassword.getNote(), existingPassword.getNote())) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (!isDuplicate) {
                        passwordRepository.insert(importedPassword);
                        importedCount++;
                    }

                    final int progress = 50 + (int) (((i + 1) / (float) totalPasswords) * 50);
                    requireActivity().runOnUiThread(() -> updateProgress(progress));
                }

                final int finalImportedCount = importedCount;
                requireActivity().runOnUiThread(() -> {
                    if (finalImportedCount == importedPasswords.size()) {
                        Toast.makeText(requireContext(), R.string.import_success, Toast.LENGTH_SHORT).show();
                    } else if (finalImportedCount > 0) {
                        Toast.makeText(requireContext(), R.string.import_partial_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "All passwords already exist", Toast.LENGTH_SHORT).show();
                    }
                    if (activeDialog != null) activeDialog.dismiss();
                });

            } catch (Exception e) {
                Log.e(TAG, "Error importing passwords", e);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Import failed. The key may be invalid or the file is corrupt.", Toast.LENGTH_SHORT).show();
                    if (activeDialog != null) activeDialog.dismiss();
                });
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

