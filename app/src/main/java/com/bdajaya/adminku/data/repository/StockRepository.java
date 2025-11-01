package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;
import com.bdajaya.adminku.core.BaseRepository;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.StockTransactionDao;
import com.bdajaya.adminku.data.entity.StockTransaction;
import com.bdajaya.adminku.data.entity.Unit;

import java.util.List;

/**
 * Repository for stock transactions with unit conversion support.
 * ALL stock quantities are stored in BASE UNITS for consistency.
 */
public class StockRepository extends BaseRepository {
    private final StockTransactionDao stockTransactionDao;
    private final UnitRepository unitRepository;

    public StockRepository(StockTransactionDao stockTransactionDao, UnitRepository unitRepository) {
        this.stockTransactionDao = stockTransactionDao;
        this.unitRepository = unitRepository;
    }

    @Override
    protected String getRepositoryName() {
        return "StockRepository";
    }

    // ================================
    // READ OPERATIONS
    // ================================

    public LiveData<List<StockTransaction>> getTransactionsByProductId(String productId) {
        return stockTransactionDao.getByProductId(productId);
    }

    public List<StockTransaction> getTransactionsByProductIdSync(String productId) {
        return stockTransactionDao.getByProductIdSync(productId);
    }

    public List<StockTransaction> getTransactionsByTimeRange(long startTime, long endTime) {
        return stockTransactionDao.getByTimeRange(startTime, endTime);
    }

    /**
     * Get total stock in BASE UNIT
     */
    public long getTotalStockForProduct(String productId) {
        return stockTransactionDao.getTotalStockForProduct(productId);
    }

    /**
     * Get stock in specific unit (converted from base unit)
     */
    public Result<Long> getStockInUnit(String productId, String unitId) {
        return executeReadOperation(() -> {
            long baseStock = stockTransactionDao.getTotalStockForProduct(productId);
            Unit unit = unitRepository.getUnitByIdSync(unitId);

            if (unit == null) {
                throw new IllegalArgumentException("Unit not found");
            }

            return unit.fromBaseUnit(baseStock);
        }, "getStockInUnit");
    }

    // ================================
    // WRITE OPERATIONS WITH UNIT CONVERSION
    // ================================

    /**
     * Add stock - converts to base unit automatically
     */
    public void addStock(String productId, long quantity, String unitId, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Unit unit = unitRepository.getUnitByIdSync(unitId);
                if (unit == null) {
                    throw new IllegalArgumentException("Unit not found");
                }

                // Convert to base unit
                long baseQuantity = unit.toBaseUnit(quantity);

                StockTransaction transaction = new StockTransaction.Builder(
                        productId, StockTransaction.TransactionType.ADD)
                        .quantity(baseQuantity)
                        .originalQuantity(quantity)
                        .originalConversionFactor(unit.getConversionFactor())
                        .unitId(unitId)
                        .notes(notes)
                        .build();

                stockTransactionDao.insert(transaction);
                logOperationSuccess("addStock: " + quantity + " " + unit.getName());
            } catch (Exception e) {
                logOperationFailure("addStock", e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Remove stock - converts to base unit automatically
     */
    public void removeStock(String productId, long quantity, String unitId, String notes) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Unit unit = unitRepository.getUnitByIdSync(unitId);
                if (unit == null) {
                    throw new IllegalArgumentException("Unit not found");
                }

                // Convert to base unit
                long baseQuantity = unit.toBaseUnit(quantity);

                // Check stock availability
                long currentStock = stockTransactionDao.getTotalStockForProduct(productId);
                if (currentStock < baseQuantity) {
                    throw new IllegalStateException("Insufficient stock");
                }

                StockTransaction transaction = new StockTransaction.Builder(
                        productId, StockTransaction.TransactionType.REMOVE)
                        .quantity(baseQuantity)
                        .originalQuantity(quantity)
                        .originalConversionFactor(unit.getConversionFactor())
                        .unitId(unitId)
                        .notes(notes)
                        .build();

                stockTransactionDao.insert(transaction);
                logOperationSuccess("removeStock: " + quantity + " " + unit.getName());
            } catch (Exception e) {
                logOperationFailure("removeStock", e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Adjust stock to specific quantity - converts to base unit
     */
    public void adjustStock(String productId, long quantity, String unitId, String notes) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                Unit unit = unitRepository.getUnitByIdSync(unitId);
                if (unit == null) {
                    throw new IllegalArgumentException("Unit not found");
                }

                // Convert to base unit
                long baseQuantity = unit.toBaseUnit(quantity);

                StockTransaction transaction = new StockTransaction.Builder(
                        productId, StockTransaction.TransactionType.ADJUST)
                        .quantity(baseQuantity)
                        .originalQuantity(quantity)
                        .originalConversionFactor(unit.getConversionFactor())
                        .unitId(unitId)
                        .notes(notes)
                        .build();

                stockTransactionDao.insert(transaction);
                logOperationSuccess("adjustStock: " + quantity + " " + unit.getName());
            } catch (Exception e) {
                logOperationFailure("adjustStock", e.getMessage());
                throw e;
            }
        });
    }

    // ================================
    // LEGACY & UTILITY METHODS
    // ================================

    public List<StockTransaction> getRecentTransactions(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        return stockTransactionDao.getRecentTransactions(limit);
    }

    public List<StockTransaction> getTransactionsByProductIdAndType(String productId,
                                                                    StockTransaction.TransactionType transactionType) {
        return stockTransactionDao.getByProductIdAndType(productId, transactionType.getValue());
    }

    public StockTransaction getLastTransactionForProduct(String productId) {
        return stockTransactionDao.getLastTransactionForProduct(productId);
    }

    public int getTransactionCountForProduct(String productId) {
        return stockTransactionDao.getTransactionCountForProduct(productId);
    }

    public int deleteTransactionsByProductId(String productId) {
        return stockTransactionDao.deleteByProductId(productId);
    }

    public int deleteOldTransactions(long timestamp) {
        return stockTransactionDao.deleteOlderThan(timestamp);
    }

    public void insertAllTransactions(List<StockTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return;
        }

        for (StockTransaction transaction : transactions) {
            if (!transaction.isValid()) {
                throw new IllegalArgumentException("Invalid transaction: " + transaction.getId());
            }
        }

        AppDatabase.databaseWriteExecutor.execute(() -> {
            stockTransactionDao.insertAll(transactions);
        });
    }
}