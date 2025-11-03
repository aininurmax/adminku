package com.bdajaya.adminku.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bdajaya.adminku.AdminkuApplication;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.Breadcrumb;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.domain.service.CategoryService;
import dagger.hilt.android.lifecycle.HiltViewModel;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * ViewModel for browsing categories with improved error handling and architecture.
 * This ViewModel focuses on UI state management and delegates business logic to use cases.
 *
 * @author Adminku Development Team
 * @version 2.1.0
 */
@HiltViewModel
public class BrowseCategoryViewModel extends ViewModel {



    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    // UI State LiveData
    private final MutableLiveData<Category> currentCategory = new MutableLiveData<>();
    private final MutableLiveData<List<Breadcrumb>> breadcrumbLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Category>> currentLevelItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryWithPath>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearchingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    // Navigation state
    private String currentParentId = null;

    /**
     * Creates a new BrowseCategoryViewModel with dependencies.
     *
     * @param categoryRepository The category repository for data access
     * @param categoryService The category service for business logic
     */
    @Inject
    public BrowseCategoryViewModel(CategoryRepository categoryRepository, CategoryService categoryService) {
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    /**
     * Simplified constructor for backward compatibility.
     *
     * @param categoryRepository The category repository for data access
     */
    public BrowseCategoryViewModel(CategoryRepository categoryRepository) {
        this(categoryRepository, null); // CategoryService will be null, use repository directly
    }

    public LiveData<List<Breadcrumb>> getBreadcrumb() {
        return breadcrumbLiveData;
    }

    public LiveData<List<Category>> getCurrentLevelItems() {
        return currentLevelItemsLiveData;
    }

    public LiveData<List<CategoryWithPath>> getSearchResults() {
        return searchResultsLiveData;
    }

    public LiveData<Boolean> isSearching() {
        return isSearchingLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessageLiveData;
    }

    public LiveData<List<Category>> getRootCategories() {
        return categoryService.getRootCategoriesLive();
    }

    /**
     * Checks if a category can be deleted and returns deletion info
     */
    public LiveData<CategoryDeletionInfo> getCategoryDeletionInfo(String categoryId) {
        MutableLiveData<CategoryDeletionInfo> result = new MutableLiveData<>();

        isLoadingLiveData.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Category> children = categoryRepository.getChildCategoriesSync(categoryId);
                int productCount = categoryRepository.countProductsByCategoryId(categoryId);

                CategoryDeletionInfo info = new CategoryDeletionInfo(
                        categoryId,
                        children != null && !children.isEmpty(),
                        productCount > 0,
                        productCount
                );

                result.postValue(info);
            } catch (Exception e) {
                ErrorHandler.logError(ErrorHandler.ERROR_CODE_UNKNOWN,
                        "Error checking category deletion info", e);
                result.postValue(null);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });

        return result;
    }

    public void loadRoot() {
        isLoadingLiveData.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> rootCategories = categoryRepository.getRootCategoriesSync();

            // Reset breadcrumb
            List<Breadcrumb> breadcrumbs = new ArrayList<>();
            breadcrumbLiveData.postValue(breadcrumbs);

            currentParentId = null;
            currentLevelItemsLiveData.postValue(rootCategories);
            isLoadingLiveData.postValue(false);
        });
    }

    public void openParent(Category category) {
        isLoadingLiveData.setValue(true);
        currentCategory.setValue(category);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> children = categoryRepository.getChildCategoriesSync(category.getId());

            // Update breadcrumb
            List<Breadcrumb> breadcrumbs = breadcrumbLiveData.getValue();
            if (breadcrumbs == null) {
                breadcrumbs = new ArrayList<>();
            }

            breadcrumbs.add(new Breadcrumb(category));
            breadcrumbLiveData.postValue(new ArrayList<>(breadcrumbs));

            currentParentId = category.getId();
            currentLevelItemsLiveData.postValue(children);
            isLoadingLiveData.postValue(false);
        });
    }

    public void jumpToBreadcrumb(int index) {
        List<Breadcrumb> breadcrumbs = breadcrumbLiveData.getValue();
        if (breadcrumbs == null || index >= breadcrumbs.size()) {
            return;
        }

        isLoadingLiveData.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            // Get the selected breadcrumb
            Breadcrumb selected = breadcrumbs.get(index);

            // Truncate breadcrumb list to this index
            List<Breadcrumb> newBreadcrumbs = new ArrayList<>(breadcrumbs.subList(0, index + 1));
            breadcrumbLiveData.postValue(newBreadcrumbs);

            // Load children of this category
            List<Category> children = categoryRepository.getChildCategoriesSync(selected.getId());
            currentParentId = selected.getId();
            currentLevelItemsLiveData.postValue(children);
            isLoadingLiveData.postValue(false);
        });
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            isSearchingLiveData.setValue(false);
            return;
        }

        isSearchingLiveData.setValue(true);
        isLoadingLiveData.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<CategoryWithPath> results = categoryRepository.searchCategoriesWithPath(query, 20);
            searchResultsLiveData.postValue(results);
            isLoadingLiveData.postValue(false);
        });
    }

    public void clearSearch() {
        isSearchingLiveData.setValue(false);
        searchResultsLiveData.setValue(new ArrayList<>());
    }

    public void addCategory(String name, String parentId) {
        isLoadingLiveData.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Gunakan CategoryRepository.addCategory() yang sudah ada
                // Method ini akan menghitung level secara otomatis dengan validasi
                Result<String> result = categoryRepository.addCategory(parentId, name);

                if (result.isFailure()) {
                    // Gunakan getCategoryOperationErrorCode untuk mendapatkan error code yang tepat
                    String errorCode = result.getErrorCode();
                    if (errorCode != null) {
                        // Gunakan ErrorHandler untuk mendapatkan user-friendly message berdasarkan error code
                        String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                                AdminkuApplication.getInstance().getApplicationContext(), errorCode, result.getErrorMessage());
                        errorMessageLiveData.postValue(userFriendlyMessage);
                    } else {
                        errorMessageLiveData.postValue(result.getErrorMessage());
                    }
                } else {
                    // Refresh the current level data
                    refreshCurrentLevel();
                    errorMessageLiveData.postValue(null); // Clear any previous errors
                }
            } catch (Exception e) {
                errorMessageLiveData.postValue("Error refreshing categories: " + e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void updateCategory(String categoryId, String newName) {
        isLoadingLiveData.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Result<Void> result = categoryRepository.updateCategory(categoryId, newName);
                if (result.isFailure()) {
                    // Gunakan getCategoryOperationErrorCode untuk mendapatkan error code yang tepat
                    String errorCode = result.getErrorCode();
                    if (errorCode != null) {
                        // Gunakan ErrorHandler untuk mendapatkan user-friendly message berdasarkan error code
                        String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                                AdminkuApplication.getInstance().getApplicationContext(), errorCode, result.getErrorMessage());
                        errorMessageLiveData.postValue(userFriendlyMessage);
                    } else {
                        errorMessageLiveData.postValue(result.getErrorMessage());
                    }
                } else {
                    // Refresh the current level data
                    refreshCurrentLevel();
                    errorMessageLiveData.postValue(null); // Clear any previous errors
                }
            } catch (Exception e) {
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(e.getMessage());
                String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                        AdminkuApplication.getInstance().getApplicationContext(), errorCode, e.getMessage());
                errorMessageLiveData.postValue(userFriendlyMessage);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    public void deleteCategory(String categoryId, AdminkuApplication application) {
        isLoadingLiveData.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Menggunakan method deleteCategory yang sudah ada dengan enhancement untuk update produk
                String result = categoryRepository.deleteCategoryWithProductUpdate(categoryId,
                        application.getAppDatabase().productDao());

                if ("SUCCESS".equals(result)) {
                    // Refresh the current level data
                    if (currentParentId != null) {
                        // We're in a subcategory level, refresh parent level counts
                        refreshParentLevel();
                    }
                    refreshCurrentLevel();
                    errorMessageLiveData.postValue(null); // Clear any previous errors
                } else {
                    // Gunakan getCategoryOperationErrorCode untuk error code yang tepat
                    String errorCode = ErrorHandler.getCategoryOperationErrorCode("delete failed");
                    String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                            AdminkuApplication.getInstance().getApplicationContext(), errorCode, "Gagal menghapus kategori");
                    errorMessageLiveData.postValue(userFriendlyMessage);
                }
            } catch (Exception e) {
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(e.getMessage());
                String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                        AdminkuApplication.getInstance().getApplicationContext(), errorCode, e.getMessage());
                errorMessageLiveData.postValue(userFriendlyMessage);
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }

    private void refreshParentLevel() {
        if (currentParentId != null) {
            // Set hasChildren untuk parent
            categoryRepository.updateParentHasChildren(currentParentId,
                    getChildCategoriesSync(currentParentId).size() > 0);
        }
    }

    private List<Category> getChildCategoriesSync(String parentId) {
        try {
            Future<List<Category>> future = AppDatabase.databaseWriteExecutor.submit(new java.util.concurrent.Callable<List<Category>>() {
                @Override
                public List<Category> call() {
                    return categoryRepository.getChildCategoriesSync(parentId);
                }
            });
            return future.get();
        } catch (Exception e) {
            errorMessageLiveData.postValue("Error loading child categories: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void refreshCurrentLevel() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<Category> children;
                if (currentParentId == null) {
                    children = categoryRepository.getRootCategoriesSync();
                } else {
                    children = categoryRepository.getChildCategoriesSync(currentParentId);
                }
                currentLevelItemsLiveData.postValue(children);
            } catch (Exception e) {
                String errorCode = ErrorHandler.getCategoryOperationErrorCode(e.getMessage());
                String userFriendlyMessage = ErrorHandler.getUserFriendlyMessage(
                        AdminkuApplication.getInstance().getApplicationContext(), errorCode, e.getMessage());
                errorMessageLiveData.postValue(userFriendlyMessage);
            }
        });
    }

    public String getCurrentParentId() {
        return currentParentId;
    }

    /**
     * Checks if a category can have subcategories added to it.
     *
     * @param category The category to check
     * @return true if subcategories can be added
     */
    public boolean canAddCategory(Category category) {
        if (category == null) return true;
        return category.getLevel() < Constants.MAX_CATEGORY_LEVEL;
    }

    /**
     * Gets the current category being displayed.
     *
     * @return The current category or null if none
     */
    public Category getCurrentCategory() {
        List<Category> currentItems = currentLevelItemsLiveData.getValue();
        return currentItems != null && !currentItems.isEmpty() ? currentItems.get(0) : null;
    }

    /**
     * Checks if the maximum category depth has been reached.
     *
     * @return true if maximum depth is reached
     */
    public boolean isMaxDepthReached() {
        List<Breadcrumb> breadcrumbs = breadcrumbLiveData.getValue();
        if (breadcrumbs == null) {
            return false;
        }
        return breadcrumbs.size() >= Constants.MAX_CATEGORY_LEVEL;
    }

    /**
     * Gets the current navigation depth based on breadcrumbs.
     *
     * @return The current depth level
     */
    public int getCurrentDepth() {
        List<Breadcrumb> breadcrumbs = breadcrumbLiveData.getValue();
        return breadcrumbs != null ? breadcrumbs.size() : 0;
    }

    /**
     * Checks if subcategories can be added to the given category.
     *
     * @param category The category to check
     * @return true if subcategories can be added
     */
    public boolean canAddSubcategoryTo(Category category) {
        if (category == null) return true; // root level
        return category.getLevel() < Constants.MAX_CATEGORY_LEVEL;
    }

    /**
     * Gets the level of the current category.
     *
     * @return The current category level or 0 if no category
     */
    public int getCurrentCategoryLevel() {
        Category current = getCurrentCategory();
        return current != null ? current.getLevel() : Constants.ROOT_CATEGORY_LEVEL;
    }

    /**
     * Data class for category deletion information
     */
    public static class CategoryDeletionInfo {
        private final String categoryId;
        private final boolean hasChildren;
        private final boolean hasProducts;
        private final int productCount;

        public CategoryDeletionInfo(String categoryId, boolean hasChildren,
                                    boolean hasProducts, int productCount) {
            this.categoryId = categoryId;
            this.hasChildren = hasChildren;
            this.hasProducts = hasProducts;
            this.productCount = productCount;
        }

        // Getters
        public String getCategoryId() { return categoryId; }
        public boolean hasChildren() { return hasChildren; }
        public boolean hasProducts() { return hasProducts; }
        public int getProductCount() { return productCount; }
        public boolean canDeleteSafely() { return !hasChildren && !hasProducts; }
    }
}