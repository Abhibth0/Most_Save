package com.example.mostsave.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.annotation.NonNull;

@Entity(tableName = "categories")
public class Category {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "display_order") // Added display_order column
    public int displayOrder;            // Added display_order field

    public Category(@NonNull String name, int displayOrder) { // Updated constructor
        this.name = name;
        this.displayOrder = displayOrder; // Set display_order in constructor
    }

    // Getters and setters (optional, Room can access fields directly)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getDisplayOrder() { // Added getter for displayOrder
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) { // Added setter for displayOrder
        this.displayOrder = displayOrder;
    }
}
