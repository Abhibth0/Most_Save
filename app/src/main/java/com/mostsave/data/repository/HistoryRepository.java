package com.mostsave.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.mostsave.data.database.AppDatabase;
import com.mostsave.data.dao.HistoryDao;
import com.mostsave.data.model.History;

import java.util.List;

public class HistoryRepository {

    private final HistoryDao mHistoryDao;
    private final LiveData<List<History>> mAllHistory;

    public HistoryRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mHistoryDao = db.historyDao();
        mAllHistory = mHistoryDao.getAllHistory();
    }

    public LiveData<List<History>> getAllHistory() {
        return mAllHistory;
    }

    public void insert(History history) {
        AppDatabase.databaseWriteExecutor.execute(() -> mHistoryDao.insert(history));
    }

    public void clearHistory() {
        AppDatabase.databaseWriteExecutor.execute(mHistoryDao::clearHistory);
    }
}
