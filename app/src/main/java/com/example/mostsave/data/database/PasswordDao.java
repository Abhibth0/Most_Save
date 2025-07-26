package com.example.mostsave.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mostsave.data.model.Password;

import java.util.List;

@Dao
public interface PasswordDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Password password);

    @Update
    void update(Password password);

    @Delete
    void delete(Password password);

    @Delete
    void delete(List<Password> passwords);

    @Query("DELETE FROM passwords")
    void deleteAllPasswords();

    @Query("SELECT * FROM passwords WHERE is_deleted = 0 ORDER BY title ASC")
    LiveData<List<Password>> getAllPasswords();

    @Query("SELECT * FROM passwords WHERE is_deleted = 1 ORDER BY title ASC")
    LiveData<List<Password>> getDeletedPasswords();

    @Query("SELECT * FROM passwords WHERE id = :passwordId")
    LiveData<Password> getPasswordById(int passwordId);

    @Query("UPDATE passwords SET is_deleted = 1 WHERE id = :passwordId")
    void softDelete(int passwordId);

    @Query("UPDATE passwords SET is_deleted = 1 WHERE id IN (:passwordIds)")
    void softDelete(List<Integer> passwordIds);

    @Query("UPDATE passwords SET is_deleted = 0 WHERE id = :passwordId")
    void restorePassword(int passwordId);

    @Query("DELETE FROM passwords WHERE id = :passwordId AND is_deleted = 1")
    void deletePermanently(int passwordId);

    @Query("DELETE FROM passwords WHERE is_deleted = 1")
    void deleteAllPermanently();

    @Query("SELECT * FROM passwords WHERE category_id = :categoryId AND is_deleted = 0 ORDER BY title ASC")
    LiveData<List<Password>> getPasswordsByCategoryId(int categoryId);

    @Query("SELECT * FROM passwords WHERE title LIKE :searchQuery AND is_deleted = 0 ORDER BY title ASC")
    LiveData<List<Password>> searchPasswords(String searchQuery);

    @Query("SELECT * FROM passwords WHERE is_favorite = 1 AND is_deleted = 0 ORDER BY title ASC")
    LiveData<List<Password>> getFavoritePasswords();

    @Query("UPDATE passwords SET is_favorite = :isFavorite WHERE id = :passwordId")
    void updateFavoriteStatus(int passwordId, boolean isFavorite);
}
