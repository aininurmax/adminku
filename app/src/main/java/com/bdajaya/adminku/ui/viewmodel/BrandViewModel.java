package com.bdajaya.adminku.ui.viewmodel;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.data.repository.BrandRepository;
import com.bdajaya.adminku.domain.service.BrandService;

import java.util.List;

public class BrandViewModel extends ViewModel {
    private final BrandService brandService;
    private final MutableLiveData<List<Brand>> brands = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public BrandViewModel(BrandRepository brandRepository) {
        this.brandService = new BrandService(brandRepository);
        loadBrands();
    }

    public LiveData<List<Brand>> getBrands() {
        return brands;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadBrands() {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        brandService.getAllBrands().observeForever(brands -> {
            isLoading.setValue(false);
            this.brands.setValue(brands);
        });
    }

    public void searchBrands(String query) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        // Use a background thread for search
        new Thread(() -> {
            try {
                List<Brand> searchResults = brandService.searchBrands(query, 50);
                brands.postValue(searchResults);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error searching brands: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    public void addBrand(String name) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            try {
                Result<String> result = brandService.addBrand(name);
                if (result.isSuccess()) {
                    // Reload the list on main thread
                    new Handler(Looper.getMainLooper()).post(() -> loadBrands());
                } else {
                    errorMessage.postValue(result.getErrorMessage());
                }
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error adding brand: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    public void updateBrand(String brandId, String name) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            try {
                Result<Void> result = brandService.updateBrand(brandId, name);
                if (result.isSuccess()) {
                    // Reload the list on main thread
                    new Handler(Looper.getMainLooper()).post(() -> loadBrands());
                } else {
                    errorMessage.postValue(result.getErrorMessage());
                }
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error updating brand: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }

    public void deleteBrand(String brandId) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        new Thread(() -> {
            try {
                Result<Void> result = brandService.deleteBrand(brandId);
                if (result.isSuccess()) {
                    // Reload the list on main thread
                    new Handler(Looper.getMainLooper()).post(() -> loadBrands());
                } else {
                    errorMessage.postValue(result.getErrorMessage());
                }
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Error deleting brand: " + e.getMessage());
                isLoading.postValue(false);
            }
        }).start();
    }
}
