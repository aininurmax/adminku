package com.bdajaya.adminku.core;

/**
 * Centralized constants for the entire application.
 * This class eliminates magic numbers and provides a single source of truth for all configuration values.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public final class Constants {

    // Prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class cannot be instantiated");
    }

    // ================================
    // CATEGORY CONSTANTS
    // ================================

    /**
     * Maximum depth level for category hierarchy.
     * Defines how many levels deep the category tree can go.
     */
    public static final int MAX_CATEGORY_LEVEL = 4;

    /**
     * Default category level for root categories.
     */
    public static final int ROOT_CATEGORY_LEVEL = 0;

    /**
     * Search results limit for category queries.
     */
    public static final int CATEGORY_SEARCH_LIMIT = 20;

    // ================================
    // UNIT CONSTANTS
    // ================================

    /**
     * Base unit for countable items.
     */
    public static final String BASE_UNIT_PCS = "pcs";

    /**
     * Base unit for weight-based items.
     */
    public static final String BASE_UNIT_GRAM = "gr";

    /**
     * Maximum length for unit names.
     */
    public static final int MAX_UNIT_NAME_LENGTH = 50;

    /**
     * Minimum length for unit names.
     */
    public static final int MIN_UNIT_NAME_LENGTH = 1;

    /**
     * Maximum conversion factor.
     */
    public static final long MAX_CONVERSION_FACTOR = 1000000;

    // ================================
    // STOCK CONSTANTS
    // ================================

    /**
     * Minimum stock quantity.
     */
    public static final long MIN_STOCK_QUANTITY = 0;

    /**
     * Maximum stock quantity.
     */
    public static final long MAX_STOCK_QUANTITY = Long.MAX_VALUE;

    // ================================
    // ERROR CONSTANTS - UNIT
    // ================================

    /**
     * Error message for invalid unit.
     */
    public static final String ERROR_INVALID_UNIT = "Satuan tidak valid";

    /**
     * Error message for unit not found.
     */
    public static final String ERROR_UNIT_NOT_FOUND = "Satuan tidak ditemukan";

    /**
     * Error message for duplicate unit.
     */
    public static final String ERROR_DUPLICATE_UNIT = "Satuan sudah ada";

    /**
     * Error message for unit in use.
     */
    public static final String ERROR_UNIT_IN_USE = "Satuan masih digunakan";

    /**
     * Error message for invalid conversion.
     */
    public static final String ERROR_INVALID_CONVERSION = "Konversi tidak valid";

    // ================================
    // ERROR CONSTANTS - STOCK
    // ================================

    /**
     * Error message for insufficient stock.
     */
    public static final String ERROR_INSUFFICIENT_STOCK = "Stok tidak mencukupi";

    /**
     * Error message for invalid stock quantity.
     */
    public static final String ERROR_INVALID_STOCK_QUANTITY = "Kuantitas stok tidak valid";

    /**
     * Error message for stock transaction failed.
     */
    public static final String ERROR_STOCK_TRANSACTION_FAILED = "Transaksi stok gagal";

    // ================================
    // UI CONSTANTS
    // ================================

    /**
     * Delay in milliseconds for search debouncing.
     * Prevents excessive API calls while user is typing.
     */
    public static final long SEARCH_DEBOUNCE_DELAY_MS = 300;

    /**
     * Animation duration for breadcrumb navigation.
     */
    public static final long BREADCRUMB_NAVIGATION_DELAY_MS = 100;

    // ================================
    // DATABASE CONSTANTS
    // ================================

    /**
     * Database version for schema migrations.
     */
    public static final int DATABASE_VERSION = 1;

    /**
     * Thread pool size for database write operations.
     */
    public static final int DATABASE_THREAD_POOL_SIZE = 4;

    // ================================
    // VALIDATION METHODS
    // ================================

    /**
     * Checks if a unit name is valid.
     */
    public static boolean isValidUnitName(String name) {
        return name != null
                && name.length() >= MIN_UNIT_NAME_LENGTH
                && name.length() <= MAX_UNIT_NAME_LENGTH;
    }

    /**
     * Checks if a conversion factor is valid.
     */
    public static boolean isValidConversionFactor(long factor) {
        return factor > 0 && factor <= MAX_CONVERSION_FACTOR;
    }

    /**
     * Checks if a stock quantity is valid.
     */
    public static boolean isValidStockQuantity(long quantity) {
        return quantity >= MIN_STOCK_QUANTITY && quantity <= MAX_STOCK_QUANTITY;
    }

    /**
     * Checks if a base unit is valid.
     */
    public static boolean isValidBaseUnit(String baseUnit) {
        return BASE_UNIT_PCS.equals(baseUnit) || BASE_UNIT_GRAM.equals(baseUnit);
    }

    // ================================
    // VALIDATION CONSTANTS
    // ================================

    /**
     * Maximum length for category names.
     */
    public static final int MAX_CATEGORY_NAME_LENGTH = 100;

    /**
     * Minimum length for category names.
     */
    public static final int MIN_CATEGORY_NAME_LENGTH = 1;

    /**
     * Maximum length for icon URLs.
     */
    public static final int MAX_ICON_URL_LENGTH = 500;

    // ================================
    // ERROR CONSTANTS
    // ================================

    /**
     * Generic error message for unexpected errors.
     */
    public static final String ERROR_UNEXPECTED = "Terjadi kesalahan yang tidak terduga";

    /**
     * Error message for duplicate category names.
     */
    public static final String ERROR_DUPLICATE_CATEGORY = "Nama kategori sudah ada";

    /**
     * Error message for maximum depth reached.
     */
    public static final String ERROR_MAX_DEPTH_REACHED = "Tidak bisa menambah subkategori lagi";

    /**
     * Error message for category not found.
     */
    public static final String ERROR_CATEGORY_NOT_FOUND = "Kategori tidak ditemukan";

    /**
     * Error message for category has children.
     */
    public static final String ERROR_CATEGORY_HAS_CHILDREN = "Kategori memiliki subkategori";

    /**
     * Error message for category has products.
     */
    public static final String ERROR_CATEGORY_HAS_PRODUCTS = "Kategori memiliki produk terkait";

    /**
     * Error message for invalid input.
     */
    public static final String ERROR_INVALID_INPUT = "Input tidak valid";

    // ================================
    // SUCCESS CONSTANTS
    // ================================

    /**
     * Success message for category operations.
     */
    public static final String SUCCESS_CATEGORY_OPERATION = "Operasi kategori berhasil";

    /**
     * Success message for category deletion.
     */
    public static final String SUCCESS_CATEGORY_DELETED = "Kategori berhasil dihapus";

    /**
     * Success message for category update.
     */
    public static final String SUCCESS_CATEGORY_UPDATED = "Kategori berhasil diperbarui";

    // ================================
    // BUNDLE KEYS
    // ================================

    /**
     * Key for category ID in Intent extras.
     */
    public static final String EXTRA_CATEGORY_ID = "categoryId";

    /**
     * Key for category name in Intent extras.
     */
    public static final String EXTRA_CATEGORY_NAME = "categoryName";

    /**
     * Key for category path in Intent extras.
     */
    public static final String EXTRA_CATEGORY_PATH = "pathString";

    /**
     * Key for category level in Intent extras.
     */
    public static final String EXTRA_CATEGORY_LEVEL = "categoryLevel";

    // ================================
    // REGEX PATTERNS
    // ================================

    /**
     * Regex pattern for validating category names.
     * Allows alphanumeric characters, spaces, hyphens, and underscores.
     */
    public static final String PATTERN_CATEGORY_NAME = "^[a-zA-Z0-9\\s\\-_]{1,100}$";

    /**
     * Regex pattern for validating icon URLs.
     * Basic URL validation pattern.
     */
    public static final String PATTERN_ICON_URL = "^https?://.*";

    // ================================
    // ANIMATION CONSTANTS
    // ================================

    /**
     * Duration for fade animations in milliseconds.
     */
    public static final long ANIMATION_FADE_DURATION_MS = 200;

    /**
     * Duration for slide animations in milliseconds.
     */
    public static final long ANIMATION_SLIDE_DURATION_MS = 300;

    // ================================
    // MEMORY CONSTANTS
    // ================================

    /**
     * Maximum number of items to load at once in RecyclerView.
     */
    public static final int RECYCLERVIEW_PAGE_SIZE = 50;

    /**
     * Cache size for image loading.
     */
    public static final int IMAGE_CACHE_SIZE = 10 * 1024 * 1024; // 10MB

    // ================================
    // NETWORK CONSTANTS
    // ================================

    /**
     * Connection timeout in milliseconds.
     */
    public static final int NETWORK_TIMEOUT_MS = 30000;

    /**
     * Read timeout in milliseconds.
     */
    public static final int NETWORK_READ_TIMEOUT_MS = 30000;

    // ================================
    // FILE PATHS
    // ================================

    /**
     * Base path for application assets.
     */
    public static final String ASSETS_BASE_PATH = "file:///android_asset/";

    /**
     * Path for category icons.
     */
    public static final String CATEGORY_ICONS_PATH = ASSETS_BASE_PATH + "img/categories/";

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Checks if a category level is valid.
     *
     * @param level The level to validate
     * @return true if the level is within acceptable range
     */
    public static boolean isValidCategoryLevel(int level) {
        return level >= ROOT_CATEGORY_LEVEL && level <= MAX_CATEGORY_LEVEL;
    }

    /**
     * Checks if a category name is valid.
     *
     * @param name The name to validate
     * @return true if the name meets validation criteria
     */
    public static boolean isValidCategoryName(String name) {
        return name != null
                && name.length() >= MIN_CATEGORY_NAME_LENGTH
                && name.length() <= MAX_CATEGORY_NAME_LENGTH
                && name.matches(PATTERN_CATEGORY_NAME);
    }

    /**
     * Checks if an icon URL is valid.
     *
     * @param url The URL to validate
     * @return true if the URL is valid or null
     */
    public static boolean isValidIconUrl(String url) {
        return url == null || (url.length() <= MAX_ICON_URL_LENGTH && url.matches(PATTERN_ICON_URL));
    }
}
