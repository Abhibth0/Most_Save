package com.mostsave.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class History {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int passwordId;

    public String passwordTitle;

    public long viewedTimestamp;
}

