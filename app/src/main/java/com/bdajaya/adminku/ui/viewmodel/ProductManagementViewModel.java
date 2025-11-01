package com.bdajaya.adminku.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.model.ProductWithDetails;
import com.bdajaya.adminku.data.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

public class ProductManagementViewModel extends ViewModel {

    private final ProductRepository productRepository;

    private final LiveData<List<ProductWithDetails>> liveProducts;
    private final LiveData<List<ProductWithDetails>> outOfStockProducts;
    private final LiveData<List<ProductWithDetails>> archivedProducts;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<List<ProductWithDetails>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ProductManagementViewModel(ProductRepository productRepository) {
        this.productRepository = productRepository;

        // Initialize LiveData for each tab
        liveProducts = productRepository.getProductsWithDetailsByStatus("LIVE");
        outOfStockProducts = productRepository.getProductsWithDetailsByStatus("OUT_OF_STOCK");
        archivedProducts = productRepository.getProductsWithDetailsByStatus("ARCHIVED");
    }

    public LiveData<List<ProductWithDetails>> getLiveProducts() {
        return liveProducts;
    }

    public LiveData<List<ProductWithDetails>> getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public LiveData<List<ProductWithDetails>> getArchivedProducts() {
        return archivedProducts;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public LiveData<List<ProductWithDetails>> getSearchResults() {
        return searchResults;
    }

    public LiveData<Boolean> isSearching() {
        return isSearching;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void search(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearSearch();
            return;
        }

        searchQuery.setValue(query);
        isSearching.setValue(true);
        isLoading.setValue(true);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<ProductWithDetails> results = productRepository.searchProductsWithDetails(query, 50);
            searchResults.postValue(results);
            isLoading.postValue(false);
        });
    }

    public void clearSearch() {
        searchQuery.setValue("");
        isSearching.setValue(false);
        searchResults.setValue(new ArrayList<>());
    }

    public void archiveProduct(String productId) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productRepository.updateProductStatus(productId, "ARCHIVED");
        });
    }

    public void unarchiveProduct(String productId, long stock) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            String newStatus = stock > 0 ? "LIVE" : "OUT_OF_STOCK";
            productRepository.updateProductStatus(productId, newStatus);
        });
    }

    public void deleteProduct(Product product) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            productRepository.deleteProduct(product);
        });
    }
}

