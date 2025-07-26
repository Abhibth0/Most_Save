package com.example.mostsave.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mostsave.R;

import java.util.List;

public class PasswordIconAdapter extends RecyclerView.Adapter<PasswordIconAdapter.IconViewHolder> {

    private final List<Integer> iconResIds;
    private final OnIconClickListener listener;

    public interface OnIconClickListener {
        void onIconClicked(int iconResId);
    }

    public PasswordIconAdapter(List<Integer> iconResIds, OnIconClickListener listener) {
        this.iconResIds = iconResIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon, parent, false);
        return new IconViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, int position) {
        int iconResId = iconResIds.get(position);
        holder.imageView.setImageResource(iconResId);

        if (iconResId == R.drawable.ic_add) {
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.width = (int) (params.width * 1.5);
            params.height = (int) (params.height * 1.5);
            holder.imageView.setLayoutParams(params);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIconClicked(iconResId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return iconResIds.size();
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;

        IconViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_icon);
        }
    }
}
