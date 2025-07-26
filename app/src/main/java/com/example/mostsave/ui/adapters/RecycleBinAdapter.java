package com.example.mostsave.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mostsave.R;
import com.example.mostsave.data.model.Password;

public class RecycleBinAdapter extends ListAdapter<RecycleBinAdapter.RecycledPasswordDisplayItem, RecycleBinAdapter.ViewHolder> {

    private final OnRestoreListener restoreListener;
    private final OnDeletePermanentlyListener deletePermanentlyListener;

    public interface OnRestoreListener {
        void onRestoreClicked(Password password);
    }

    public interface OnDeletePermanentlyListener {
        void onDeletePermanentlyClicked(Password password);
    }

    public RecycleBinAdapter(OnRestoreListener restoreListener, OnDeletePermanentlyListener deletePermanentlyListener) {
        super(DIFF_CALLBACK);
        this.restoreListener = restoreListener;
        this.deletePermanentlyListener = deletePermanentlyListener;
    }

    private static final DiffUtil.ItemCallback<RecycledPasswordDisplayItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<RecycledPasswordDisplayItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull RecycledPasswordDisplayItem oldItem, @NonNull RecycledPasswordDisplayItem newItem) {
            return oldItem.password.id == newItem.password.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull RecycledPasswordDisplayItem oldItem, @NonNull RecycledPasswordDisplayItem newItem) {
            return oldItem.password.getTitle().equals(newItem.password.getTitle()) &&
                   oldItem.categoryName.equals(newItem.categoryName);
        }
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_deleted_password, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecycledPasswordDisplayItem currentItem = getItem(position);
        holder.bind(currentItem, restoreListener, deletePermanentlyListener);
    }

    /**
     * Animate item for clear all operation with staggered effect
     */
    public void animateItemClearAll(View itemView, int position, int totalItems, Runnable onAnimationEnd) {
        // Staggered slide out animation for clear all
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(itemView, "translationX", 0f, -itemView.getWidth() * 1.2f);
        slideOut.setDuration(300 + (position * 30)); // Staggered duration
        slideOut.setInterpolator(new DecelerateInterpolator());

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f);
        fadeOut.setDuration(300 + (position * 30));

        slideOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onAnimationEnd != null) {
                    onAnimationEnd.run();
                }
            }
        });

        slideOut.start();
        fadeOut.start();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewCategory;
        private final ImageButton buttonRestore;
        private final ImageButton buttonDeletePermanently;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_deleted_password_title);
            textViewCategory = itemView.findViewById(R.id.text_view_deleted_password_category);
            buttonRestore = itemView.findViewById(R.id.button_restore_password);
            buttonDeletePermanently = itemView.findViewById(R.id.button_delete_permanently);
        }

        public void bind(RecycledPasswordDisplayItem item, OnRestoreListener restoreListener, OnDeletePermanentlyListener deleteListener) {
            textViewTitle.setText(item.password.getTitle());
            textViewCategory.setText(item.categoryName);

            // Reset view state first (fix for cancel issue)
            itemView.setTranslationX(0f);
            itemView.setAlpha(1f);
            itemView.setVisibility(View.VISIBLE);

            buttonRestore.setOnClickListener(v -> {
                if (restoreListener != null) {
                    // Animate restore with slide back effect
                    animateRestoreItem(itemView, () -> {
                        restoreListener.onRestoreClicked(item.password);
                    });
                }
            });

            buttonDeletePermanently.setOnClickListener(v -> {
                if (deleteListener != null) {
                    // Show dialog first, then animate after confirmation
                    deleteListener.onDeletePermanentlyClicked(item.password);
                }
            });
        }

        /**
         * Animate item restore with slide back effect
         */
        private void animateRestoreItem(View itemView, Runnable onAnimationEnd) {
            // First slide out to the right
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(itemView, "translationX", 0f, itemView.getWidth());
            slideOut.setDuration(250);
            slideOut.setInterpolator(new DecelerateInterpolator());

            // Then fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0.3f);
            fadeOut.setDuration(250);

            slideOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                }
            });

            slideOut.start();
            fadeOut.start();
        }

        /**
         * Animate item permanent deletion with slide out effect
         */
        private void animateDeleteItem(View itemView, Runnable onAnimationEnd) {
            // Slide out to the left
            ObjectAnimator slideOut = ObjectAnimator.ofFloat(itemView, "translationX", 0f, -itemView.getWidth());
            slideOut.setDuration(300);
            slideOut.setInterpolator(new DecelerateInterpolator());

            // Fade out
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(itemView, "alpha", 1f, 0f);
            fadeOut.setDuration(300);

            slideOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (onAnimationEnd != null) {
                        onAnimationEnd.run();
                    }
                }
            });

            slideOut.start();
            fadeOut.start();
        }
    }

    // Helper class to hold Password and its resolved category name
    public static class RecycledPasswordDisplayItem {
        public final Password password;
        public final String categoryName;

        public RecycledPasswordDisplayItem(Password password, String categoryName) {
            this.password = password;
            this.categoryName = categoryName;
        }

        // Optional: equals and hashCode if these items are put in sets or used as map keys directly
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RecycledPasswordDisplayItem that = (RecycledPasswordDisplayItem) o;
            return password.id == that.password.id &&
                   categoryName.equals(that.categoryName);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(password.id, categoryName);
        }
    }
}
