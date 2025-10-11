package com.bdajaya.adminku.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.Breadcrumb;
import com.bdajaya.adminku.data.model.CategoryWithPath;
import com.bdajaya.adminku.data.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BrowseCategoryViewModel extends ViewModel {
    // Removed hardcoded MAX_CATEGORY_LEVEL, now using dynamic from repository

    public final CategoryRepository categoryRepository;

    private MutableLiveData<Category> currentCategory = new MutableLiveData<>();
    private final MutableLiveData<List<Breadcrumb>> breadcrumbLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Category>> currentLevelItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryWithPath>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearchingLiveData = new MutableLiveData<>(false);
    //
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    private String currentParentId = null;

    public BrowseCategoryViewModel(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
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
        long startTime = System.currentTimeMillis();

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

            // Ensure minimum loading duration for smooth UX
            long elapsedTime = System.currentTimeMillis() - startTime;
            long remainingTime = Math.max(0, 500 - elapsedTime);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                isLoadingLiveData.setValue(false);
            }, remainingTime);
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

    public void addCategory(String name, int level, String parentId) {
        isLoadingLiveData.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Generate a new UUID for the category
                String id = UUID.randomUUID().toString();
                long now = System.currentTimeMillis();

                Category newCategory = new Category(
                        id,          // Generated UUID
                        parentId,    // Parent ID
                        level,      // Level
                        name,       // Name
                        null,       // No icon URL
                        now,        // Created at
                        now        // Updated at
                );

                // Insert the category
                categoryRepository.insert(newCategory);

                // Refresh the current level data
                refreshCurrentLevel();
                errorMessageLiveData.postValue(null); // Clear any previous errors
            } catch (Exception e) {
                errorMessageLiveData.postValue(e.getMessage());
            } finally {
                isLoadingLiveData.postValue(false);
            }
        });
    }
    public void refreshCurrentLevel() {
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
                // Ignore errors during refresh
            }
        });
    }


    public String getCurrentParentId() {
        return currentParentId;
    }

    public boolean canAddCategory(Category category) {
        if (category == null) return true;
        return category.getLevel() < categoryRepository.getMaxDepth();
    }

    public Category getCurrentCategory() {
        List<Category> currentItems = currentLevelItemsLiveData.getValue();
        return currentItems != null && !currentItems.isEmpty() ? currentItems.get(0) : null;
    }

    public boolean isMaxDepthReached() {
        return getCurrentCategoryLevel() >= categoryRepository.getMaxDepth();
    }

    public int getCurrentCategoryLevel() {
        Category current = getCurrentCategory();
        return current != null ? current.getLevel() : 0;
    }
}
