package com.example.mostsave.data.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;


import com.example.mostsave.data.model.Category;
import com.example.mostsave.data.model.Password;
import com.mostsave.data.dao.HistoryDao;
import com.mostsave.data.model.History;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Password.class, Category.class, History.class}, version = 5, exportSchema = false) // Incremented version for isFavorite field
public abstract class AppDatabase extends RoomDatabase {

    public abstract PasswordDao passwordDao();
    public abstract CategoryDao categoryDao();
    public abstract HistoryDao historyDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
        Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private static String passphrase;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    passphrase = getPassphrase(context);
                    SupportFactory factory = new SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()));
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "most_save_database_encrypted") // Use consistent name
                            .openHelperFactory(factory)
                            .addCallback(sRoomDatabaseCallback)
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static String getPassphrase(final Context context) {
        if (passphrase != null) {
            return passphrase;
        }
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secret_shared_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            String storedPassphrase = sharedPreferences.getString("db_passphrase", null);
            if (storedPassphrase == null) {
                byte[] key = new byte[32];
                new SecureRandom().nextBytes(key);
                storedPassphrase = Base64.encodeToString(key, Base64.NO_WRAP);
                sharedPreferences.edit().putString("db_passphrase", storedPassphrase).apply();
            }
            passphrase = storedPassphrase;
            return passphrase;
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Could not create or retrieve master key", e);
        }
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE passwords ADD COLUMN icon_res_id INTEGER NOT NULL DEFAULT 0");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE passwords ADD COLUMN icon_path TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Migration from version 3 to 4
            // Add any changes that happened in version 4
        }
    };

    static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add is_favorite column to passwords table
            database.execSQL("ALTER TABLE passwords ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0");

            // Check if categories exist, if not insert them
            database.execSQL("INSERT OR IGNORE INTO categories (id, name, display_order) VALUES (1, 'Social', 1)");
            database.execSQL("INSERT OR IGNORE INTO categories (id, name, display_order) VALUES (2, 'Personal', 2)");
            database.execSQL("INSERT OR IGNORE INTO categories (id, name, display_order) VALUES (3, 'Work', 3)");
            database.execSQL("INSERT OR IGNORE INTO categories (id, name, display_order) VALUES (4, 'Others', 4)");
        }
    };

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                // Insert default categories with display order using raw SQL
                db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Social', 1)");
                db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Personal', 2)");
                db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Work', 3)");
                db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Others', 4)");
            });
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Check and ensure categories exist when database is opened
            databaseWriteExecutor.execute(() -> {
                // Query to see if any categories exist
                long count = db.compileStatement("SELECT COUNT(*) FROM categories").simpleQueryForLong();

                // If no categories exist, insert the defaults
                if (count == 0) {
                    db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Social', 1)");
                    db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Personal', 2)");
                    db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Work', 3)");
                    db.execSQL("INSERT INTO categories (name, display_order) VALUES ('Others', 4)");
                }
            });
        }
    };
}
