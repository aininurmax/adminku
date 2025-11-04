package com.bdajaya.adminku;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.manager.ImageStorageManager;
import com.bdajaya.adminku.data.repository.*;
import dagger.hilt.android.HiltAndroidApp;
import javax.inject.Inject;

@HiltAndroidApp
public class AdminkuApplication extends Application {
    private static AdminkuApplication instance;

    @Inject AppDatabase appDatabase;
    @Inject ProductRepository productRepository;
    @Inject CategoryRepository categoryRepository;
    @Inject BrandRepository brandRepository;
    @Inject UnitRepository unitRepository;
    @Inject StockRepository stockRepository;
    @Inject ImageStorageManager imageStorageManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Apply dark mode preference on app start
        applyDarkModePreference();
    }

    private void applyDarkModePreference() {
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        boolean isDarkModeEnabled = preferences.getBoolean("dark_mode", false);

        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void toggleDarkMode(boolean enabled) {
        SharedPreferences preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        preferences.edit().putBoolean("dark_mode", enabled).apply();

        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static AdminkuApplication getInstance() {
        return instance;
    }

    public AppDatabase getAppDatabase() {
        return appDatabase;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public BrandRepository getBrandRepository() {
        return brandRepository;
    }

    public UnitRepository getUnitRepository() {
        return unitRepository;
    }

    public StockRepository getStockRepository() {
        return stockRepository;
    }

    public ImageStorageManager getImageStorageManager() {
        return imageStorageManager;
    }
}
