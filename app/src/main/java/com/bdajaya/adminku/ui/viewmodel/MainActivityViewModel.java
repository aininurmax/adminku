package com.bdajaya.adminku.ui.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.data.repository.ProductRepository;

public class MainActivityViewModel extends ViewModel {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<String> productId = new MutableLiveData<>(null);
    private final MutableLiveData<String> categoryId = new MutableLiveData<>(null);
    private final MutableLiveData<String> categoryName = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>(false);
    private boolean isEditMode = false;

    public MainActivityViewModel(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }
    public void loadProduct(String id) {
        if (productId.getValue() == null || productId.getValue().isEmpty()) {
            // New product mode
            isEditMode = false;
            isLoading.setValue(false);
            return;
        }
        // Edit mode
        isEditMode = true;
        isLoading.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            var product = productRepository.getProductByIdSync(id);
            if (product != null) {
                productId.postValue(product.getId());
                categoryId.postValue(product.getCategoryId());

                // Load category name
                if (product.getCategoryId() != null) {
                    var category = categoryRepository.getCategoryByIdSync(product.getCategoryId());
                    if (category != null) {
                        categoryName.postValue(category.getName());
                    } else {
                        categoryName.postValue("");
                    }
                } else {
                    categoryName.postValue("");
                }
            } else {
                errorMessage.postValue("Product not found");
            }
            isLoading.postValue(false);
        });
    }
    // Save product
    public void saveProduct() {
        isLoading.setValue(true);
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // Create or update product
                // For simplicity, we are not handling images here
                Product product;
                if (isEditMode && productId.getValue() != null) {
                    // Update existing product
                    product = productRepository.getProductByIdSync(productId.getValue());
                    if (product == null) {
                        errorMessage.postValue("Product not found");
                        isLoading.postValue(false);
                        return;
                    }
                } else {
                    // Create new product
                    product = new Product(
                            null, // ID will be generated
                            "", // Name will be set below
                            "", // Description will be set below
                            "", // Barcode will be generated
                            null, // Category will be set below
                            "", // Unit will be set below
                            0, // Buy price will be set below
                            0, // Sell price will be set below
                            0, // Margin will be set below
                            0, // Stock will be set below
                            "active", // Status
                            System.currentTimeMillis(), // Created at
                            System.currentTimeMillis() // Updated at
                    );
                }
                // Update product fields
                product.setCategoryId(categoryId.getValue());

                // Save product
                if (isEditMode) {
                    productRepository.updateProduct(product, null);
                } else {
                    String newId = productRepository.insertProduct(product, null);
                    productId.postValue(newId);
                }
                saveSuccess.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Error saving product: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });

    }
}
