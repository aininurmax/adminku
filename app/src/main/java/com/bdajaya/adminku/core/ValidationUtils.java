package com.bdajaya.adminku.core;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Comprehensive input validation utilities for the application.
 * This class provides centralized validation logic to ensure data integrity and security.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public final class ValidationUtils {

    // Prevent instantiation
    private ValidationUtils() {
        throw new AssertionError("ValidationUtils class cannot be instantiated");
    }

    // ================================
    // CATEGORY VALIDATION
    // ================================

    /**
     * Validates a category name.
     *
     * @param name The category name to validate
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateCategoryName(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Nama kategori tidak boleh kosong");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < Constants.MIN_CATEGORY_NAME_LENGTH) {
            return ValidationResult.failure("Nama kategori terlalu pendek (minimal " +
                Constants.MIN_CATEGORY_NAME_LENGTH + " karakter)");
        }

        if (trimmedName.length() > Constants.MAX_CATEGORY_NAME_LENGTH) {
            return ValidationResult.failure("Nama kategori terlalu panjang (maksimal " +
                Constants.MAX_CATEGORY_NAME_LENGTH + " karakter)");
        }

        if (!trimmedName.matches(Constants.PATTERN_CATEGORY_NAME)) {
            return ValidationResult.failure("Nama kategori hanya boleh berisi huruf, angka, spasi, tanda hubung, dan garis bawah");
        }

        return ValidationResult.success();
    }

    /**
     * Validates a category level.
     *
     * @param level The level to validate
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateCategoryLevel(int level) {
        if (!Constants.isValidCategoryLevel(level)) {
            return ValidationResult.failure("Level kategori tidak valid (harus antara " +
                Constants.ROOT_CATEGORY_LEVEL + " dan " + Constants.MAX_CATEGORY_LEVEL + ")");
        }

        return ValidationResult.success();
    }

    /**
     * Validates a parent ID for category creation.
     *
     * @param parentId The parent ID to validate
     * @param parentLevel The level of the parent category
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateParentId(@Nullable String parentId, int parentLevel) {
        // null parentId is valid for root categories
        if (parentId == null) {
            return ValidationResult.success();
        }

        // Validate parent exists (this would need to be checked against database)
        if (parentId.trim().isEmpty()) {
            return ValidationResult.failure("Parent ID tidak valid");
        }

        // Check if adding child would exceed max level
        if (parentLevel + 1 > Constants.MAX_CATEGORY_LEVEL) {
            return ValidationResult.failure(Constants.ERROR_MAX_DEPTH_REACHED);
        }

        return ValidationResult.success();
    }

    /**
     * Validates an icon URL.
     *
     * @param iconUrl The icon URL to validate
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateIconUrl(@Nullable String iconUrl) {
        if (iconUrl == null) {
            return ValidationResult.success(); // null is valid
        }

        if (iconUrl.trim().isEmpty()) {
            return ValidationResult.failure("Icon URL tidak boleh kosong jika diisi");
        }

        if (iconUrl.length() > Constants.MAX_ICON_URL_LENGTH) {
            return ValidationResult.failure("Icon URL terlalu panjang (maksimal " +
                Constants.MAX_ICON_URL_LENGTH + " karakter)");
        }

        if (!Constants.isValidIconUrl(iconUrl)) {
            return ValidationResult.failure("Format Icon URL tidak valid");
        }

        return ValidationResult.success();
    }

    // ================================
    // UNIT VALIDATION
    // ================================

    /**
     * Validates a unit name.
     */
    public static ValidationResult validateUnitName(@Nullable String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.failure("Nama unit tidak boleh kosong");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < Constants.MIN_UNIT_NAME_LENGTH) {
            return ValidationResult.failure("Nama unit terlalu pendek");
        }

        if (trimmedName.length() > Constants.MAX_UNIT_NAME_LENGTH) {
            return ValidationResult.failure("Nama unit terlalu panjang (maksimal " +
                    Constants.MAX_UNIT_NAME_LENGTH + " karakter)");
        }

        return ValidationResult.success();
    }

    /**
     * Validates a conversion factor.
     */
    public static ValidationResult validateConversionFactor(long factor) {
        if (factor <= 0) {
            return ValidationResult.failure("Faktor konversi harus lebih dari 0");
        }

        if (factor > Constants.MAX_CONVERSION_FACTOR) {
            return ValidationResult.failure("Faktor konversi terlalu besar");
        }

        return ValidationResult.success();
    }

    /**
     * Validates a base unit.
     */
    public static ValidationResult validateBaseUnit(@Nullable String baseUnit) {
        if (baseUnit == null || baseUnit.trim().isEmpty()) {
            return ValidationResult.failure("Base unit tidak boleh kosong");
        }

        if (!Constants.isValidBaseUnit(baseUnit)) {
            return ValidationResult.failure("Base unit harus 'pcs' atau 'gr'");
        }

        return ValidationResult.success();
    }

    /**
     * Validates complete unit data.
     */
    public static ValidationResult validateUnitData(@Nullable String name,
                                                    @Nullable String baseUnit,
                                                    long conversionFactor) {
        List<String> errors = new ArrayList<>();

        ValidationResult nameResult = validateUnitName(name);
        if (nameResult.isFailure()) {
            errors.add(nameResult.getErrorMessage());
        }

        ValidationResult baseUnitResult = validateBaseUnit(baseUnit);
        if (baseUnitResult.isFailure()) {
            errors.add(baseUnitResult.getErrorMessage());
        }

        ValidationResult conversionResult = validateConversionFactor(conversionFactor);
        if (conversionResult.isFailure()) {
            errors.add(conversionResult.getErrorMessage());
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(String.join("; ", errors));
        }

        return ValidationResult.success();
    }

    // ================================
    // STOCK VALIDATION
    // ================================

    /**
     * Validates stock quantity.
     */
    public static ValidationResult validateStockQuantity(long quantity) {
        if (quantity < Constants.MIN_STOCK_QUANTITY) {
            return ValidationResult.failure("Kuantitas tidak boleh negatif");
        }

        if (quantity > Constants.MAX_STOCK_QUANTITY) {
            return ValidationResult.failure("Kuantitas terlalu besar");
        }

        return ValidationResult.success();
    }

    /**
     * Validates stock transaction.
     */
    public static ValidationResult validateStockTransaction(String productId,
                                                            String unitId,
                                                            long quantity) {
        List<String> errors = new ArrayList<>();

        ValidationResult productResult = validateNotEmpty(productId, "Product ID");
        if (productResult.isFailure()) {
            errors.add(productResult.getErrorMessage());
        }

        ValidationResult unitResult = validateNotEmpty(unitId, "Unit ID");
        if (unitResult.isFailure()) {
            errors.add(unitResult.getErrorMessage());
        }

        ValidationResult quantityResult = validateStockQuantity(quantity);
        if (quantityResult.isFailure()) {
            errors.add(quantityResult.getErrorMessage());
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(String.join("; ", errors));
        }

        return ValidationResult.success();
    }

    /**
     * Sanitizes unit name.
     */
    @Nullable
    public static String sanitizeUnitName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return name.trim().replaceAll("\\s+", " ");
    }

    // ================================
    // GENERAL VALIDATION
    // ================================

    /**
     * Validates that a string is not null or empty.
     *
     * @param value The string to validate
     * @param fieldName The name of the field for error messages
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateNotEmpty(@Nullable String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return ValidationResult.failure(fieldName + " tidak boleh kosong");
        }

        return ValidationResult.success();
    }

    /**
     * Validates that an object is not null.
     *
     * @param value The object to validate
     * @param fieldName The name of the field for error messages
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateNotNull(Object value, String fieldName) {
        if (value == null) {
            return ValidationResult.failure(fieldName + " tidak boleh null");
        }

        return ValidationResult.success();
    }

    /**
     * Validates that a number is within a specified range.
     *
     * @param value The number to validate
     * @param min The minimum allowed value (inclusive)
     * @param max The maximum allowed value (inclusive)
     * @param fieldName The name of the field for error messages
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            return ValidationResult.failure(fieldName + " harus antara " + min + " dan " + max);
        }

        return ValidationResult.success();
    }

    /**
     * Validates that a string length is within specified bounds.
     *
     * @param value The string to validate
     * @param minLength The minimum allowed length
     * @param maxLength The maximum allowed length
     * @param fieldName The name of the field for error messages
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateLength(@Nullable String value, int minLength, int maxLength, String fieldName) {
        if (value == null) {
            return ValidationResult.failure(fieldName + " tidak boleh null");
        }

        if (value.length() < minLength) {
            return ValidationResult.failure(fieldName + " terlalu pendek (minimal " + minLength + " karakter)");
        }

        if (value.length() > maxLength) {
            return ValidationResult.failure(fieldName + " terlalu panjang (maksimal " + maxLength + " karakter)");
        }

        return ValidationResult.success();
    }

    // ================================
    // BATCH VALIDATION
    // ================================

    /**
     * Validates multiple category fields together.
     *
     * @param name The category name
     * @param parentId The parent ID (can be null for root categories)
     * @param parentLevel The level of the parent category
     * @param iconUrl The icon URL (can be null)
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateCategoryData(@Nullable String name, @Nullable String parentId,
                                                     int parentLevel, @Nullable String iconUrl) {
        List<String> errors = new ArrayList<>();

        // Validate name
        ValidationResult nameResult = validateCategoryName(name);
        if (nameResult.isFailure()) {
            errors.add(nameResult.getErrorMessage());
        }

        // Validate parent ID
        ValidationResult parentResult = validateParentId(parentId, parentLevel);
        if (parentResult.isFailure()) {
            errors.add(parentResult.getErrorMessage());
        }

        // Validate icon URL
        ValidationResult iconResult = validateIconUrl(iconUrl);
        if (iconResult.isFailure()) {
            errors.add(iconResult.getErrorMessage());
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(String.join("; ", errors));
        }

        return ValidationResult.success();
    }

    /**
     * Validates a complete category object.
     *
     * @param category The category to validate
     * @return A ValidationResult indicating success or failure with error message
     */
    public static ValidationResult validateCategory(com.bdajaya.adminku.data.entity.Category category) {
        if (category == null) {
            return ValidationResult.failure("Kategori tidak boleh null");
        }

        List<String> errors = new ArrayList<>();

        // Validate name
        ValidationResult nameResult = validateCategoryName(category.getName());
        if (nameResult.isFailure()) {
            errors.add(nameResult.getErrorMessage());
        }

        // Validate level
        ValidationResult levelResult = validateCategoryLevel(category.getLevel());
        if (levelResult.isFailure()) {
            errors.add(levelResult.getErrorMessage());
        }

        // Validate icon URL
        ValidationResult iconResult = validateIconUrl(category.getIconUrl());
        if (iconResult.isFailure()) {
            errors.add(iconResult.getErrorMessage());
        }

        if (!errors.isEmpty()) {
            return ValidationResult.failure(String.join("; ", errors));
        }

        return ValidationResult.success();
    }

    // ================================
    // SANITIZATION METHODS
    // ================================

    /**
     * Sanitizes a category name by trimming whitespace and normalizing spaces.
     *
     * @param name The name to sanitize
     * @return The sanitized name, or null if input was null
     */
    @Nullable
    public static String sanitizeCategoryName(@Nullable String name) {
        if (name == null) {
            return null;
        }

        return name.trim().replaceAll("\\s+", " ");
    }

    /**
     * Sanitizes an icon URL by trimming whitespace.
     *
     * @param iconUrl The URL to sanitize
     * @return The sanitized URL, or null if input was null or empty
     */
    @Nullable
    public static String sanitizeIconUrl(@Nullable String iconUrl) {
        if (iconUrl == null || iconUrl.trim().isEmpty()) {
            return null;
        }

        return iconUrl.trim();
    }

    /**
     * Normalizes a string by trimming and converting to lowercase.
     *
     * @param value The string to normalize
     * @return The normalized string, or null if input was null
     */
    @Nullable
    public static String normalizeString(@Nullable String value) {
        if (value == null) {
            return null;
        }

        return value.trim().toLowerCase();
    }

    // ================================
    // VALIDATION RESULT CLASS
    // ================================

    /**
     * Represents the result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult failure(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isSuccess() {
            return valid;
        }

        public boolean isFailure() {
            return !valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getErrorMessageOrDefault(String defaultMessage) {
            return errorMessage != null ? errorMessage : defaultMessage;
        }

        @Override
        public String toString() {
            return valid ? "ValidationResult{success}" : "ValidationResult{failure='" + errorMessage + "'}";
        }
    }
}
