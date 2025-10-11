package com.bdajaya.adminku;

import android.app.Application;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.repository.CategoryRepository;
import com.bdajaya.adminku.data.repository.ProductRepository;

public class AdminkuApplication extends Application {

    private AppDatabase database;
    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize database
        database = AppDatabase.getInstance(this);

        // Initialize repositories
        productRepository = new ProductRepository(database.productDao(), database.productImageDao(), database.categoryDao());
        categoryRepository = new CategoryRepository(database.categoryDao(), database.configDao());
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
}
