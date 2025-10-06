package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.data.entity.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private final CategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, CategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<Category> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    public interface CategoryClickListener {
        void onCategoryClick(Category category, boolean hasChildren);
        void onAddSubcategoryClick(Category category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final ImageView chevronImageView;
        private final Button selectButton;
        private final Button addSubcategoryButton;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_name_text_view);
            chevronImageView = itemView.findViewById(R.id.chevron_image_view);
            selectButton = itemView.findViewById(R.id.select_button);
            addSubcategoryButton = itemView.findViewById(R.id.add_subcategory_button);
        }

        void bind(Category category) {
            nameTextView.setText(category.getName());

            // Check if this category has children
            boolean hasChildren = getChildCount(category) > 0;

            chevronImageView.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
            selectButton.setVisibility(hasChildren ? View.GONE : View.VISIBLE);
            addSubcategoryButton.setVisibility(View.VISIBLE);

            itemView.setOnClickListener(v -> {
                listener.onCategoryClick(category, hasChildren);
            });

            selectButton.setOnClickListener(v -> {
                listener.onCategoryClick(category, false);
            });

            addSubcategoryButton.setOnClickListener(v -> {
                listener.onAddSubcategoryClick(category);
            });
        }

        private int getChildCount(Category category) {
            // This is a placeholder. In a real app, you would query the database
            // or use a cached value to determine if a category has children.
            // For now, we'll assume categories with level < 4 might have children.
            return category.getLevel() < 4 ? 1 : 0;
        }
    }
}

