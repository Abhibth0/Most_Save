package com.example.mostsave.ui.fragments;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mostsave.R;

import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder> {
    private final List<Uri> imageUris;
    private final OnRemoveClickListener removeClickListener;

    public interface OnRemoveClickListener {
        void onRemoveClick(Uri uri);
    }

    public ImagePreviewAdapter(List<Uri> imageUris, OnRemoveClickListener removeClickListener) {
        this.imageUris = imageUris;
        this.removeClickListener = removeClickListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        holder.imageView.setImageURI(uri);
        holder.removeIcon.setVisibility(View.VISIBLE);
        holder.removeIcon.setOnClickListener(v -> removeClickListener.onRemoveClick(uri));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView removeIcon;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivImagePreview);
            removeIcon = itemView.findViewById(R.id.ivRemoveImage);
        }
    }
}

