package com.bdajaya.adminku.domain.service;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.CategoryWithPath;

import java.util.List;

/**
 * Service interface for category operations.
 * Defines the contract for category-related business operations and promotes
 * the Interface Segregation and Dependency Inversion principles.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public interface CategoryService {

    // ================================
    // CATEGORY CRUD OPERATIONS
    // ================================

    /**
     * Creates a new category.
     *
     * @param name The category name
     * @param parentId The parent category ID (null for root categories)
     * @return A Result containing the new category ID or error
     */
    Result<String> createCategory(String name, String parentId);

    /**
     * Updates an existing category.
     *
     * @param categoryId The category ID to update
     * @param newName The new category name
     * @return A Result indicating success or failure
     */
    Result<Void> updateCategory(String categoryId, String newName);

    /**
     * Deletes a category.
     *
     * @param categoryId The category ID to delete
     * @param productDao The product DAO for handling related products
     * @return A Result indicating success or failure
     */
    Result<Void> deleteCategory(String categoryId, ProductDao productDao);

    // ================================
    // CATEGORY QUERIES
    // ================================

    /**
     * Gets all root categories.
     *
     * @return A Result containing the list of root categories or error
     */
    Result<List<Category>> getRootCategories();

    /**
     * Gets child categories for a specific parent.
     *
     * @param parentId The parent category ID
     * @return A Result containing the list of child categories or error
     */
    Result<List<Category>> getChildCategories(String parentId);

    /**
     * Gets a category by its ID.
     *
     * @param categoryId The category ID
     * @return A Result containing the category or error
     */
    Result<Category> getCategoryById(String categoryId);

    /**
     * Searches categories by name or other criteria.
     *
     * @param query The search query
     * @param limit The maximum number of results
     * @return A Result containing the search results or error
     */
    Result<List<CategoryWithPath>> searchCategories(String query, int limit);

    /**
     * Gets the path from a category to its root.
     *
     * @param categoryId The category ID
     * @return A Result containing the path to root or error
     */
    Result<List<Category>> getCategoryPathToRoot(String categoryId);

    // ================================
    // CATEGORY VALIDATION
    // ================================

    /**
     * Checks if a category can have subcategories.
     *
     * @param category The category to check
     * @return A Result indicating whether subcategories can be added
     */
    Result<Boolean> canAddSubcategory(Category category);

    /**
     * Checks if a category is selectable (has no children).
     *
     * @param category The category to check
     * @return A Result indicating whether the category is selectable
     */
    Result<Boolean> isCategorySelectable(Category category);

    /**
     * Checks if adding a child to a parent would exceed maximum depth.
     *
     * @param parentId The parent category ID
     * @return A Result indicating whether max depth would be reached
     */
    Result<Boolean> isMaxDepthReached(String parentId);

    // ================================
    // ASYNC OPERATIONS
    // ================================

    /**
     * Asynchronously gets child categories for validation purposes.
     *
     * @param parentId The parent category ID
     * @param callback The callback to handle the result
     */
    void getChildCategoriesAsync(String parentId, CategoryChildrenCallback callback);

    /**
     * Gets the number of products in a category.
     *
     * @param categoryId The category ID
     * @return The number of products in the category
     */
    int countProductsByCategoryId(String categoryId);

    // ================================
    // LIVE DATA OBSERVABLES
    // ================================

    /**
     * Gets a LiveData of root categories for reactive UI updates.
     *
     * @return LiveData containing the list of root categories
     */
    LiveData<List<Category>> getRootCategoriesLiveData();

    /**
     * Gets a LiveData of child categories for a specific parent.
     *
     * @param parentId The parent category ID
     * @return LiveData containing the list of child categories
     */
    LiveData<List<Category>> getChildCategoriesLiveData(String parentId);

    /**
     * Gets a LiveData of a specific category for reactive UI updates.
     *
     * @param categoryId The category ID
     * @return LiveData containing the category
     */
    LiveData<Category> getCategoryByIdLiveData(String categoryId);

    // ================================
    // CALLBACK INTERFACES
    // ================================

    /**
     * Callback interface for asynchronous category operations.
     */
    interface CategoryChildrenCallback {
        /**
         * Called when child categories are loaded.
         *
         * @param children The list of child categories
         */
        void onChildrenLoaded(List<Category> children);

        /**
         * Called when an error occurs during the operation.
         *
         * @param error The error that occurred
         */
        void onError(Exception error);
    }

    // ================================
    // BATCH OPERATIONS
    // ================================

    /**
     * Validates a complete category data set.
     *
     * @param name The category name
     * @param parentId The parent category ID
     * @param iconUrl The icon URL (optional)
     * @return A Result indicating validation success or failure
     */
    Result<Void> validateCategoryData(String name, String parentId, String iconUrl);

    /**
     * Gets category statistics.
     *
     * @return A Result containing category statistics or error
     */
    Result<CategoryStatistics> getCategoryStatistics();

    // ================================
    // STATISTICS DATA CLASS
    // ================================

    /**
     * Data class for category statistics.
     */
    class CategoryStatistics {
        private final int totalCategories;
        private final int rootCategories;
        private final int maxDepth;
        private final int averageChildrenPerCategory;

        public CategoryStatistics(int totalCategories, int rootCategories, int maxDepth, int averageChildrenPerCategory) {
            this.totalCategories = totalCategories;
            this.rootCategories = rootCategories;
            this.maxDepth = maxDepth;
            this.averageChildrenPerCategory = averageChildrenPerCategory;
        }

        public int getTotalCategories() {
            return totalCategories;
        }

        public int getRootCategories() {
            return rootCategories;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public int getAverageChildrenPerCategory() {
            return averageChildrenPerCategory;
        }

        @Override
        public String toString() {
            return "CategoryStatistics{" +
                    "totalCategories=" + totalCategories +
                    ", rootCategories=" + rootCategories +
                    ", maxDepth=" + maxDepth +
                    ", averageChildrenPerCategory=" + averageChildrenPerCategory +
                    '}';
        }
    }
}
