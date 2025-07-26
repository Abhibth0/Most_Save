package com.example.mostsave.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.mostsave.data.model.Password;
import com.example.mostsave.data.repository.PasswordRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PasswordViewModel extends AndroidViewModel {

    public enum SortOrder {
        NONE,
        A_TO_Z,
        Z_TO_A,
        NEWEST_TO_OLDEST,
        OLDEST_TO_NEWEST
    }

    private PasswordRepository mRepository;
    private LiveData<List<Password>> mAllPasswordsSource; // Original source
    private LiveData<List<Password>> mDeletedPasswords;
    private LiveData<List<Password>> mFavoritePasswordsSource;
    private MediatorLiveData<List<Password>> mSortedFavoritePasswords;

    private MutableLiveData<SortOrder> currentSortOrder = new MutableLiveData<>(SortOrder.NONE);

    // MediatorLiveData to combine source data and sort order
    private MediatorLiveData<List<Password>> mAllPasswords;
    private final MutableLiveData<String> favoriteSearchQuery = new MutableLiveData<>("");

    public PasswordViewModel(@NonNull Application application) {
        super(application);
        mRepository = new PasswordRepository(application);
        mAllPasswordsSource = mRepository.getAllPasswords();
        mDeletedPasswords = mRepository.getDeletedPasswords();
        mFavoritePasswordsSource = mRepository.getFavoritePasswords();

        mAllPasswords = new MediatorLiveData<>();
        mSortedFavoritePasswords = new MediatorLiveData<>();

        // Initialize with empty lists to prevent null values
        mSortedFavoritePasswords.setValue(new ArrayList<>());
        mAllPasswords.setValue(new ArrayList<>());

        mAllPasswords.addSource(mAllPasswordsSource, passwords -> {
            mAllPasswords.setValue(sortPasswords(passwords, currentSortOrder.getValue()));
        });

        mAllPasswords.addSource(currentSortOrder, sortOrder -> {
            mAllPasswords.setValue(sortPasswords(mAllPasswordsSource.getValue(), sortOrder));
        });

        // Add sources for favorite passwords to apply sorting
        mSortedFavoritePasswords.addSource(mFavoritePasswordsSource, passwords -> {
            mSortedFavoritePasswords.setValue(sortPasswords(passwords, currentSortOrder.getValue()));
        });

        mSortedFavoritePasswords.addSource(currentSortOrder, sortOrder -> {
            mSortedFavoritePasswords.setValue(sortPasswords(mFavoritePasswordsSource.getValue(), sortOrder));
        });
    }

    private List<Password> sortPasswords(List<Password> passwords, SortOrder sortOrder) {
        if (passwords == null || passwords.isEmpty() || sortOrder == null) {
            return passwords;
        }
        List<Password> sortedList = new ArrayList<>(passwords); // Create a mutable copy
        switch (sortOrder) {
            case A_TO_Z:
                Collections.sort(sortedList, Comparator.comparing(p -> p.getTitle().toLowerCase()));
                break;
            case Z_TO_A:
                Collections.sort(sortedList, (p1, p2) -> p2.getTitle().toLowerCase().compareTo(p1.getTitle().toLowerCase()));
                break;
            case NEWEST_TO_OLDEST:
                Collections.sort(sortedList, Comparator.comparingLong(Password::getLastUpdated).reversed());
                break;
            case OLDEST_TO_NEWEST:
                Collections.sort(sortedList, Comparator.comparingLong(Password::getLastUpdated));
                break;
            case NONE:
            default:
                // No explicit sort, rely on original order from repository (likely by ID or insertion)
                // Or, if you want a specific default sort (e.g. by ID asc for NONE):
                // Collections.sort(sortedList, Comparator.comparingInt(Password::getId));
                break;
        }
        return sortedList;
    }

    public LiveData<List<Password>> getAllPasswords() {
        return mAllPasswords; // Return the mediated LiveData
    }

    public void setSortOrder(SortOrder sortOrder) {
        currentSortOrder.setValue(sortOrder);
    }

    public LiveData<List<Password>> getDeletedPasswords() {
        return mDeletedPasswords;
    }

    public LiveData<List<Password>> getFavoritePasswords() {
        return mSortedFavoritePasswords; // Return the sorted favorites
    }

    public void setFavoriteSearchQuery(String query) {
        favoriteSearchQuery.setValue(query);
    }

    public LiveData<List<Password>> getFilteredFavoritePasswords() {
        MediatorLiveData<List<Password>> result = new MediatorLiveData<>();
        result.setValue(new ArrayList<>()); // Initialize with empty list to prevent null

        result.addSource(mSortedFavoritePasswords, passwords -> {
            if (passwords == null) {
                result.setValue(new ArrayList<>());
                return;
            }

            String query = favoriteSearchQuery.getValue();
            if (query == null || query.isEmpty()) {
                result.setValue(passwords);
                return;
            }

            List<Password> filteredList = new ArrayList<>();
            for (Password password : passwords) {
                if (password.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(password);
                }
            }
            result.setValue(filteredList);
        });

        result.addSource(favoriteSearchQuery, query -> {
            List<Password> passwords = mSortedFavoritePasswords.getValue();
            if (passwords == null) {
                result.setValue(new ArrayList<>());
                return;
            }

            if (query == null || query.isEmpty()) {
                result.setValue(passwords);
                return;
            }

            List<Password> filteredList = new ArrayList<>();
            for (Password password : passwords) {
                if (password.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(password);
                }
            }
            result.setValue(filteredList);
        });

        return result;
    }

    public LiveData<Password> getPasswordById(int id) {
        return mRepository.getPasswordById(id);
    }

    public void insert(Password password) {
        mRepository.insert(password);
    }

    public void update(Password password) {
        // Ensure lastUpdated is set on update
        password.setLastUpdated(System.currentTimeMillis());
        mRepository.update(password);
    }

    public void softDelete(int passwordId) {
        mRepository.softDelete(passwordId);
    }

    public void restorePassword(int passwordId) {
        mRepository.restorePassword(passwordId);
    }

    public void deletePermanently(int passwordId) {
        mRepository.deletePermanently(passwordId);
    }

    public void deleteAllPermanently() {
        mRepository.deleteAllPermanently();
    }

    public void deletePasswords(List<Password> passwords) {
        mRepository.deletePasswords(passwords);
    }

    public void movePasswordsToRecycleBin(List<Password> passwords) {
        List<Integer> passwordIds = new ArrayList<>();
        for (Password password : passwords) {
            passwordIds.add(password.id);
        }
        mRepository.softDelete(passwordIds);
    }

    public LiveData<List<Password>> searchPasswords(String query) {
        // This repository method should ideally also respect the current sort order
        // or return raw data to be sorted/filtered by a MediatorLiveData here.
        // For now, assuming it returns unsorted search results.
        // To integrate sorting with search, you might need another MediatorLiveData
        // that combines search results with sort order.
        return Transformations.map(mRepository.searchPasswords(query), passwords -> sortPasswords(passwords, currentSortOrder.getValue()));
    }

    public LiveData<List<Password>> getPasswordsByCategoryId(int categoryId) {
        // Apply sorting to category-specific passwords
        return Transformations.map(mRepository.getPasswordsByCategoryId(categoryId), passwords -> sortPasswords(passwords, currentSortOrder.getValue()));
    }

    public void updateFavoriteStatus(int passwordId, boolean isFavorite) {
        mRepository.updateFavoriteStatus(passwordId, isFavorite);
    }

    public void toggleFavoriteStatus(int passwordId, boolean currentStatus) {
        mRepository.toggleFavoriteStatus(passwordId, currentStatus);
    }
}
