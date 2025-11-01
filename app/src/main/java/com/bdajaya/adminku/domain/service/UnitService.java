package com.bdajaya.adminku.domain.service;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.bdajaya.adminku.core.Constants;
import com.bdajaya.adminku.core.ErrorHandler;
import com.bdajaya.adminku.core.Result;
import com.bdajaya.adminku.core.ValidationUtils;
import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.entity.Unit;
import com.bdajaya.adminku.data.repository.UnitRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnitService {
    private final UnitRepository unitRepository;

    // Base units
    public static final String BASE_UNIT_PCS = "pcs";
    public static final String BASE_UNIT_GRAM = "gr";

    public UnitService(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
        initializeDefaultUnits();
    }

    private void initializeDefaultUnits() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Unit> existingUnits = unitRepository.getAllUnitsSync();
            if (existingUnits.isEmpty()) {
                // Create default pcs unit
                unitRepository.addUnit("pcs", BASE_UNIT_PCS, 1, true);
                // Create default gram unit
                unitRepository.addUnit("gr", BASE_UNIT_GRAM, 1, true);
            }
        });
    }

    public Result<String> addUnit(String code, String name, String baseUnit, long conversionFactor) {
        // Validate inputs
        ValidationUtils.ValidationResult codeValidation = ValidationUtils.validateNotEmpty(code, "Kode unit");
        if (codeValidation.isFailure()) {
            return Result.failure(codeValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult nameValidation = ValidationUtils.validateNotEmpty(name, "Nama unit");
        if (nameValidation.isFailure()) {
            return Result.failure(nameValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult baseUnitValidation = validateBaseUnit(baseUnit);
        if (baseUnitValidation.isFailure()) {
            return Result.failure(baseUnitValidation.getErrorMessage());
        }

        if (conversionFactor <= 0) {
            return Result.failure("Faktor konversi harus lebih dari 0");
        }

        try {
            // Check if unit with same name already exists
            Unit existingUnit = unitRepository.getUnitByName(name);
            if (existingUnit != null) {
                return Result.failure("Satuan dengan nama '" + name + "' sudah ada");
            }

            // Add unit
            boolean isBaseUnit = conversionFactor == 1 &&
                    (baseUnit.equals(BASE_UNIT_PCS) || baseUnit.equals(BASE_UNIT_GRAM));
            String unitId = unitRepository.addUnit(name, baseUnit, conversionFactor, isBaseUnit);

            if (unitId == null) {
                return Result.failure("Gagal menambahkan satuan");
            }

            return Result.success(unitId);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Add unit");
        }
    }

    public Result<Void> updateUnit(String id, String name, long conversionFactor) {
        ValidationUtils.ValidationResult idValidation = ValidationUtils.validateNotEmpty(id, "ID unit");
        if (idValidation.isFailure()) {
            return Result.failure(idValidation.getErrorMessage());
        }

        ValidationUtils.ValidationResult nameValidation = ValidationUtils.validateNotEmpty(name, "Nama unit");
        if (nameValidation.isFailure()) {
            return Result.failure(nameValidation.getErrorMessage());
        }

        if (conversionFactor <= 0) {
            return Result.failure("Faktor konversi harus lebih dari 0");
        }

        try {
            Unit unit = unitRepository.getUnitByIdSync(id);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            // Base units cannot change conversion factor
            if (unit.isBaseUnit() && conversionFactor != 1) {
                return Result.failure("Satuan dasar tidak dapat mengubah faktor konversi");
            }

            boolean success = unitRepository.updateUnit(id, name, conversionFactor);
            if (!success) {
                return Result.failure("Gagal mengupdate satuan");
            }

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Update unit");
        }
    }

    public Result<Void> deleteUnit(String id) {
        ValidationUtils.ValidationResult validation = ValidationUtils.validateNotEmpty(id, "ID unit");
        if (validation.isFailure()) {
            return Result.failure(validation.getErrorMessage());
        }

        try {
            Unit unit = unitRepository.getUnitByIdSync(id);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            // Cannot delete base units
            if (unit.isBaseUnit()) {
                return Result.failure("Satuan dasar tidak dapat dihapus");
            }

            boolean success = unitRepository.deleteUnit(id);
            if (!success) {
                return Result.failure("Gagal menghapus satuan");
            }

            return Result.success();
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Delete unit");
        }
    }

    public LiveData<List<Unit>> getAllUnits() {
        return unitRepository.getAllUnits();
    }

    public LiveData<List<Unit>> searchUnitsLive(String query) {
        MutableLiveData<List<Unit>> result = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Unit> allUnits = unitRepository.getAllUnitsSync();
            List<Unit> filtered = allUnits.stream()
                    .filter(unit -> unit.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
            result.postValue(filtered);
        });
        return result;
    }

    public List<Unit> getUnitsByBaseUnit(String baseUnit) {
        return unitRepository.getUnitsByBaseUnit(baseUnit);
    }

    public Result<Long> convertToBaseUnit(long quantity, String unitId) {
        try {
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            long baseQuantity = quantity * unit.getConversionFactor();
            return Result.success(baseQuantity);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Convert to base unit");
        }
    }

    public Result<Long> convertFromBaseUnit(long baseQuantity, String unitId) {
        try {
            Unit unit = unitRepository.getUnitByIdSync(unitId);
            if (unit == null) {
                return Result.failure("Satuan tidak ditemukan");
            }

            if (unit.getConversionFactor() == 0) {
                return Result.failure("Faktor konversi tidak valid");
            }

            long quantity = baseQuantity / unit.getConversionFactor();
            return Result.success(quantity);
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Convert from base unit");
        }
    }

    private ValidationUtils.ValidationResult validateBaseUnit(String baseUnit) {
        if (baseUnit == null || baseUnit.trim().isEmpty()) {
            return ValidationUtils.ValidationResult.failure("Base unit tidak boleh kosong");
        }

        if (!baseUnit.equals(BASE_UNIT_PCS) && !baseUnit.equals(BASE_UNIT_GRAM)) {
            return ValidationUtils.ValidationResult.failure("Base unit harus 'pcs' atau 'gr'");
        }

        return ValidationUtils.ValidationResult.success();
    }
}