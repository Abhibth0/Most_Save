package com.mostsave.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;
import com.google.android.material.snackbar.Snackbar;
import com.mostsave.data.model.History;

import java.util.List;

public class HistoryFragment extends Fragment {

    private HistoryViewModel historyViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.history_menu, menu);
            }

            @Override
            public void onPrepareMenu(@NonNull Menu menu) {
                MenuItem clearHistoryItem = menu.findItem(R.id.action_clear_history);
                if (clearHistoryItem != null) {
                    List<History> historyList = historyViewModel.getAllHistory().getValue();
                    clearHistoryItem.setVisible(historyList != null && !historyList.isEmpty());
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_clear_history) {
                    showClearHistoryConfirmationDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        RecyclerView recyclerView = view.findViewById(R.id.history_recycler_view);
        final HistoryAdapter adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        TextView noHistoryText = view.findViewById(R.id.no_history_text);

        historyViewModel.getAllHistory().observe(getViewLifecycleOwner(), history -> {
            if (history == null || history.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                noHistoryText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noHistoryText.setVisibility(View.GONE);
                adapter.submitList(history);
            }
            requireActivity().invalidateOptionsMenu();
        });
    }

    private void showClearHistoryConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_clear_history_title)
                .setMessage(R.string.confirm_clear_history_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    historyViewModel.clearHistory();
                    Snackbar.make(requireView(), R.string.history_cleared_successfully, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(R.drawable.ic_delete_forever)
                .show();
    }
}
