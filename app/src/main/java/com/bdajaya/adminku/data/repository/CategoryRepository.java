package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.core.BaseRepository;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.CategoryDao;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.model.CategoryWithPath;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository class for category-related database operations.
 * Extends BaseRepository to provide standardized error handling and async operations.
 * This class focuses solely on data access and delegates business logic to use cases.
 *
 * @author Adminku Development Team
 * @version 2.0.0
 */
public class CategoryRepository extends BaseRepository {
    private final CategoryDao categoryDao;

    public CategoryRepository(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    protected String getRepositoryName() {
        return "CategoryRepository";
    }

    public LiveData<List<Category>> getRootCategories() {
        return categoryDao.getRoots();
    }

    public List<Category> getRootCategoriesSync() {
        return categoryDao.getRootsSync();
    }

    public LiveData<List<Category>> getChildCategories(String parentId) {
        return categoryDao.getChildren(parentId);
    }

    public List<Category> getChildCategoriesSync(String parentId) {
        return categoryDao.getChildrenSync(parentId);
    }

    public LiveData<Category> getCategoryById(String id) {
        return categoryDao.getByIdLive(id);
    }

    public Category getCategoryByIdSync(String id) {
        return categoryDao.getById(id);
    }

    public List<Category> searchCategories(String query, int limit) {
        return categoryDao.search(query, limit);
    }

    public List<CategoryWithPath> searchCategoriesWithPath(String query, int limit) {
        List<Category> categories = categoryDao.search(query, limit);
        List<CategoryWithPath> result = new ArrayList<>();

        for (Category category : categories) {
            List<Category> path = pathToRoot(category.getId());
            result.add(new CategoryWithPath(category, path));
        }

        return result;
    }

    public List<Category> pathToRoot(String categoryId) {
        List<Category> path = new ArrayList<>();
        Category current = categoryDao.getById(categoryId);

        while (current != null) {
            path.add(current);
            if (current.getParentId() == null) {
                break;
            }
            current = categoryDao.getById(current.getParentId());
        }

        return path;
    }

    /**
     * Adds a new category with proper validation and error handling.
     * This method now delegates business logic to CategoryUseCase and focuses on data access.
     *
     * @param parentId The parent category ID (null for root categories)
     * @param name The category name
     * @return A Result containing the new category ID or error
     */
    public Result<String> addCategory(String parentId, String name) {
        // Basic validation
        ValidationUtils.ValidationResult validation = ValidationUtils.validateCategoryName(name);
        if (validation.isFailure()) {
            String errorCode = ErrorHandler.getCategoryOperationErrorCode(validation.getErrorMessage());
            return Result.failure(validation.getErrorMessage(), errorCode);
        }

        return executeWriteOperation(() -> {
            // Check if name already exists at this level
            // Modify to handle null parentId explicitly
            long duplicateCount = parentId == null 
                ? categoryDao.countRootCategoriesByName(name) 
                : categoryDao.countByParentAndName(parentId, name);
            
            if (duplicateCount > 0) {
                String errorMessage = Constants.ERROR_DUPLICATE_CATEGORY;
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(errorMessage);
                throw new ErrorHandler.ValidationException(errorMessage);
            }

            // Calculate level
            int level = calculateCategoryLevel(parentId);

            // Validate level doesn't exceed maximum
            if (level > Constants.MAX_CATEGORY_LEVEL) {
                String errorMessage = Constants.ERROR_MAX_DEPTH_REACHED;
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(errorMessage);
                throw new ErrorHandler.ValidationException(errorMessage);
            }

            // Create and insert new category
            String id = UUID.randomUUID().toString();
            long now = getCurrentTimestamp();

            Category category = new Category(
                id,
                parentId,
                level,
                name,
                null,
                false, // hasChildren initially false, will update parent if needed
                now,
                now
            );

            categoryDao.insert(category);

            // Update parent's hasChildren if this is a new child
            if (parentId != null) {
                updateParentHasChildren(parentId, true);
            }

            return id;
        }, "Add category: " + name);
    }

    /**
     * Updates an existing category with proper validation and error handling.
     *
     * @param id The category ID to update
     * @param newName The new category name
     * @return A Result indicating success or failure
     */
    public Result<Void> updateCategory(String id, String newName) {
        // Basic validation
        ValidationUtils.ValidationResult idValidation = validateId(id, "Category ID");
        if (idValidation.isFailure()) {
            return Result.failure(idValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult nameValidation = ValidationUtils.validateCategoryName(newName);
        if (nameValidation.isFailure()) {
            return Result.failure(nameValidation.getErrorMessage());
        }

        return executeWriteOperation(() -> {
            Category category = categoryDao.getById(id);
            if (category == null) {
                throw new ErrorHandler.DatabaseException(Constants.ERROR_CATEGORY_NOT_FOUND);
            }

            // Check if name already exists at this level
            if (categoryDao.countByParentAndName(category.getParentId(), newName) > 0) {
                throw new ErrorHandler.ValidationException(Constants.ERROR_DUPLICATE_CATEGORY);
            }

            category.setName(newName);
            category.setUpdatedAt(getCurrentTimestamp());
            categoryDao.update(category);

            return null;
        }, "Update category: " + id);
    }

    public String deleteCategory(String id, ProductDao productDao) {
        try {
            Future<String> future = AppDatabase.databaseWriteExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    // Check if category has children
                    if (categoryDao.countChildren(id) > 0) {
                        return "HAS_CHILDREN";
                    }

                    // Check if category has products
                    if (productDao.countByCategoryId(id) > 0) {
                        return "HAS_PRODUCTS";
                    }

                    // Get the category before deleting
                    Category category = categoryDao.getById(id);
                    if (category != null) {
                        // Delete category
                        categoryDao.delete(category);

                        // Update parent's hasChildren flag if needed
                        String parentId = category.getParentId();
                        if (parentId != null) {
                            int remainingChildren = categoryDao.countChildren(parentId);
                            updateParentHasChildren(parentId, remainingChildren > 0);
                        }

                        return "SUCCESS";
                    }

                    return "NOT_FOUND";
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public int getMaxLevel() {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return categoryDao.getMaxLevel();
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Checks if the maximum category depth would be reached by adding a child to the given parent.
     *
     * @param parentId The parent category ID
     * @return A Result indicating whether max depth would be reached
     */
    public Result<Boolean> isMaxDepthReached(String parentId) {
        if (parentId == null) {
            return Result.success(false);
        }

        return executeReadOperation(() -> {
            Category parent = categoryDao.getById(parentId);
            return parent != null && parent.getLevel() >= Constants.MAX_CATEGORY_LEVEL;
        }, "Check max depth for parent: " + parentId);
    }

    /**
     * Calculates the level for a new category based on its parent.
     *
     * @param parentId The parent category ID (null for root categories)
     * @return The calculated level
     */
    private int calculateCategoryLevel(String parentId) {
        if (parentId == null) {
            return Constants.ROOT_CATEGORY_LEVEL;
        }

        Category parent = categoryDao.getById(parentId);
        return parent != null ? parent.getLevel() + 1 : Constants.ROOT_CATEGORY_LEVEL;
    }

    // Method untuk mendapatkan children secara asynchronous (untuk cek konfirmasi)
    public void getChildCategoriesAsync(String parentId, CategoryChildrenCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Category> children = getChildCategoriesSync(parentId);
                callback.onChildrenLoaded(children);
            } catch (Exception e) {
                callback.onChildrenLoaded(new ArrayList<>());
            }
        });
    }

    public interface CategoryChildrenCallback {
        void onChildrenLoaded(List<Category> children);
    }

    // Method untuk menghitung jumlah produk dalam kategori
    public int countProductsByCategoryId(String categoryId) {
        ProductDao productDao = AppDatabase.getInstance(null).productDao();
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return productDao.countByCategoryId(categoryId);
                }
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String deleteCategoryWithProductUpdate(String id, ProductDao productDaoParam) {
        final ProductDao productDao = productDaoParam;
        try {
            Future<String> future = AppDatabase.databaseWriteExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    // Check if category has children
                    if (categoryDao.countChildren(id) > 0) {
                        return "HAS_CHILDREN";
                    }

                    // Get the category before deleting
                    Category category = categoryDao.getById(id);
                    if (category != null) {
                        // Update products: Set categoryId to null instead of deleting them
                        if (productDao.countByCategoryId(id) > 0) {
                            // Get all products in this category and set their categoryId to null
                            List<Product> products = productDao.getByCategoryId(id);
                            for (Product product : products) {
                                product.setCategoryId(null);
                                product.setUpdatedAt(System.currentTimeMillis());
                                productDao.update(product);
                            }
                        }

                        // Delete category
                        categoryDao.delete(category);

                        // Update parent's hasChildren flag if needed
                        String parentId = category.getParentId();
                        if (parentId != null) {
                            int remainingChildren = categoryDao.countChildren(parentId);
                            updateParentHasChildren(parentId, remainingChildren > 0);
                        }

                        return "SUCCESS";
                    }

                    return "NOT_FOUND";
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public void insert(Category newCategory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.insert(newCategory);
        });
    }

    /**
     * Update the hasChildren flag for a parent category
     */
    public void updateParentHasChildren(String parentId, boolean hasChildren) {
        if (parentId != null) {
            Category parent = categoryDao.getById(parentId);
            if (parent != null) {
                parent.setHasChildren(hasChildren);
                parent.setUpdatedAt(System.currentTimeMillis());
                categoryDao.update(parent);
            }
        }
    }
}
