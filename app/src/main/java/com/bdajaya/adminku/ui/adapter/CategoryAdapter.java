package com.bdajaya.adminku.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bdajaya.adminku.R;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying categories with improved error handling and null safety.
 * This adapter handles category display, click events, and provides better user experience.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private final CategoryClickListener listener;

    /**
     * Creates a new CategoryAdapter with the given categories and click listener.
     *
     * @param categories The list of categories to display (can be null)
     * @param listener The click listener for category interactions
     */
    public CategoryAdapter(List<Category> categories, CategoryClickListener listener) {
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category, parent, false);
            return new CategoryViewHolder(view);
        } catch (Exception e) {
            ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Failed to create CategoryViewHolder", e);
            // Fallback to a simple view if inflation fails
            View fallbackView = new View(parent.getContext());
            return new CategoryViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        if (position < 0 || position >= categories.size()) {
            ErrorHandler.logWarning("Invalid position in onBindViewHolder: " + position, null);
            return;
        }

        Category category = categories.get(position);
        if (category != null) {
            holder.bind(category);
        } else {
            ErrorHandler.logWarning("Null category at position: " + position, null);
            holder.bindEmpty();
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    /**
     * Updates the adapter data with null safety.
     *
     * @param newCategories The new list of categories (can be null)
     */
    public void updateData(List<Category> newCategories) {
        this.categories = newCategories != null ? new ArrayList<>(newCategories) : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Safely adds a category to the list.
     *
     * @param category The category to add
     */
    public void addCategory(Category category) {
        if (category != null) {
            if (this.categories == null) {
                this.categories = new ArrayList<>();
            }
            this.categories.add(category);
            notifyItemInserted(this.categories.size() - 1);
        }
    }

    /**
     * Safely removes a category from the list.
     *
     * @param position The position to remove
     */
    public void removeCategory(int position) {
        if (this.categories != null && position >= 0 && position < this.categories.size()) {
            this.categories.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Gets a category at the specified position safely.
     *
     * @param position The position to get
     * @return The category or null if not found
     */
    public Category getCategory(int position) {
        if (categories != null && position >= 0 && position < categories.size()) {
            return categories.get(position);
        }
        return null;
    }

    public interface CategoryClickListener {
        void onCategoryClick(Category category, boolean hasChildren);
        void onAddSubcategoryClick(Category category);
        void onCategoryLongClick(Category category);
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final ImageView chevronImageView;
        private CategoryRepository categoryRepository;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            // Safely find views with null checks
            nameTextView = itemView.findViewById(R.id.category_name_text_view);
            chevronImageView = itemView.findViewById(R.id.chevron_image_view);

            // Initialize to null first
            categoryRepository = null;

            try {
                // Get CategoryRepository from context
                AppDatabase database = AppDatabase.getInstance(itemView.getContext());
                categoryRepository = new CategoryRepository(database.categoryDao());
            } catch (Exception e) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_DATABASE, "Failed to initialize CategoryRepository in ViewHolder", e);
                // categoryRepository already set to null above
            }
        }

        /**
         * Binds category data to the view with comprehensive error handling.
         *
         * @param category The category to bind
         */
        void bind(Category category) {
            if (category == null) {
                bindEmpty();
                return;
            }

            try {
                // Safely set category name
                if (nameTextView != null) {
                    String categoryName = category.getName();
                    nameTextView.setText(categoryName != null ? categoryName : "Unnamed Category");
                }

                // Determine category properties with null safety
                boolean hasChildren = category.hasChildren();
                boolean isSelectable = !hasChildren;

                // Update UI based on category state
                updateButtonVisibility(hasChildren, isSelectable);
                setupClickListeners(category, hasChildren, isSelectable);

            } catch (Exception e) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Failed to bind category: " + category.getId(), e);
                bindEmpty();
            }
        }

        /**
         * Binds an empty state when category data is unavailable.
         */
        void bindEmpty() {
            if (nameTextView != null) {
                nameTextView.setText("Loading...");
            }
            if (chevronImageView != null) {
                chevronImageView.setVisibility(View.GONE);
            }
        }

        /**
         * Updates button visibility based on category state.
         *
         * @param hasChildren Whether the category has children
         * @param isSelectable Whether the category is selectable
         */
        private void updateButtonVisibility(boolean hasChildren, boolean isSelectable) {
            if (chevronImageView != null) {
                // Show chevron only if category has children
                chevronImageView.setVisibility(hasChildren ? View.VISIBLE : View.GONE);
            }
        }

        /**
         * Sets up click listeners for the category item.
         *
         * @param category The category
         * @param hasChildren Whether the category has children
         * @param isSelectable Whether the category is selectable
         */
        private void setupClickListeners(Category category, boolean hasChildren, boolean isSelectable) {
            if (listener == null || category == null) {
                return;
            }

            // Click listener: if has children, open; if not, select (if selectable)
            itemView.setOnClickListener(v -> {
                try {
                    if (hasChildren) {
                        listener.onCategoryClick(category, true);
                    } else if (isSelectable) {
                        listener.onCategoryClick(category, false);
                    }
                } catch (Exception e) {
                    ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error in category click listener", e);
                }
            });

            // Long click listener for showing bottom sheet options
            itemView.setOnLongClickListener(v -> {
                try {
                    listener.onCategoryLongClick(category);
                    return true;
                } catch (Exception e) {
                    ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN, "Error in category long click listener", e);
                    return false;
                }
            });
        }

        /**
         * Checks if a category has children with error handling.
         *
         * @param category The category to check
         * @return true if the category has children
         */
        private boolean checkCategoryChildren(Category category) {
            if (categoryRepository == null || category == null) {
                return false;
            }

            try {
                List<Category> children = categoryRepository.getChildCategoriesSync(category.getId());
                return children != null && !children.isEmpty();
            } catch (Exception e) {
                ErrorHandler.logWarning("Failed to check category children for: " + category.getId(), e.getMessage());
                return false;
            }
        }
    }
}
