package com.mostsave.ui.history;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mostsave.data.model.History;
import com.example.mostsave.R;

import java.text.DateFormat;
import java.util.Date;

public class HistoryAdapter extends ListAdapter<History, HistoryAdapter.HistoryViewHolder> {

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<History> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull History oldItem, @NonNull History newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull History oldItem, @NonNull History newItem) {
            return oldItem.passwordTitle.equals(newItem.passwordTitle) &&
                    oldItem.viewedTimestamp == newItem.viewedTimestamp;
        }
    };

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History current = getItem(position);
        if (current != null) {
            holder.passwordTitleText.setText(current.passwordTitle);
            String formattedDate = DateFormat.getDateTimeInstance().format(new Date(current.viewedTimestamp));
            holder.timestampText.setText(formattedDate);
        } else {
            Context context = holder.itemView.getContext();
            holder.passwordTitleText.setText(context.getString(R.string.no_title));
            holder.timestampText.setText("");
        }
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView passwordTitleText;
        private final TextView timestampText;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            passwordTitleText = itemView.findViewById(R.id.password_title_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }
    }
}
