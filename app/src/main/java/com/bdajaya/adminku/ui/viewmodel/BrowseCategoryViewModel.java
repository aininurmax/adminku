package com.bdajaya.adminku.ui.viewmodel;

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

public class BrowseCategoryViewModel extends ViewModel {

    private final CategoryRepository categoryRepository;

    private final MutableLiveData<List<Breadcrumb>> breadcrumbLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Category>> currentLevelItemsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CategoryWithPath>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSearchingLiveData = new MutableLiveData<>(false);
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

    public void openParent(Category parent) {
        isLoadingLiveData.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Category> children = categoryRepository.getChildCategoriesSync(parent.getId());

            // Update breadcrumb
            List<Breadcrumb> breadcrumbs = breadcrumbLiveData.getValue();
            if (breadcrumbs == null) {
                breadcrumbs = new ArrayList<>();
            }

            breadcrumbs.add(new Breadcrumb(parent));
            breadcrumbLiveData.postValue(new ArrayList<>(breadcrumbs));

            currentParentId = parent.getId();
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

    public String addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            errorMessageLiveData.setValue("Category name cannot be empty");
            return null;
        }

        try {
            String result = categoryRepository.addCategory(currentParentId, name.trim());

            if (result == null) {
                errorMessageLiveData.setValue("Category name already exists at this level");
                return null;
            } else if (result.equals("MAX_DEPTH_REACHED")) {
                errorMessageLiveData.setValue("Maximum category depth reached");
                return null;
            }

            // Refresh current level
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<Category> children = categoryRepository.getChildCategoriesSync(currentParentId);
                currentLevelItemsLiveData.postValue(children);
            });

            return result;
        } catch (Exception e) {
            errorMessageLiveData.setValue("Error adding category: " + e.getMessage());
            return null;
        }
    }

    public boolean isMaxDepthReached() {
        return categoryRepository.isMaxDepthReached(currentParentId);
    }

    public String getCurrentParentId() {
        return currentParentId;
    }
}

