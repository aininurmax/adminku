package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;
import com.bdajaya.adminku.core.BaseRepository;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.UnitDao;
import com.bdajaya.adminku.data.entity.Unit;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Repository for Unit entity with enhanced CRUD operations.
 * Extends BaseRepository for standardized error handling and async operations.
 */
public class UnitRepository extends BaseRepository {
    private final UnitDao unitDao;

    public UnitRepository(UnitDao unitDao) {
        this.unitDao = unitDao;
    }

    @Override
    protected String getRepositoryName() {
        return "UnitRepository";
    }

    // ================================
    // READ OPERATIONS
    // ================================

    public LiveData<List<Unit>> getAllUnits() {
        return unitDao.getAll();
    }

    public List<Unit> getAllUnitsSync() {
        return unitDao.getAllSync();
    }

    public LiveData<Unit> getUnitById(String id) {
        return unitDao.getByIdLive(id);
    }

    public Unit getUnitByIdSync(String id) {
        return unitDao.getById(id);
    }

    public Unit getUnitByName(String name) {
        return unitDao.getByName(name);
    }

    public List<Unit> getBaseUnits() {
        return unitDao.getBaseUnits();
    }

    public List<Unit> getUnitsByBaseUnit(String baseUnitName) {
        return unitDao.getByBaseUnit(baseUnitName);
    }

    public List<Unit> searchUnits(String query) {
        return unitDao.search(query);
    }

    // ================================
    // CREATE OPERATIONS
    // ================================

    /**
     * Add a new unit with validation.
     * Returns the ID of the created unit or null if failed.
     */
    public String addUnit(String name, String baseUnit, long conversionFactor, boolean isBaseUnit) {
        try {
            Future<String> future = AppDatabase.databaseWriteExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    // Check if name already exists
                    if (unitDao.countByName(name) > 0) {
                        logOperationFailure("addUnit", "Duplicate unit name: " + name);
                        return null;
                    }

                    // Prevent duplicate base units with same conversion
                    Unit existing = unitDao.findByBaseUnitAndConversion(baseUnit, conversionFactor);
                    if (existing != null && !existing.getName().equals(name)) {
                        logOperationFailure("addUnit", "Unit with same conversion already exists");
                        return null;
                    }

                    // Create and insert new unit
                    String id = UUID.randomUUID().toString();
                    long now = System.currentTimeMillis();

                    Unit unit = new Unit(
                            id,
                            name,
                            baseUnit,
                            conversionFactor,
                            isBaseUnit,
                            now,
                            now
                    );

                    unitDao.insert(unit);
                    logOperationSuccess("addUnit: " + name);
                    return id;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("addUnit", e.getMessage());
            return null;
        }
    }

    /**
     * Initialize default units if not exists.
     */
    public void initializeDefaultUnits() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Unit> existingUnits = unitDao.getAllSync();
            if (existingUnits.isEmpty()) {
                long now = System.currentTimeMillis();

                // Create pcs (pieces) as base unit
                Unit pcs = new Unit(
                        UUID.randomUUID().toString(),
                        "pcs",
                        "pcs",
                        1,
                        true,
                        now,
                        now
                );

                // Create gr (gram) as base unit
                Unit gr = new Unit(
                        UUID.randomUUID().toString(),
                        "gr",
                        "gr",
                        1,
                        true,
                        now,
                        now
                );

                unitDao.insert(pcs);
                unitDao.insert(gr);

                logOperationSuccess("initializeDefaultUnits");
            }
        });
    }

    // ================================
    // UPDATE OPERATIONS
    // ================================

    /**
     * Update an existing unit.
     * Returns true if successful, false otherwise.
     */
    public boolean updateUnit(String id, String name, long conversionFactor) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Unit unit = unitDao.getById(id);
                    if (unit == null) {
                        logOperationFailure("updateUnit", "Unit not found: " + id);
                        return false;
                    }

                    // Check if name already exists (excluding current unit)
                    if (!unit.getName().equals(name) && unitDao.countByNameExcludingId(name, id) > 0) {
                        logOperationFailure("updateUnit", "Duplicate unit name: " + name);
                        return false;
                    }

                    // Base units cannot change conversion factor
                    if (unit.isBaseUnit() && conversionFactor != 1) {
                        logOperationFailure("updateUnit", "Cannot change base unit conversion factor");
                        return false;
                    }

                    unit.setName(name);
                    if (!unit.isBaseUnit()) {
                        unit.setConversionFactor(conversionFactor);
                    }
                    unit.setUpdatedAt(System.currentTimeMillis());

                    unitDao.update(unit);
                    logOperationSuccess("updateUnit: " + name);
                    return true;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("updateUnit", e.getMessage());
            return false;
        }
    }

    // ================================
    // DELETE OPERATIONS
    // ================================

    /**
     * Delete a unit.
     * Returns true if successful, false otherwise.
     * Base units cannot be deleted.
     */
    public boolean deleteUnit(String id) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Unit unit = unitDao.getById(id);
                    if (unit == null) {
                        logOperationFailure("deleteUnit", "Unit not found: " + id);
                        return false;
                    }

                    // Base units cannot be deleted
                    if (unit.isBaseUnit()) {
                        logOperationFailure("deleteUnit", "Cannot delete base unit");
                        return false;
                    }

                    // Check if unit is in use by products
                    int productCount = unitDao.countProductsByUnitId(id);
                    if (productCount > 0) {
                        logOperationFailure("deleteUnit", "Unit in use by " + productCount + " products");
                        return false;
                    }

                    unitDao.delete(unit);
                    logOperationSuccess("deleteUnit: " + unit.getName());
                    return true;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("deleteUnit", e.getMessage());
            return false;
        }
    }

    // ================================
    // VALIDATION OPERATIONS
    // ================================

    /**
     * Check if a unit is in use by any products.
     */
    public boolean isUnitInUse(String unitId) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(
                    () -> unitDao.countProductsByUnitId(unitId) > 0
            );
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("isUnitInUse", e.getMessage());
            return false;
        }
    }

    /**
     * Check if unit name exists.
     */
    public boolean isUnitNameExists(String name) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(
                    () -> unitDao.countByName(name) > 0
            );
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("isUnitNameExists", e.getMessage());
            return false;
        }
    }

    /**
     * Validate unit compatibility (same base unit).
     */
    public boolean areUnitsCompatible(String unitId1, String unitId2) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(() -> {
                Unit unit1 = unitDao.getById(unitId1);
                Unit unit2 = unitDao.getById(unitId2);

                if (unit1 == null || unit2 == null) {
                    return false;
                }

                return unit1.getBaseUnit().equals(unit2.getBaseUnit());
            });
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            logOperationFailure("areUnitsCompatible", e.getMessage());
            return false;
        }
    }

    // ================================
    // CONVERSION OPERATIONS
    // ================================

    /**
     * Convert quantity from one unit to another.
     * Both units must have the same base unit.
     */
    public Result<Long> convertBetweenUnits(long quantity, String fromUnitId, String toUnitId) {
        return executeReadOperation(() -> {
            Unit fromUnit = unitDao.getById(fromUnitId);
            Unit toUnit = unitDao.getById(toUnitId);

            if (fromUnit == null || toUnit == null) {
                throw new IllegalArgumentException("Unit not found");
            }

            if (!fromUnit.getBaseUnit().equals(toUnit.getBaseUnit())) {
                throw new IllegalArgumentException("Units are not compatible");
            }

            // Convert to base unit first
            long baseQuantity = quantity * fromUnit.getConversionFactor();

            // Then convert to target unit
            long targetQuantity = baseQuantity / toUnit.getConversionFactor();

            return targetQuantity;
        }, "convertBetweenUnits");
    }
}