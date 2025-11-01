package com.bdajaya.adminku.domain.service;

import androidx.lifecycle.LiveData;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.entity.StockTransaction;
import com.bdajaya.adminku.data.entity.Unit;
import com.bdajaya.adminku.data.repository.StockRepository;
import com.bdajaya.adminku.data.repository.UnitRepository;
import java.util.List;

/**
 * Service for managing stock transactions with unit conversion.
 * Ensures all stock is stored in base units for consistency.
 */
public class StockService {
    private final StockRepository stockRepository;
    private final UnitRepository unitRepository;
    private final UnitService unitService;

    public StockService(StockRepository stockRepository, UnitRepository unitRepository) {
        this.stockRepository = stockRepository;
        this.unitRepository = unitRepository;
        this.unitService = new UnitService(unitRepository);
    }

    /**
     * Add stock with automatic conversion to base unit.
     */
    public Result<Void> addStock(String productId, long quantity, String unitId, String notes) {
        // Validate inputs
        ValidationUtils.ValidationResult productValidation = ValidationUtils.validateNotEmpty(productId, "Product ID");
        if (productValidation.isFailure()) {
            return Result.failure(productValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult unitValidation = ValidationUtils.validateNotEmpty(unitId, "Unit ID");
        if (unitValidation.isFailure()) {
            return Result.failure(unitValidation.getErrorMessage());
        }

        if (quantity <= 0) {
            return Result.failure("Kuantitas harus lebih dari 0");
        }

        try {
            // Get unit for conversion
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            // Convert to base unit
            Result<Long> conversionResult = unitService.convertToBaseUnit(quantity, unitId);
            if (conversionResult.isFailure()) {
                return Result.failure(conversionResult.getErrorMessage());
            }

            long baseQuantity = conversionResult.getData();

            // Add stock transaction
            stockRepository.addStock(productId, baseQuantity, unitId, notes);

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Add stock");
        }
    }

    /**
     * Remove stock with automatic conversion to base unit.
     */
    public Result<Void> removeStock(String productId, long quantity, String unitId, String notes) {
        ValidationUtils.ValidationResult productValidation = ValidationUtils.validateNotEmpty(productId, "Product ID");
        if (productValidation.isFailure()) {
            return Result.failure(productValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult unitValidation = ValidationUtils.validateNotEmpty(unitId, "Unit ID");
        if (unitValidation.isFailure()) {
            return Result.failure(unitValidation.getErrorMessage());
        }

        if (quantity <= 0) {
            return Result.failure("Kuantitas harus lebih dari 0");
        }

        try {
            // Get unit for conversion
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            // Convert to base unit
            Result<Long> conversionResult = unitService.convertToBaseUnit(quantity, unitId);
            if (conversionResult.isFailure()) {
                return Result.failure(conversionResult.getErrorMessage());
            }

            long baseQuantity = conversionResult.getData();

            // Check if enough stock available
            long currentStock = stockRepository.getTotalStockForProduct(productId);
            if (currentStock < baseQuantity) {
                return Result.failure("Stok tidak mencukupi");
            }

            // Remove stock transaction
            stockRepository.removeStock(productId, baseQuantity, unitId, notes);

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Remove stock");
        }
    }

    /**
     * Adjust stock to a specific quantity with unit conversion.
     */
    public Result<Void> adjustStock(String productId, long quantity, String unitId, String notes) {
        ValidationUtils.ValidationResult productValidation = ValidationUtils.validateNotEmpty(productId, "Product ID");
        if (productValidation.isFailure()) {
            return Result.failure(productValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult unitValidation = ValidationUtils.validateNotEmpty(unitId, "Unit ID");
        if (unitValidation.isFailure()) {
            return Result.failure(unitValidation.getErrorMessage());
        }

        if (quantity < 0) {
            return Result.failure("Kuantitas tidak boleh negatif");
        }

        try {
            // Get unit for conversion
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            // Convert to base unit
            Result<Long> conversionResult = unitService.convertToBaseUnit(quantity, unitId);
            if (conversionResult.isFailure()) {
                return Result.failure(conversionResult.getErrorMessage());
            }

            long baseQuantity = conversionResult.getData();

            // Adjust stock transaction
            stockRepository.adjustStock(productId, baseQuantity, unitId, notes);

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Adjust stock");
        }
    }

    /**
     * Get stock in specific unit (converted from base unit).
     */
    public Result<Long> getStockInUnit(String productId, String unitId) {
        ValidationUtils.ValidationResult productValidation = ValidationUtils.validateNotEmpty(productId, "Product ID");
        if (productValidation.isFailure()) {
            return Result.failure(productValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult unitValidation = ValidationUtils.validateNotEmpty(unitId, "Unit ID");
        if (unitValidation.isFailure()) {
            return Result.failure(unitValidation.getErrorMessage());
        }

        try {
            // Get total stock in base unit
            long baseStock = stockRepository.getTotalStockForProduct(productId);

            // Convert from base unit to target unit
            Result<Long> conversionResult = unitService.convertFromBaseUnit(baseStock, unitId);
            if (conversionResult.isFailure()) {
                return Result.failure(conversionResult.getErrorMessage());
            }

            return Result.success(conversionResult.getData());
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Get stock in unit");
        }
    }

    /**
     * Get all transactions for a product.
     */
    public LiveData<List<StockTransaction>> getTransactionsByProductId(String productId) {
        return stockRepository.getTransactionsByProductId(productId);
    }

    /**
     * Get total stock for a product in base unit.
     */
    public long getTotalStockForProduct(String productId) {
        return stockRepository.getTotalStockForProduct(productId);
    }

    /**
     * Get recent transactions across all products.
     */
    public List<StockTransaction> getRecentTransactions(int limit) {
        return stockRepository.getRecentTransactions(limit);
    }

    /**
     * Validate stock transaction before execution.
     */
    public Result<Void> validateStockTransaction(String productId, StockTransaction.TransactionType type,
                                                 long quantity, String unitId) {
        ValidationUtils.ValidationResult productValidation = ValidationUtils.validateNotEmpty(productId, "Product ID");
        if (productValidation.isFailure()) {
            return Result.failure(productValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult unitValidation = ValidationUtils.validateNotEmpty(unitId, "Unit ID");
        if (unitValidation.isFailure()) {
            return Result.failure(unitValidation.getErrorMessage());
        }

        if (quantity < 0) {
            return Result.failure("Kuantitas tidak boleh negatif");
        }

        try {
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            if (type == StockTransaction.TransactionType.REMOVE) {
                Result<Long> conversionResult = unitService.convertToBaseUnit(quantity, unitId);
                if (conversionResult.isFailure()) {
                    return Result.failure(conversionResult.getErrorMessage());
                }

                long baseQuantity = conversionResult.getData();
                long currentStock = stockRepository.getTotalStockForProduct(productId);

                if (currentStock < baseQuantity) {
                    return Result.failure("Stok tidak mencukupi. Stok tersedia: " + currentStock);
                }
            }

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Validate stock transaction");
        }
    }
}