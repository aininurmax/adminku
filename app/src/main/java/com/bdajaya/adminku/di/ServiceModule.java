package com.bdajaya.adminku.di;

import com.bdajaya.adminku.data.repository.*;
import com.bdajaya.adminku.domain.service.*;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class ServiceModule {

    @Provides
    @Singleton
    public UnitService provideUnitService(UnitRepository unitRepository) {
        return new UnitService(unitRepository);
    }

    @Provides
    @Singleton
    public BrandService provideBrandService(BrandRepository brandRepository) {
        return new BrandService(brandRepository);
    }

    @Provides
    @Singleton
    public StockService provideStockService(
            StockRepository stockRepository,
            UnitRepository unitRepository
    ) {
        return new StockService(stockRepository, unitRepository);
    }

}