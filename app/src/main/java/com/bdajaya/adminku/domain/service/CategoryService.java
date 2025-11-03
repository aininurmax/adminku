package com.bdajaya.adminku.domain.service;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.data.repository.CategoryRepository;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Concrete service class for managing category operations.
 * This replaces the old CategoryService interface.
 * Handles business rules, validation, and delegates persistence to CategoryRepository.
 *
 * @author Dimas
 * @version 2.0.0
 */
@Singleton
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Inject
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // ================================
    // CATEGORY CREATION
    // ================================

    public Result<String> createCategory(String name, String parentId) {
        try {
            // Basic validation
            if (name == null || name.trim().isEmpty()) {
                return Result.failure("Nama kategori tidak boleh kosong");
            }

            if (!Constants.isValidCategoryName(name)) {
                return Result.failure("Nama kategori tidak valid");
            }

            // Check depth limit
            Result<Boolean> maxDepthCheck = categoryRepository.isMaxDepthReached(parentId);
            if (maxDepthCheck.isFailure()) {
                return Result.failure(maxDepthCheck.getErrorMessage());
            }
            if (Boolean.TRUE.equals(maxDepthCheck.getData())) {
                return Result.failure(Constants.ERROR_MAX_DEPTH_REACHED);
            }

            // Delegate to repository
            return categoryRepository.addCategory(parentId, name);

        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Create category");
        }
    }

    // ================================
    // CATEGORY UPDATE
    // ================================

    public Result<Void> updateCategory(String categoryId, String newName) {
        try {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                return Result.failure("ID kategori tidak boleh kosong");
            }
            if (newName == null || newName.trim().isEmpty()) {
                return Result.failure("Nama kategori tidak boleh kosong");
            }

            return categoryRepository.updateCategory(categoryId, newName);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Update category");
        }
    }

    // ================================
    // CATEGORY DELETION
    // ================================

    public Result<Void> deleteCategory(String categoryId, ProductDao productDao) {
        try {
            if (categoryId == null || categoryId.trim().isEmpty()) {
                return Result.failure("ID kategori tidak boleh kosong");
            }

            String result = categoryRepository.deleteCategory(categoryId, productDao);

            switch (result) {
                case "SUCCESS":
                    return Result.success();
                case "HAS_CHILDREN":
                    return Result.failure(Constants.ERROR_CATEGORY_HAS_CHILDREN);
                case "HAS_PRODUCTS":
                    return Result.failure(Constants.ERROR_CATEGORY_HAS_PRODUCTS);
                case "NOT_FOUND":
                    return Result.failure(Constants.ERROR_CATEGORY_NOT_FOUND);
                default:
                    return Result.failure("Gagal menghapus kategori: " + result);
            }

        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Delete category");
        }
    }

    // ================================
    // CATEGORY QUERIES
    // ================================

    public Result<List<Category>> getRootCategories() {
        try {
            List<Category> categories = categoryRepository.getRootCategoriesSync();
            return Result.success(categories);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get root categories");
        }
    }

    public Result<List<Category>> getChildCategories(String parentId) {
        try {
            List<Category> children = categoryRepository.getChildCategoriesSync(parentId);
            return Result.success(children);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get child categories");
        }
    }

    public Result<Category> getCategoryById(String categoryId) {
        try {
            Category category = categoryRepository.getCategoryByIdSync(categoryId);
            if (category == null) {
                return Result.failure(Constants.ERROR_CATEGORY_NOT_FOUND);
            }
            return Result.success(category);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get category by id");
        }
    }

    public Result<List<CategoryWithPath>> searchCategories(String query) {
        try {
            if (query == null || query.trim().isEmpty()) {
                return Result.failure("Query pencarian tidak boleh kosong");
            }

            List<CategoryWithPath> results =
                    categoryRepository.searchCategoriesWithPath(query, Constants.CATEGORY_SEARCH_LIMIT);
            return Result.success(results);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Search categories");
        }
    }

    // ================================
    // LIVE DATA ACCESSORS
    // ================================

    public LiveData<List<Category>> getRootCategoriesLive() {
        return categoryRepository.getRootCategories();
    }

    public LiveData<List<Category>> getChildCategoriesLive(String parentId) {
        return categoryRepository.getChildCategories(parentId);
    }

    public LiveData<Category> getCategoryByIdLive(String categoryId) {
        return categoryRepository.getCategoryById(categoryId);
    }
}
