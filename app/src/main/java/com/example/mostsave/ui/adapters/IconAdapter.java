package com.example.mostsave.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mostsave.R;
import com.example.mostsave.ui.dialogs.ChooseIconDialogFragment.IconItem;

import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder> {

    private List<IconItem> iconList;
    private OnIconClickListener onIconClickListener;
    private IconItem selectedIcon;

    public interface OnIconClickListener {
        void onIconClick(IconItem iconItem);
    }

    public IconAdapter(List<IconItem> iconList, OnIconClickListener listener) {
        this.iconList = iconList;
        this.onIconClickListener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_icon_chooser, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        IconItem iconItem = iconList.get(position);
        holder.bind(iconItem);
    }

    @Override
    public int getItemCount() {
        return iconList.size();
    }

    public void updateIcons(List<IconItem> newIcons) {
        this.iconList = newIcons;
        notifyDataSetChanged();
    }

    public void setSelectedIcon(IconItem icon) {
        this.selectedIcon = icon;
        notifyDataSetChanged();
    }

    class IconViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconImageView;
        private ImageView selectionIndicator;
        private TextView iconNameTextView;

        public IconViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.img_icon);
            selectionIndicator = itemView.findViewById(R.id.img_selection_indicator);
            iconNameTextView = itemView.findViewById(R.id.txt_icon_name);
        }

        public void bind(IconItem iconItem) {
            iconImageView.setImageResource(iconItem.iconResId);

            // Set icon name
            if (iconNameTextView != null && iconItem.iconName != null) {
                iconNameTextView.setText(iconItem.iconName);
            }

            // Show selection state
            boolean isSelected = selectedIcon != null &&
                selectedIcon.iconResId == iconItem.iconResId;
            selectionIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (onIconClickListener != null) {
                    onIconClickListener.onIconClick(iconItem);
                }
            });
        }
    }
}
