package com.example.mostsave.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mostsave.data.model.Category;

import java.util.List;

@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Category category); // Return long to get the ID of the inserted category

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("DELETE FROM categories")
    void deleteAllCategories();

    @Query("SELECT * FROM categories ORDER BY display_order ASC") // Changed to order by display_order
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY display_order ASC")
    List<Category> getAllCategoriesSync(); // Synchronous version for import operation

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    LiveData<Category> getCategoryById(int categoryId);

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    Category getCategoryByIdSync(int categoryId); // Synchronous version for import/export

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name); // For checking duplicate names

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertWithId(Category category); // For synchronous insert during import
}
