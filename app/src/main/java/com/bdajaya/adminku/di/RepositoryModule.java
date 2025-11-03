package com.bdajaya.adminku.di;

import android.content.Context;
import com.bdajaya.adminku.data.dao.*;
import com.bdajaya.adminku.data.manager.ImageStorageManager;
import com.bdajaya.adminku.data.repository.*;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {

    @Provides
    @Singleton
    public ProductRepository provideProductRepository(
            @ApplicationContext Context context,
            ProductDao productDao,
            ProductImageDao productImageDao
    ) {
        return new ProductRepository(context, productDao, productImageDao);
    }

    @Provides
    @Singleton
    public CategoryRepository provideCategoryRepository(CategoryDao categoryDao) {
        return new CategoryRepository(categoryDao);
    }

    @Provides
    @Singleton
    public BrandRepository provideBrandRepository(BrandDao brandDao) {
        return new BrandRepository(brandDao);
    }

    @Provides
    @Singleton
    public UnitRepository provideUnitRepository(UnitDao unitDao) {
        return new UnitRepository(unitDao);
    }

    @Provides
    @Singleton
    public StockRepository provideStockRepository(
            StockTransactionDao stockTransactionDao,
            UnitRepository unitRepository
    ) {
        return new StockRepository(stockTransactionDao, unitRepository);
    }

    @Provides
    @Singleton
    public ImageStorageManager provideImageStorageManager(@ApplicationContext Context context) {
        return new ImageStorageManager(context);
    }
}