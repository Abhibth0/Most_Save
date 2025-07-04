package com.example.mostsave.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.repository.CategoryRepository;
import java.util.List;

public class CategoryViewModel extends AndroidViewModel {

    private CategoryRepository mRepository;
    private LiveData<List<Category>> mAllCategories;

    public CategoryViewModel(@NonNull Application application) {
        super(application);
        mRepository = new CategoryRepository(application);
        mAllCategories = mRepository.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return mAllCategories;
    }

    public LiveData<Category> getCategoryById(int id) {
        return mRepository.getCategoryById(id);
    }

    // This method is synchronous and should be called from a background thread.
    public Category getCategoryByName(String name) {
        return mRepository.getCategoryByName(name);
    }

    public void insert(Category category) {
        mRepository.insert(category);
    }

    public void update(Category category) {
        mRepository.update(category);
    }

    public void delete(Category category) {
        mRepository.delete(category);
    }

    public void deleteAllCategories() {
        mRepository.deleteAllCategories();
    }
}

