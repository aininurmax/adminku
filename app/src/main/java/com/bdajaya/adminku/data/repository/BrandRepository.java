package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.BrandDao;
import com.bdajaya.adminku.data.entity.Brand;

import java.util.List;
import java.util.UUID;

public class BrandRepository {
    private final BrandDao brandDao;

    public BrandRepository(BrandDao brandDao) {
        this.brandDao = brandDao;
    }

    public LiveData<Brand> getBrandById(String id) {
        return brandDao.getById(id);
    }

    public Brand getBrandByIdSync(String id) {
        return brandDao.getByIdSync(id);
    }

    public LiveData<List<Brand>> getAllBrands() {
        return brandDao.getAll();
    }

    public List<Brand> getAllBrandsSync() {
        return brandDao.getAllSync();
    }

    public List<Brand> searchBrands(String query, int limit) {
        return brandDao.search(query, limit);
    }

    public String insertBrand(Brand brand) {
        if (brand.getId() == null || brand.getId().isEmpty()) {
            brand.setId(UUID.randomUUID().toString());
        }

        // Set timestamps
        long now = System.currentTimeMillis();
        brand.setCreatedAt(now);
        brand.setUpdatedAt(now);

        // Insert brand
        AppDatabase.databaseWriteExecutor.execute(() -> {
            brandDao.insert(brand);
        });

        return brand.getId();
    }

    public void updateBrand(Brand brand) {
        // Update timestamp
        brand.setUpdatedAt(System.currentTimeMillis());

        AppDatabase.databaseWriteExecutor.execute(() -> {
            brandDao.update(brand);
        });
    }

    public void deleteBrand(Brand brand) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            brandDao.delete(brand);
        });
    }

    public boolean isBrandNameExists(String name) {
        return brandDao.countByName(name) > 0;
    }

    public int countProductsByBrandId(String brandId) {
        return brandDao.countProductsByBrandId(brandId);
    }
}
