package com.example.mostsave.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;
import com.example.mostsave.data.model.Password;
import com.example.mostsave.util.PasswordAnalyzer;

public class PasswordAnalysisAdapter extends ListAdapter<Password, PasswordAnalysisAdapter.PasswordViewHolder> {

    private final OnPasswordActionListener listener;

    public interface OnPasswordActionListener {
        void onUpdateClicked(Password password);
    }

    public PasswordAnalysisAdapter(OnPasswordActionListener listener) {
        super(new PasswordDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_analysis_password, parent, false);
        return new PasswordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password password = getItem(position);
        holder.bind(password, listener);
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView infoText;
        private final Button updateButton;

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_password_title);
            infoText = itemView.findViewById(R.id.text_password_info);
            updateButton = itemView.findViewById(R.id.button_update);
        }

        public void bind(Password password, OnPasswordActionListener listener) {
            titleText.setText(password.title);

            // Display password age
            String passwordAge = itemView.getContext().getString(
                    R.string.analysis_password_age,
                    PasswordAnalyzer.getPasswordAge(password.getLastUpdated())
            );
            infoText.setText(passwordAge);

            // Set up update button click listener
            updateButton.setOnClickListener(v -> listener.onUpdateClicked(password));
        }
    }

    private static class PasswordDiffCallback extends DiffUtil.ItemCallback<Password> {
        @Override
        public boolean areItemsTheSame(@NonNull Password oldItem, @NonNull Password newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Password oldItem, @NonNull Password newItem) {
            return oldItem.equals(newItem);
        }
    }
}
