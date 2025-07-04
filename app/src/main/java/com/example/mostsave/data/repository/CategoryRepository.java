package com.example.mostsave.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.mostsave.data.database.AppDatabase;
import com.example.mostsave.data.database.CategoryDao;
import com.example.mostsave.data.model.Category;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CategoryRepository {
    private CategoryDao mCategoryDao;
    private LiveData<List<Category>> mAllCategories;

    public CategoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mCategoryDao = db.categoryDao();
        mAllCategories = mCategoryDao.getAllCategories();
    }

    public LiveData<List<Category>> getAllCategories() {
        return mAllCategories;
    }

    public LiveData<Category> getCategoryById(int id) {
        return mCategoryDao.getCategoryById(id);
    }

    // You must call this on a non-UI thread or use an AsyncTask/Executor.
    public Category getCategoryByName(String name) {
        // This is a synchronous call, handle threading appropriately in ViewModel or UseCase
        return mCategoryDao.getCategoryByName(name);
    }

    public void insert(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.insert(category);
        });
    }

    public void update(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.update(category);
        });
    }

    public void delete(Category category) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.delete(category);
        });
    }

    public void deleteAllCategories() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mCategoryDao.deleteAllCategories();
        });
    }

    // Synchronous method to get all categories (use carefully, blocks the thread)
    public List<Category> getAllCategoriesSync() {
        Future<List<Category>> future = AppDatabase.databaseWriteExecutor.submit(() ->
            mCategoryDao.getAllCategoriesSync());
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Synchronous method to get a category by ID (use carefully, blocks the thread)
    public Category getCategoryByIdSync(Integer categoryId) {
        if (categoryId == null) return null;
        Future<Category> future = AppDatabase.databaseWriteExecutor.submit(() ->
            mCategoryDao.getCategoryByIdSync(categoryId));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Synchronous method to insert a category and return its ID
    public long insertSync(Category category) {
        Future<Long> future = AppDatabase.databaseWriteExecutor.submit(() ->
            mCategoryDao.insertWithId(category));
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
