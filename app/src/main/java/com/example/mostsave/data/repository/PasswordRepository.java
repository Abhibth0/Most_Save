package com.example.mostsave.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.mostsave.data.database.AppDatabase;
import com.example.mostsave.data.database.PasswordDao;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.data.security.EncryptionHelper;
import java.util.List;

public class PasswordRepository {
    private final PasswordDao mPasswordDao;
    private final LiveData<List<Password>> mAllPasswords;
    private final LiveData<List<Password>> mDeletedPasswords;
    private final LiveData<List<Password>> mFavoritePasswords;
    private final String passphrase;

    public PasswordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mPasswordDao = db.passwordDao();
        passphrase = AppDatabase.getPassphrase(application);

        mAllPasswords = Transformations.map(mPasswordDao.getAllPasswords(), passwords -> {
            for (Password p : passwords) {
                try {
                    p.password = EncryptionHelper.decrypt(p.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error, e.g., log it or set password to null
                }
            }
            return passwords;
        });

        mDeletedPasswords = Transformations.map(mPasswordDao.getDeletedPasswords(), passwords -> {
            for (Password p : passwords) {
                try {
                    p.password = EncryptionHelper.decrypt(p.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error
                }
            }
            return passwords;
        });

        mFavoritePasswords = Transformations.map(mPasswordDao.getFavoritePasswords(), passwords -> {
            for (Password p : passwords) {
                try {
                    p.password = EncryptionHelper.decrypt(p.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error
                }
            }
            return passwords;
        });
    }

    public LiveData<List<Password>> getAllPasswords() {
        return mAllPasswords;
    }

    public LiveData<List<Password>> getDeletedPasswords() {
        return mDeletedPasswords;
    }

    public LiveData<List<Password>> getFavoritePasswords() {
        return mFavoritePasswords;
    }

    public LiveData<Password> getPasswordById(int id) {
        return Transformations.map(mPasswordDao.getPasswordById(id), password -> {
            if (password != null) {
                try {
                    password.password = EncryptionHelper.decrypt(password.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error
                }
            }
            return password;
        });
    }

    public void insert(Password password) {
        try {
            password.password = EncryptionHelper.encrypt(password.password, passphrase);
        } catch (Exception e) {
            // Handle encryption error
        }
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.insert(password));
    }

    public void update(Password password) {
        try {
            password.password = EncryptionHelper.encrypt(password.password, passphrase);
        } catch (Exception e) {
            // Handle encryption error
        }
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.update(password));
    }

    // No direct delete, use softDelete or deletePermanently
    // public void delete(Password password) {
    //     AppDatabase.databaseWriteExecutor.execute(() -> {
    //         mPasswordDao.delete(password);
    //     });
    // }

    public void softDelete(int passwordId) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.softDelete(passwordId));
    }

    public void softDelete(List<Integer> passwordIds) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.softDelete(passwordIds));
    }

    public void restorePassword(int passwordId) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.restorePassword(passwordId));
    }

    public void deletePermanently(int passwordId) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.deletePermanently(passwordId));
    }

    public void deleteAllPermanently() {
        AppDatabase.databaseWriteExecutor.execute(mPasswordDao::deleteAllPermanently);
    }

    public void deletePasswords(List<Password> passwords) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.delete(passwords));
    }

    public LiveData<List<Password>> getPasswordsByCategoryId(int categoryId) {
        return Transformations.map(mPasswordDao.getPasswordsByCategoryId(categoryId), passwords -> {
            for (Password p : passwords) {
                try {
                    p.password = EncryptionHelper.decrypt(p.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error
                }
            }
            return passwords;
        });
    }

    public LiveData<List<Password>> searchPasswords(String query) {
        return Transformations.map(mPasswordDao.searchPasswords("%" + query + "%"), passwords -> {
            for (Password p : passwords) {
                try {
                    p.password = EncryptionHelper.decrypt(p.password, passphrase);
                } catch (Exception e) {
                    // Handle decryption error
                }
            }
            return passwords;
        }); // Add wildcards for LIKE query
    }

    public void updateFavoriteStatus(int passwordId, boolean isFavorite) {
        AppDatabase.databaseWriteExecutor.execute(() -> mPasswordDao.updateFavoriteStatus(passwordId, isFavorite));
    }

    public void toggleFavoriteStatus(int passwordId, boolean currentStatus) {
        updateFavoriteStatus(passwordId, !currentStatus);
    }
}
