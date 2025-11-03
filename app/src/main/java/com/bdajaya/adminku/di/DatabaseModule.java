package com.bdajaya.adminku.di;

import android.content.Context;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.*;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return AppDatabase.getInstance(context);
    }

    @Provides
    @Singleton
    public ProductDao provideProductDao(AppDatabase database) {
        return database.productDao();
    }

    @Provides
    @Singleton
    public ProductImageDao provideProductImageDao(AppDatabase database) {
        return database.productImageDao();
    }

    @Provides
    @Singleton
    public CategoryDao provideCategoryDao(AppDatabase database) {
        return database.categoryDao();
    }

    @Provides
    @Singleton
    public UnitDao provideUnitDao(AppDatabase database) {
        return database.unitDao();
    }

    @Provides
    @Singleton
    public StockTransactionDao provideStockTransactionDao(AppDatabase database) {
        return database.stockTransactionDao();
    }

    @Provides
    @Singleton
    public BrandDao provideBrandDao(AppDatabase database) {
        return database.brandDao();
    }
}