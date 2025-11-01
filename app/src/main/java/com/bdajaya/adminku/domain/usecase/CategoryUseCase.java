package com.bdajaya.adminku.domain.usecase;

import android.content.Context;
import androidx.annotation.Nullable;

import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.dao.CategoryDao;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.data.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Use case class for category-related business logic.
 * This class extracts business logic from Activities and ViewModels, implementing
 * the Single Responsibility Principle and improving testability.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public class CategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final Context context;

    /**
     * Creates a new CategoryUseCase instance.
     *
     * @param categoryRepository The category repository
     * @param context The Android context for user-friendly messages
     */
    public CategoryUseCase(CategoryRepository categoryRepository, Context context) {
        this.categoryRepository = categoryRepository;
        this.context = context;
    }

    // ================================
    // CATEGORY CREATION
    // ================================

    /**
     * Creates a new category with validation and business rules.
     *
     * @param name The category name
     * @param parentId The parent category ID (null for root categories)
     * @return A Result containing the new category ID or error
     */
    public Result<String> createCategory(String name, @Nullable String parentId) {
        // Validate input
        ValidationUtils.ValidationResult validation = ValidationUtils.validateCategoryData(
            name, parentId, getParentLevel(parentId), null);

        if (validation.isFailure()) {
            String errorCode = ErrorHandler.getCategoryOperationErrorCode(validation.getErrorMessage());
            return Result.failure(validation.getErrorMessage(), errorCode);
        }

        // Sanitize input
        String sanitizedName = ValidationUtils.sanitizeCategoryName(name);
        String sanitizedParentId = parentId; // parentId is already validated

        try {
            // Check if name already exists at this level
            if (isNameExistsAtLevel(sanitizedName, sanitizedParentId)) {
                String errorMessage = Constants.ERROR_DUPLICATE_CATEGORY;
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(errorMessage);
                return Result.failure(errorMessage, errorCode);
            }

            // Calculate level
            int level = calculateCategoryLevel(sanitizedParentId);

            // Validate level doesn't exceed maximum
            if (level > Constants.MAX_CATEGORY_LEVEL) {
                String errorMessage = Constants.ERROR_MAX_DEPTH_REACHED;
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(errorMessage);
                return Result.failure(errorMessage, errorCode);
            }

            // Create category
            return executeCreateCategory(sanitizedName, sanitizedParentId, level);

        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Category creation");
        }
    }

    /**
     * Updates an existing category with validation.
     *
     * @param categoryId The category ID to update
     * @param newName The new category name
     * @return A Result indicating success or failure
     */
    public Result<Void> updateCategory(String categoryId, String newName) {
        // Validate input
        ValidationUtils.ValidationResult idValidation = ValidationUtils.validateNotEmpty(categoryId, "Category ID");
        if (idValidation.isFailure()) {
            return Result.failure(idValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult nameValidation = ValidationUtils.validateCategoryName(newName);
        if (nameValidation.isFailure()) {
            return Result.failure(nameValidation.getErrorMessage());
        }

        // Sanitize input
        String sanitizedName = ValidationUtils.sanitizeCategoryName(newName);

        try {
            // Check if new name already exists at this level
            Category existingCategory = categoryRepository.getCategoryByIdSync(categoryId);
            if (existingCategory == null) {
                return Result.failure(Constants.ERROR_CATEGORY_NOT_FOUND);
            }

            if (isNameExistsAtLevel(sanitizedName, existingCategory.getParentId()) &&
                !sanitizedName.equals(existingCategory.getName())) {
                String errorMessage = Constants.ERROR_DUPLICATE_CATEGORY;
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(errorMessage);
                return Result.failure(errorMessage, errorCode);
            }

            // Update category
            return executeUpdateCategory(categoryId, sanitizedName);

        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Category update");
        }
    }

    // ================================
    // CATEGORY DELETION
    // ================================

    /**
     * Deletes a category with business rule validation.
     *
     * @param categoryId The category ID to delete
     * @param productDao The product DAO for checking related products
     * @return A Result indicating success or failure
     */
    public Result<Void> deleteCategory(String categoryId, ProductDao productDao) {
        // Validate input
        ValidationUtils.ValidationResult validation = ValidationUtils.validateNotEmpty(categoryId, "Category ID");
        if (validation.isFailure()) {
            return Result.failure(validation.getErrorMessage());
        }

        try {
            // Check if category exists
            Category category = categoryRepository.getCategoryByIdSync(categoryId);
            if (category == null) {
                return Result.failure(Constants.ERROR_CATEGORY_NOT_FOUND);
            }

            // Check business rules
            return validateAndExecuteDeletion(categoryId, category, productDao);

        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Category deletion");
        }
    }

    // ================================
    // CATEGORY QUERIES
    // ================================

    /**
     * Gets root categories with error handling.
     *
     * @return A Result containing the list of root categories or error
     */
    public Result<List<Category>> getRootCategories() {
        try {
            return executeReadOperation(
                () -> categoryRepository.getRootCategoriesSync(),
                "Get root categories"
            );
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get root categories");
        }
    }

    /**
     * Gets child categories for a parent category.
     *
     * @param parentId The parent category ID
     * @return A Result containing the list of child categories or error
     */
    public Result<List<Category>> getChildCategories(String parentId) {
        ValidationUtils.ValidationResult validation = ValidationUtils.validateNotEmpty(parentId, "Parent ID");
        if (validation.isFailure()) {
            return Result.failure(validation.getErrorMessage());
        }

        try {
            return executeReadOperation(
                () -> categoryRepository.getChildCategoriesSync(parentId),
                "Get child categories for parent: " + parentId
            );
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get child categories");
        }
    }

    /**
     * Searches categories with path information.
     *
     * @param query The search query
     * @param limit The maximum number of results
     * @return A Result containing the search results or error
     */
    public Result<List<CategoryWithPath>> searchCategories(String query, int limit) {
        ValidationUtils.ValidationResult validation = ValidationUtils.validateNotEmpty(query, "Search query");
        if (validation.isFailure()) {
            return Result.failure(validation.getErrorMessage());
        }

        try {
            return executeReadOperation(
                () -> categoryRepository.searchCategoriesWithPath(query, limit),
                "Search categories: " + query
            );
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Search categories");
        }
    }

    // ================================
    // CATEGORY VALIDATION
    // ================================

    /**
     * Validates if a category can have subcategories.
     *
     * @param category The category to check
     * @return A Result indicating whether subcategories can be added
     */
    public Result<Boolean> canAddSubcategory(Category category) {
        if (category == null) {
            return Result.success(true); // Root level can always have categories
        }

        try {
            boolean canAdd = category.getLevel() < Constants.MAX_CATEGORY_LEVEL;
            return Result.success(canAdd);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Check subcategory permission");
        }
    }

    /**
     * Validates if a category is selectable (has no children).
     *
     * @param category The category to check
     * @return A Result indicating whether the category is selectable
     */
    public Result<Boolean> isCategorySelectable(Category category) {
        if (category == null) {
            return Result.success(false);
        }

        try {
            boolean isSelectable = !category.hasChildren();
            return Result.success(isSelectable);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Check category selectability");
        }
    }

    /**
     * Gets the path to root for a category.
     *
     * @param categoryId The category ID
     * @return A Result containing the path to root or error
     */
    public Result<List<Category>> getCategoryPathToRoot(String categoryId) {
        ValidationUtils.ValidationResult validation = ValidationUtils.validateNotEmpty(categoryId, "Category ID");
        if (validation.isFailure()) {
            return Result.failure(validation.getErrorMessage());
        }

        try {
            return executeReadOperation(
                () -> categoryRepository.pathToRoot(categoryId),
                "Get path to root for category: " + categoryId
            );
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get category path to root");
        }
    }

    // ================================
    // HELPER METHODS
    // ================================

    /**
     * Gets the level of a parent category.
     *
     * @param parentId The parent category ID
     * @return The parent category level, or 0 for root
     */
    private int getParentLevel(@Nullable String parentId) {
        if (parentId == null) {
            return Constants.ROOT_CATEGORY_LEVEL;
        }

        try {
            Category parent = categoryRepository.getCategoryByIdSync(parentId);
            return parent != null ? parent.getLevel() : Constants.ROOT_CATEGORY_LEVEL;
        } catch (Exception e) {
            ErrorHandler.logWarning("Failed to get parent level for ID: " + parentId, e.getMessage());
            return Constants.ROOT_CATEGORY_LEVEL;
        }
    }

    /**
     * Calculates the level for a new category.
     *
     * @param parentId The parent category ID
     * @return The calculated level
     */
    private int calculateCategoryLevel(@Nullable String parentId) {
        return getParentLevel(parentId) + 1;
    }

    /**
     * Checks if a category name already exists at the same level.
     *
     * @param name The category name
     * @param parentId The parent category ID
     * @return true if the name exists, false otherwise
     */
    private boolean isNameExistsAtLevel(String name, @Nullable String parentId) {
        try {
            return categoryRepository.getChildCategoriesSync(parentId)
                    .stream()
                    .anyMatch(category -> name.equals(category.getName()));
        } catch (Exception e) {
            ErrorHandler.logWarning("Failed to check name existence for: " + name, e.getMessage());
            return false;
        }
    }

    /**
     * Executes category creation with proper error handling.
     *
     * @param name The category name
     * @param parentId The parent category ID
     * @param level The category level
     * @return A Result containing the new category ID or error
     */
    private Result<String> executeCreateCategory(String name, @Nullable String parentId, int level) {
        return executeWriteOperation(
            () -> {
                String id = UUID.randomUUID().toString();
                long now = System.currentTimeMillis();

                Category category = new Category(
                    id,
                    parentId,
                    level,
                    name,
                    null,
                    false,
                    now,
                    now
                );

                // Insert category
                CategoryDao categoryDao = getCategoryDao();
                categoryDao.insert(category);

                // Update parent's hasChildren flag if needed
                if (parentId != null) {
                    updateParentHasChildren(parentId, true);
                }

                return id;
            },
            "Create category: " + name
        );
    }

    /**
     * Executes category update with proper error handling.
     *
     * @param categoryId The category ID
     * @param name The new name
     * @return A Result indicating success or failure
     */
    private Result<Void> executeUpdateCategory(String categoryId, String name) {
        return executeWriteOperation(
            () -> {
                CategoryDao categoryDao = getCategoryDao();
                Category category = categoryDao.getById(categoryId);

                if (category != null) {
                    category.setName(name);
                    category.setUpdatedAt(System.currentTimeMillis());
                    categoryDao.update(category);
                }

                return null;
            },
            "Update category: " + categoryId
        );
    }

    /**
     * Validates deletion rules and executes deletion.
     *
     * @param categoryId The category ID
     * @param category The category to delete
     * @param productDao The product DAO
     * @return A Result indicating success or failure
     */
    private Result<Void> validateAndExecuteDeletion(String categoryId, Category category, ProductDao productDao) {
        return executeWriteOperation(
            () -> {
                // Check if category has children
                if (categoryRepository.getChildCategoriesSync(categoryId).size() > 0) {
                    throw new ErrorHandler.BusinessLogicException(Constants.ERROR_CATEGORY_HAS_CHILDREN);
                }

                // Update products to remove category reference
                if (productDao.countByCategoryId(categoryId) > 0) {
                    List<com.bdajaya.adminku.data.entity.Product> products = productDao.getByCategoryId(categoryId);
                    for (com.bdajaya.adminku.data.entity.Product product : products) {
                        product.setCategoryId(null);
                        product.setUpdatedAt(System.currentTimeMillis());
                        productDao.update(product);
                    }
                }

                // Delete category
                CategoryDao categoryDao = getCategoryDao();
                categoryDao.delete(category);

                // Update parent's hasChildren flag if needed
                String parentId = category.getParentId();
                if (parentId != null) {
                    updateParentHasChildren(parentId, false);
                }

                return null;
            },
            "Delete category: " + categoryId
        );
    }

    /**
     * Updates the hasChildren flag for a parent category.
     *
     * @param parentId The parent category ID
     * @param hasChildren Whether the parent has children
     */
    private void updateParentHasChildren(String parentId, boolean hasChildren) {
        try {
            Category parent = categoryRepository.getCategoryByIdSync(parentId);
            if (parent != null) {
                parent.setHasChildren(hasChildren);
                parent.setUpdatedAt(System.currentTimeMillis());
                CategoryDao categoryDao = getCategoryDao();
                categoryDao.update(parent);
            }
        } catch (Exception e) {
            ErrorHandler.logWarning("Failed to update parent hasChildren flag for: " + parentId, e.getMessage());
        }
    }

    /**
     * Gets the category DAO instance.
     *
     * @return The category DAO
     */
    private CategoryDao getCategoryDao() {
        // This would need to be injected or obtained from a repository
        // For now, we'll use a simplified approach
        return com.bdajaya.adminku.data.AppDatabase.getInstance(context).categoryDao();
    }

    /**
     * Executes a read operation with proper error handling.
     *
     * @param operation The operation to execute
     * @param operationName The name of the operation for logging
     * @param <T> The return type
     * @return A Result containing the operation result or error
     */
    private <T> Result<T> executeReadOperation(Callable<T> operation, String operationName) {
        try {
            Future<T> future = com.bdajaya.adminku.data.AppDatabase.databaseWriteExecutor.submit(operation);
            T result = future.get();
            return Result.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ErrorHandler.handleDatabaseException(e, operationName);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                return ErrorHandler.handleDatabaseException((Exception) cause, operationName);
            } else {
                return ErrorHandler.handleException(new Exception(cause), operationName);
            }
        } catch (Exception e) {
            return ErrorHandler.handleException(e, operationName);
        }
    }

    /**
     * Executes a write operation with proper error handling.
     *
     * @param operation The operation to execute
     * @param operationName The name of the operation for logging
     * @param <T> The return type
     * @return A Result containing the operation result or error
     */
    private <T> Result<T> executeWriteOperation(Callable<T> operation, String operationName) {
        try {
            Future<T> future = com.bdajaya.adminku.data.AppDatabase.databaseWriteExecutor.submit(operation);
            T result = future.get();
            return Result.success(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ErrorHandler.handleDatabaseException(e, operationName);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                return ErrorHandler.handleDatabaseException((Exception) cause, operationName);
            } else {
                return ErrorHandler.handleException(new Exception(cause), operationName);
            }
        } catch (Exception e) {
            return ErrorHandler.handleException(e, operationName);
        }
    }
}
