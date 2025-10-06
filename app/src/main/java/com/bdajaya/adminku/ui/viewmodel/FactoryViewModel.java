package com.bdajaya.adminku.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.data.repository.ProductRepository;

public class FactoryViewModel implements ViewModelProvider.Factory {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    // Constructor for CategoryRepository only
    public FactoryViewModel(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = null;
    }

    // Constructor for ProductRepository only
    public FactoryViewModel(ProductRepository productRepository) {
        this.categoryRepository = null;
        this.productRepository = productRepository;
    }

    // Constructor for all repositories
    public FactoryViewModel(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BrowseCategoryViewModel.class)) {
            if (categoryRepository == null) {
                throw new IllegalArgumentException("CategoryRepository is required for BrowseCategoryViewModel");
            }
            return (T) new BrowseCategoryViewModel(categoryRepository);
        } else if (modelClass.isAssignableFrom(MainActivityViewModel.class)) {
            if (productRepository == null || categoryRepository == null) {
                throw new IllegalArgumentException("All repositories are required for MainActivityViewModel");
            }
            return (T) new MainActivityViewModel(productRepository, categoryRepository);
        }

        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
