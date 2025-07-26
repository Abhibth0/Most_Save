package com.example.mostsave.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Entity(tableName = "passwords",
        foreignKeys = @ForeignKey(entity = Category.class,
                                  parentColumns = "id",
                                  childColumns = "category_id",
                                  onDelete = ForeignKey.SET_NULL)) // Or CASCADE, depending on desired behavior
public class Password {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "title")
    public String title;

    @Nullable
    @ColumnInfo(name = "username")
    public String username;

    @NonNull
    @ColumnInfo(name = "password")
    public String password; // Consider encrypting this

    @Nullable
    @ColumnInfo(name = "note")
    public String note;

    @Nullable
    @ColumnInfo(name = "url")
    public String url; // Added URL field

    @Nullable
    @ColumnInfo(name = "category_id", index = true)
    public Integer categoryId; // Nullable if category is optional or can be deleted

    @ColumnInfo(name = "is_deleted", defaultValue = "0") // For recycle bin functionality
    public boolean isDeleted;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated; // Ensure this field exists

    @ColumnInfo(name = "icon_res_id", defaultValue = "0")
    public int iconResId;

    @Nullable
    @ColumnInfo(name = "icon_path")
    public String iconPath; // Path to custom icon if used

    @ColumnInfo(name = "is_favorite", defaultValue = "0")
    public boolean isFavorite; // Added is_favorite field

    @Ignore
    @Nullable
    private String categoryName; // Transient field to store category name during import/export

    // Constructor
    public Password(@NonNull String title, @Nullable String username, @NonNull String password,
                    @Nullable String note, @Nullable Integer categoryId, @Nullable String url) { // Added url to constructor
        this.title = title;
        this.username = username;
        this.password = password;
        this.note = note;
        this.categoryId = categoryId;
        this.url = url; // Initialize URL
        this.isDeleted = false; // Default to not deleted
        this.isFavorite = false; // Default to not favorite
        this.lastUpdated = System.currentTimeMillis(); // Set current time on creation
    }

    // Getter and setter for the transient categoryName field
    @Nullable
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(@Nullable String categoryName) {
        this.categoryName = categoryName;
    }

    // Getters and Setters (Room can access fields directly, but good for encapsulation)

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp when title changes
    }

    // Add getter for lastUpdated
    public long getLastUpdated() {
        return lastUpdated;
    }

    // Add setter for lastUpdated
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public void setUsername(@Nullable String username) {
        this.username = username;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    @Nullable
    public String getNote() {
        return note;
    }

    @SuppressWarnings("unused")
    public void setNote(@Nullable String note) {
        this.note = note;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    @SuppressWarnings("unused")
    public void setUrl(@Nullable String url) {
        this.url = url;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    @Nullable
    public Integer getCategoryId() {
        return categoryId;
    }

    @SuppressWarnings("unused")
    public void setCategoryId(@Nullable Integer categoryId) {
        this.categoryId = categoryId;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
        this.lastUpdated = System.currentTimeMillis();
    }

    @Nullable
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(@Nullable String iconPath) {
        this.iconPath = iconPath;
        this.lastUpdated = System.currentTimeMillis();
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite;
        this.lastUpdated = System.currentTimeMillis(); // Update timestamp when favorite status changes
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return id == password.id;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
