package com.mostsave.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.mostsave.data.model.History;

import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(History history);

    @Query("SELECT * FROM history ORDER BY viewedTimestamp DESC")
    LiveData<List<History>> getAllHistory();

    @Query("DELETE FROM history")
    void clearHistory();
}
