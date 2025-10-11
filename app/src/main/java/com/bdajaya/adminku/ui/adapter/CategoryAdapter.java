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
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.repository.CategoryRepository;

import java.util.List;



public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private final CategoryClickListener listener;
    private final int maxDepth;

    public CategoryAdapter(List<Category> categories, CategoryClickListener listener, int maxDepth) {
        this.categories = categories;
        this.listener = listener;
        this.maxDepth = maxDepth;
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
        void onCategoryLongClick(Category category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final ImageView chevronImageView;
        private final Button selectButton;
        private final Button addSubcategoryButton;
        private final CategoryRepository categoryRepository;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.category_name_text_view);
            chevronImageView = itemView.findViewById(R.id.chevron_image_view);
            selectButton = itemView.findViewById(R.id.select_button);
            addSubcategoryButton = itemView.findViewById(R.id.add_subcategory_button);

            // Get CategoryRepository from context
            AppDatabase database = AppDatabase.getInstance(itemView.getContext());
            categoryRepository = new CategoryRepository(database.categoryDao(), database.configDao());
        }

        void bind(Category category) {
            nameTextView.setText(category.getName());

            // Periksa status children dari database
            boolean hasChildren = checkCategoryChildren(category);
            // Update visibility berdasarkan level dan status children
            updateButtonVisibility(category, hasChildren);

            setupClickListeners(category, hasChildren);
        }

        private void updateButtonVisibility(Category category, boolean hasChildren) {
            chevronImageView.setVisibility(hasChildren ? View.VISIBLE : View.GONE);

            // Tampilkan tombol select hanya jika tidak memiliki children
            selectButton.setVisibility(hasChildren ? View.GONE : View.VISIBLE);

            // Tampilkan tombol add subcategory berdasarkan level
            boolean canAddSubcategory = category.getLevel() < maxDepth;
            addSubcategoryButton.setVisibility(canAddSubcategory ? View.VISIBLE : View.GONE);
        }

        private void setupClickListeners(Category category, boolean hasChildren) {
            itemView.setOnClickListener(v -> {
                listener.onCategoryClick(category, hasChildren);
            });

            itemView.setOnLongClickListener(v -> {
                listener.onCategoryLongClick(category);
                return true;
            });

            selectButton.setOnClickListener(v -> {
                listener.onCategoryClick(category, false);
            });

            addSubcategoryButton.setOnClickListener(v -> {
                if (category.getLevel() < maxDepth) {
                    listener.onAddSubcategoryClick(category);
                }
            });
        }

        private boolean checkCategoryChildren(Category category) {
            try {
                // Use CategoryRepository to check if category has children
                return categoryRepository.getChildCategoriesSync(category.getId()).size() > 0;
            } catch (Exception e) {
                // Fallback to simple level check if database query fails
                return category.getLevel() < maxDepth - 1;
            }
        }
    }
}
