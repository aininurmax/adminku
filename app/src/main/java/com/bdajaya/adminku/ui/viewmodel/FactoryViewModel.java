package com.bdajaya.adminku.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bdajaya.adminku.data.repository.BrandRepository;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.data.repository.ProductRepository;
import com.bdajaya.adminku.data.repository.UnitRepository;
import com.bdajaya.adminku.domain.service.UnitService;

public class FactoryViewModel implements ViewModelProvider.Factory {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;

    // Constructor untuk BrowseCategoryViewModel
    public FactoryViewModel(CategoryRepository categoryRepository) {
        this.productRepository = null;
        this.categoryRepository = categoryRepository;
        this.brandRepository = null;
        this.unitRepository = null;
    }

    // Constructor untuk AddEditProductViewModel with all repositories
    public FactoryViewModel(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository, UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitRepository = unitRepository;
    }

    // Constructor untuk AddEditProductViewModel
    public FactoryViewModel(ProductRepository productRepository, CategoryRepository categoryRepository, BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.unitRepository = null;
    }

    // Constructor untuk AddEditProductViewModel (backwards compatibility)
    public FactoryViewModel(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = null;
        this.unitRepository = null;
    }

    public FactoryViewModel(ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = null;
        this.brandRepository = null;
        this.unitRepository = null;
    }

    // Constructor untuk BrandViewModel
    public FactoryViewModel(BrandRepository brandRepository) {
        this.productRepository = null;
        this.categoryRepository = null;
        this.brandRepository = brandRepository;
        this.unitRepository = null;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(BrowseCategoryViewModel.class)) {
            return (T) new BrowseCategoryViewModel(categoryRepository);
        } else if (modelClass.isAssignableFrom(AddEditProductViewModel.class)) {
            if (brandRepository != null && unitRepository != null) {
                return (T) new AddEditProductViewModel(productRepository, categoryRepository, brandRepository, unitRepository);
            } else if (brandRepository != null) {
                return (T) new AddEditProductViewModel(productRepository, categoryRepository, brandRepository);
            } else {
                return (T) new AddEditProductViewModel(productRepository, categoryRepository);
            }
        } else if (modelClass.isAssignableFrom(UnitViewModel.class)) {
            UnitService unitService = new UnitService(unitRepository);
            return (T) new UnitViewModel(unitService);
        } else if (modelClass.isAssignableFrom(ProductManagementViewModel.class)) {
            return (T) new ProductManagementViewModel(productRepository);
        } else if (modelClass.isAssignableFrom(BrandViewModel.class)) {
            return (T) new BrandViewModel(brandRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}