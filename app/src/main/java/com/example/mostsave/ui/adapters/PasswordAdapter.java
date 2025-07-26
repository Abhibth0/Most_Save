package com.example.mostsave.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.PasswordTransformationMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mostsave.R;
import com.example.mostsave.data.model.Password;
import com.google.android.material.card.MaterialCardView;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PasswordAdapter extends ListAdapter<Password, PasswordAdapter.PasswordViewHolder> {

    private final OnPasswordClickListener passwordClickListener;
    private final OnCopyClickListener copyClickListener;
    private final OnToggleVisibilityListener toggleVisibilityListener;
    private final OnPasswordLongClickListener passwordLongClickListener;
    private final OnPasswordSelectListener passwordSelectListener;
    private final Map<Integer, Boolean> passwordVisibilityState = new HashMap<>();
    private boolean isMultiSelectMode = false;
    private List<Password> selectedItems = new ArrayList<>();

    public interface OnPasswordClickListener {
        void onPasswordClick(Password password);
    }

    public interface OnCopyClickListener {
        void onCopyClick(Password password);
    }

    public interface OnToggleVisibilityListener {
        void onToggleVisibilityClick(Password password, int position, boolean isVisible);
    }

    public interface OnPasswordLongClickListener {
        void onPasswordLongClick(Password password);
    }

    public interface OnPasswordSelectListener {
        void onPasswordSelect(Password password);
    }

    public PasswordAdapter(OnPasswordClickListener passwordClickListener,
                           OnCopyClickListener copyClickListener,
                           OnToggleVisibilityListener toggleVisibilityListener,
                           OnPasswordLongClickListener passwordLongClickListener,
                           OnPasswordSelectListener passwordSelectListener) {
        super(DIFF_CALLBACK);
        this.passwordClickListener = passwordClickListener;
        this.copyClickListener = copyClickListener;
        this.toggleVisibilityListener = toggleVisibilityListener;
        this.passwordLongClickListener = passwordLongClickListener;
        this.passwordSelectListener = passwordSelectListener;
    }

    private static final DiffUtil.ItemCallback<Password> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Password oldItem, @NonNull Password newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Password oldItem, @NonNull Password newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   Objects.equals(oldItem.getUsername(), newItem.getUsername()) &&
                   oldItem.getPassword().equals(newItem.getPassword()) && // Be cautious with direct password comparison if it changes
                   oldItem.isDeleted() == newItem.isDeleted() &&
                   oldItem.getIconResId() == newItem.getIconResId() &&
                   Objects.equals(oldItem.getIconPath(), newItem.getIconPath());
        }
    };

    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password_card, parent, false);
        return new PasswordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password currentPassword = getItem(position);
        holder.textViewTitle.setText(currentPassword.getTitle());
        holder.textViewUsername.setText(currentPassword.getUsername() != null ? currentPassword.getUsername() : "");

        if (currentPassword.getIconPath() != null && !currentPassword.getIconPath().isEmpty()) {
            File file = new File(currentPassword.getIconPath());
            if(file.exists()){
                holder.imageViewIcon.setImageURI(Uri.fromFile(file));
                holder.imageViewIcon.setVisibility(View.VISIBLE);
            } else {
                holder.imageViewIcon.setVisibility(View.GONE);
            }
        } else if (currentPassword.getIconResId() != 0) {
            holder.imageViewIcon.setImageResource(currentPassword.getIconResId());
            holder.imageViewIcon.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewIcon.setVisibility(View.GONE);
        }

        holder.checkBoxSelect.setVisibility(isMultiSelectMode ? View.VISIBLE : View.GONE);
        holder.checkBoxSelect.setChecked(selectedItems.contains(currentPassword));

        holder.checkBoxSelect.setOnClickListener(v -> {
            if (passwordSelectListener != null) {
                passwordSelectListener.onPasswordSelect(currentPassword);
            }
        });

        if (holder.itemView instanceof MaterialCardView) {
            ((MaterialCardView) holder.itemView).setChecked(selectedItems.contains(currentPassword));
        }

        Boolean isVisible = passwordVisibilityState.get(currentPassword.id);
        if (isVisible == null) {
            isVisible = false;
        }

        if (isVisible) {
            holder.textViewPassword.setTransformationMethod(null); // Show password
            holder.buttonShowHidePassword.setImageResource(R.drawable.ic_visibility);
        } else {
            holder.textViewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance()); // Hide password
            holder.buttonShowHidePassword.setImageResource(R.drawable.ic_visibility_off);
        }
        holder.textViewPassword.setText(currentPassword.getPassword());

        holder.itemView.setOnClickListener(v -> {
            if (passwordClickListener != null) {
                passwordClickListener.onPasswordClick(currentPassword);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (passwordLongClickListener != null) {
                passwordLongClickListener.onPasswordLongClick(currentPassword);
                return true;
            }
            return false;
        });

        holder.buttonCopyPassword.setOnClickListener(v -> {
            if (copyClickListener != null) {
                copyClickListener.onCopyClick(currentPassword);
            }
        });

        holder.buttonShowHidePassword.setOnClickListener(v -> {
            boolean newState = !passwordVisibilityState.getOrDefault(currentPassword.id, false);
            passwordVisibilityState.put(currentPassword.id, newState);
            notifyItemChanged(holder.getAdapterPosition()); // Refresh this item
            if (toggleVisibilityListener != null) {
                // This listener might not be strictly needed if state is handled internally
                // but can be used for external logic if required.
                toggleVisibilityListener.onToggleVisibilityClick(currentPassword, holder.getAdapterPosition(), newState);
            }
        });

        // Apply theme-based stroke colors for selected cards in multi-select mode
        if (holder.itemView instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) holder.itemView;

            if (isMultiSelectMode && selectedItems.contains(currentPassword)) {
                // Get the current theme's primary color
                TypedValue typedValue = new TypedValue();
                holder.itemView.getContext().getTheme().resolveAttribute(
                        com.google.android.material.R.attr.colorPrimary, typedValue, true);
                int primaryColor = typedValue.data;

                cardView.setStrokeColor(primaryColor);
                cardView.setStrokeWidth(3); // Set thinner stroke width
                cardView.setChecked(true);
            } else {
                cardView.setStrokeColor(Color.TRANSPARENT);
                cardView.setStrokeWidth(0);
                cardView.setChecked(false);
            }
        }
    }

    /**
     * Animate item deletion with slide out effect
     */
    public void animateItemRemoval(int position, Runnable onAnimationEnd) {
        if (position >= 0 && position < getItemCount()) {
            // This method would be called before actually removing the item from the list
            // The animation will be handled in the ViewHolder
            notifyItemRemoved(position);
            if (onAnimationEnd != null) {
                onAnimationEnd.run();
            }
        }
    }

    /**
     * Slide out animation for item deletion
     */
    private void slideOutItem(View itemView, Runnable onAnimationEnd) {
        ObjectAnimator slideOut = ObjectAnimator.ofFloat(itemView, "translationX", 0f, itemView.getWidth());
        slideOut.setDuration(300);
        slideOut.setInterpolator(new AccelerateInterpolator());

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

    public void setMultiSelectMode(boolean multiSelectMode) {
        this.isMultiSelectMode = multiSelectMode;
        if (!multiSelectMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setSelectedItems(List<Password> selectedItems) {
        this.selectedItems = new ArrayList<>(selectedItems);
        notifyDataSetChanged();
    }

    public static class PasswordViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTitle;
        private final TextView textViewUsername;
        private final TextView textViewPassword;
        private final ImageButton buttonCopyPassword;
        private final ImageButton buttonShowHidePassword;
        private final ImageView imageViewIcon;
        private final android.widget.CheckBox checkBoxSelect;
        private final MaterialCardView cardView; // Add cardView reference

        public PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.text_view_password_title);
            textViewUsername = itemView.findViewById(R.id.text_view_username);
            textViewPassword = itemView.findViewById(R.id.text_view_password_masked);
            buttonCopyPassword = itemView.findViewById(R.id.button_copy_password);
            buttonShowHidePassword = itemView.findViewById(R.id.button_show_hide_password);
            imageViewIcon = itemView.findViewById(R.id.image_view_password_icon);
            checkBoxSelect = itemView.findViewById(R.id.checkbox_select_password);
            cardView = (MaterialCardView) itemView; // Initialize cardView
        }
    }
}
