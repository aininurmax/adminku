package com.bdajaya.adminku.domain.service;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.entity.Brand;
import com.bdajaya.adminku.data.repository.BrandRepository;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BrandService {
    private final BrandRepository brandRepository;

    @Inject
    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public Result<String> addBrand(String name) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            return Result.failure("Nama brand tidak boleh kosong");
        }

        if (name.length() < 2) {
            return Result.failure("Nama brand minimal 2 karakter");
        }

        if (name.length() > 100) {
            return Result.failure("Nama brand maksimal 100 karakter");
        }

        // Check if brand name already exists
        if (brandRepository.isBrandNameExists(name.trim())) {
            return Result.failure("Nama brand sudah digunakan");
        }

        try {
            Brand brand = new Brand("", name.trim(), 0, 0);
            String brandId = brandRepository.insertBrand(brand);
            return Result.success(brandId);
        } catch (Exception e) {
            return Result.failure("Gagal menambah brand: " + e.getMessage());
        }
    }

    public Result<Void> updateBrand(String brandId, String name) {
        // Validate input
        if (brandId == null || brandId.trim().isEmpty()) {
            return Result.failure("ID brand tidak valid");
        }

        if (name == null || name.trim().isEmpty()) {
            return Result.failure("Nama brand tidak boleh kosong");
        }

        if (name.length() < 2) {
            return Result.failure("Nama brand minimal 2 karakter");
        }

        if (name.length() > 100) {
            return Result.failure("Nama brand maksimal 100 karakter");
        }

        try {
            Brand existingBrand = brandRepository.getBrandByIdSync(brandId);
            if (existingBrand == null) {
                return Result.failure("Brand tidak ditemukan");
            }

            // Check if name changed and if new name already exists
            if (!existingBrand.getName().equals(name.trim()) &&
                brandRepository.isBrandNameExists(name.trim())) {
                return Result.failure("Nama brand sudah digunakan");
            }

            existingBrand.setName(name.trim());
            brandRepository.updateBrand(existingBrand);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Gagal mengupdate brand: " + e.getMessage());
        }
    }

    public Result<Void> deleteBrand(String brandId) {
        // Validate input
        if (brandId == null || brandId.trim().isEmpty()) {
            return Result.failure("ID brand tidak valid");
        }

        try {
            Brand brand = brandRepository.getBrandByIdSync(brandId);
            if (brand == null) {
                return Result.failure("Brand tidak ditemukan");
            }

            // Check if brand has products
            int productCount = brandRepository.countProductsByBrandId(brandId);
            if (productCount > 0) {
                return Result.failure("Tidak dapat menghapus brand yang masih memiliki produk (" + productCount + " produk)");
            }

            brandRepository.deleteBrand(brand);
            return Result.success(null);
        } catch (Exception e) {
            return Result.failure("Gagal menghapus brand: " + e.getMessage());
        }
    }

    public LiveData<List<Brand>> getAllBrands() {
        return brandRepository.getAllBrands();
    }

    public List<Brand> searchBrands(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return brandRepository.getAllBrandsSync();
        }
        return brandRepository.searchBrands(query.trim(), limit);
    }
}
