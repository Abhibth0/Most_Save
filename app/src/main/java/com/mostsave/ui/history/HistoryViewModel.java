package com.mostsave.ui.history;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.mostsave.data.model.History;
import com.mostsave.data.repository.HistoryRepository;

import java.util.List;

public class HistoryViewModel extends AndroidViewModel {

    private final LiveData<List<History>> mAllHistory;

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        HistoryRepository repository = new HistoryRepository(application);
        mAllHistory = repository.getAllHistory();
    }

    public LiveData<List<History>> getAllHistory() {
        return mAllHistory;
    }

    public void clearHistory() {
        HistoryRepository repository = new HistoryRepository(getApplication());
        repository.clearHistory();
    }
}
