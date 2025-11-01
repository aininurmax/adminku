package com.bdajaya.adminku;

import android.app.Application;

import android.content.Context;
import com.bdajaya.adminku.data.manager.ImageStorageManager;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.repository.BrandRepository;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.data.repository.ProductRepository;
import com.bdajaya.adminku.data.repository.StockRepository;
import com.bdajaya.adminku.data.repository.UnitRepository;

public class AdminkuApplication extends Application {

    private static AdminkuApplication instance;
    private AppDatabase database;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private BrandRepository brandRepository;
    private UnitRepository unitRepository;
    private StockRepository stockRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize database
        database = AppDatabase.getInstance(this);

        // Initialize repositories
        productRepository = new ProductRepository(
                this,
                database.productDao(),
                database.productImageDao()
        );
        categoryRepository = new CategoryRepository(database.categoryDao());
        brandRepository = new BrandRepository(database.brandDao());
        unitRepository = new UnitRepository(database.unitDao());
        stockRepository = new StockRepository(database.stockTransactionDao(), unitRepository);

    }

    public static AdminkuApplication getInstance() {
        return instance;
    }

    public AppDatabase getDatabase() {
        return database;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public UnitRepository getUnitRepository() {
        return unitRepository;
    }

    public StockRepository getStockRepository() {
        return stockRepository;
    }

    public BrandRepository getBrandRepository() {
        return brandRepository;
    }

    public AppDatabase getAppDatabase() {
        return database;
    }

    public ImageStorageManager getImageStorageManager() {
        return new ImageStorageManager(this);
    }
}
