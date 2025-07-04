package com.example.mostsave.ui.adapters;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mostsave.R;
import com.example.mostsave.data.model.Category;

import java.util.Collections;
import java.util.List;

public class CategoryAdapter extends ListAdapter<Category, CategoryAdapter.CategoryViewHolder> {

    private final OnCategoryRenameListener renameListener;
    private final OnCategoryDeleteListener deleteListener;
    private final OnStartDragListener dragListener;

    public interface OnCategoryRenameListener {
        void onRenameCategory(Category category);
    }

    public interface OnCategoryDeleteListener {
        void onDeleteCategory(Category category);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public CategoryAdapter(OnCategoryRenameListener renameListener, OnCategoryDeleteListener deleteListener, OnStartDragListener dragListener) {
        super(DIFF_CALLBACK);
        this.renameListener = renameListener;
        this.deleteListener = deleteListener;
        this.dragListener = dragListener;
    }

    private static final DiffUtil.ItemCallback<Category> DIFF_CALLBACK = new DiffUtil.ItemCallback<Category>() {
        @Override
        public boolean areItemsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Category oldItem, @NonNull Category newItem) {
            return oldItem.getName().equals(newItem.getName()) && oldItem.getDisplayOrder() == newItem.getDisplayOrder();
        }
    };

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category currentCategory = getItem(position);
        holder.bind(currentCategory, renameListener, deleteListener);
        holder.dragHandle.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                dragListener.onStartDrag(holder);
            }
            return false;
        });
    }

    public void onItemMove(int fromPosition, int toPosition) {
        List<Category> currentList = new java.util.ArrayList<>(getCurrentList());
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(currentList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(currentList, i, i - 1);
            }
        }
        submitList(currentList);
    }


    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewCategoryName;
        private final ImageButton buttonRenameCategory;
        private final ImageButton buttonDeleteCategory;
        private final ImageView dragHandle;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCategoryName = itemView.findViewById(R.id.text_view_category_name);
            buttonRenameCategory = itemView.findViewById(R.id.button_rename_category);
            buttonDeleteCategory = itemView.findViewById(R.id.button_delete_category);
            dragHandle = itemView.findViewById(R.id.drag_handle);
        }

        public void bind(final Category category, final OnCategoryRenameListener renameListener, final OnCategoryDeleteListener deleteListener) {
            textViewCategoryName.setText(category.getName());

            buttonRenameCategory.setOnClickListener(v -> {
                if (renameListener != null) {
                    renameListener.onRenameCategory(category);
                }
            });

            buttonDeleteCategory.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteCategory(category);
                }
            });
        }
    }
}
