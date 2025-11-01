package com.bdajaya.adminku.core;

import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.bdajaya.adminku.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized error handling system for the application.
 * This class provides consistent error handling, logging, and user-friendly error messages.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public final class ErrorHandler {

    public static final String ERROR_CODE_DELETION = "DELETION_ERROR";
    private static final String TAG = "AdminkuErrorHandler";

    // Error code constants
    public static final String ERROR_CODE_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_CODE_DATABASE = "DATABASE_ERROR";
    public static final String ERROR_CODE_NETWORK = "NETWORK_ERROR";
    public static final String ERROR_CODE_PERMISSION = "PERMISSION_ERROR";
    public static final String ERROR_CODE_IO = "IO_ERROR";
    public static final String ERROR_CODE_UNKNOWN = "UNKNOWN_ERROR";

    // Prevent instantiation
    private ErrorHandler() {
        throw new AssertionError("ErrorHandler class cannot be instantiated");
    }

    // ================================
    // ERROR LOGGING
    // ================================

    /**
     * Logs an error with detailed information.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param exception The exception (can be null)
     * @param context Additional context information
     */
    public static void logError(String errorCode, String message, @Nullable Exception exception, @Nullable String context) {
        String fullMessage = buildFullMessage(errorCode, message, context);

        if (exception != null) {
            Log.e(TAG, fullMessage, exception);
        } else {
            Log.e(TAG, fullMessage);
        }
    }

    /**
     * Logs an error with detailed information.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param exception The exception (can be null)
     */
    public static void logError(String errorCode, String message, @Nullable Exception exception) {
        logError(errorCode, message, exception, null);
    }

    /**
     * Logs an error with detailed information.
     *
     * @param errorCode The error code
     * @param message The error message
     */
    public static void logError(String errorCode, String message) {
        logError(errorCode, message, null, null);
    }

    /**
     * Logs a warning message.
     *
     * @param message The warning message
     * @param context Additional context information
     */
    public static void logWarning(String message, @Nullable String context) {
        String fullMessage = context != null ? message + " | Context: " + context : message;
        Log.w(TAG, fullMessage);
    }

    /**
     * Logs an info message.
     *
     * @param message The info message
     * @param context Additional context information
     */
    public static void logInfo(String message, @Nullable String context) {
        String fullMessage = context != null ? message + " | Context: " + context : message;
        Log.i(TAG, fullMessage);
    }

    /**
     * Logs a debug message.
     *
     * @param message The debug message
     * @param context Additional context information
     */
    public static void logDebug(String message, @Nullable String context) {
        String fullMessage = context != null ? message + " | Context: " + context : message;
        Log.d(TAG, fullMessage);
    }

    // ================================
    // ERROR MESSAGE MAPPING
    // ================================

    private static final Map<String, Integer> ERROR_MESSAGE_MAP = new HashMap<>();

    static {
        // Initialize error message mappings
        ERROR_MESSAGE_MAP.put(ERROR_CODE_VALIDATION, R.string.error_validation);
        ERROR_MESSAGE_MAP.put(ERROR_CODE_DATABASE, R.string.error_database);
        ERROR_MESSAGE_MAP.put(ERROR_CODE_NETWORK, R.string.error_network);
        ERROR_MESSAGE_MAP.put(ERROR_CODE_PERMISSION, R.string.error_permission);
        ERROR_MESSAGE_MAP.put(ERROR_CODE_IO, R.string.error_io);
        ERROR_MESSAGE_MAP.put(ERROR_CODE_UNKNOWN, R.string.error_unknown);
    }

    /**
     * Gets a user-friendly error message for the given error code.
     *
     * @param context The Android context
     * @param errorCode The error code
     * @param defaultMessage The default message if no mapping found
     * @return A user-friendly error message
     */
    public static String getUserFriendlyMessage(Context context, String errorCode, String defaultMessage) {
        Integer stringResId = ERROR_MESSAGE_MAP.get(errorCode);
        if (stringResId != null) {
            try {
                return context.getString(stringResId);
            } catch (Exception e) {
                logError(ERROR_CODE_UNKNOWN, "Failed to get string resource for error code: " + errorCode, e);
            }
        }

        return defaultMessage != null ? defaultMessage : Constants.ERROR_UNEXPECTED;
    }

    /**
     * Gets a user-friendly error message for the given error code.
     *
     * @param context The Android context
     * @param errorCode The error code
     * @return A user-friendly error message
     */
    public static String getUserFriendlyMessage(Context context, String errorCode) {
        return getUserFriendlyMessage(context, errorCode, Constants.ERROR_UNEXPECTED);
    }

    // ================================
    // EXCEPTION HANDLING
    // ================================

    /**
     * Handles an exception and returns a user-friendly Result.
     *
     * @param exception The exception to handle
     * @param context Additional context for logging
     * @param <T> The expected return type
     * @return A Result with failure information
     */
    public static <T> Result<T> handleException(Exception exception, @Nullable String context) {
        String errorCode = classifyException(exception);
        String message = getExceptionMessage(exception);

        logError(errorCode, message, exception, context);

        return Result.failure(message, exception);
    }

    /**
     * Handles an exception and returns a user-friendly Result.
     *
     * @param exception The exception to handle
     * @param <T> The expected return type
     * @return A Result with failure information
     */
    public static <T> Result<T> handleException(Exception exception) {
        return handleException(exception, null);
    }

    /**
     * Handles a database exception specifically.
     *
     * @param exception The database exception
     * @param operation The operation that failed
     * @param <T> The expected return type
     * @return A Result with failure information
     */
    public static <T> Result<T> handleDatabaseException(Exception exception, String operation) {
        String context = "Database operation: " + operation;
        return handleException(exception, context);
    }

    /**
     * Handles a validation exception specifically.
     *
     * @param message The validation error message
     * @param <T> The expected return type
     * @return A Result with validation failure information
     */
    public static <T> Result<T> handleValidationError(String message) {
        logError(ERROR_CODE_VALIDATION, message);
        return Result.failure(message);
    }

    // ================================
    // EXCEPTION CLASSIFICATION
    // ================================

    /**
     * Classifies an exception into an error code.
     *
     * @param exception The exception to classify
     * @return The corresponding error code
     */
    private static String classifyException(Exception exception) {
        if (exception == null) {
            return ERROR_CODE_UNKNOWN;
        }

        String className = exception.getClass().getSimpleName().toLowerCase();

        if (className.contains("sqlite") || className.contains("database")) {
            return ERROR_CODE_DATABASE;
        }

        if (className.contains("io") || className.contains("file")) {
            return ERROR_CODE_IO;
        }

        if (className.contains("network") || className.contains("connect") || className.contains("timeout")) {
            return ERROR_CODE_NETWORK;
        }

        if (className.contains("security") || className.contains("permission")) {
            return ERROR_CODE_PERMISSION;
        }

        if (className.contains("validation") || className.contains("illegalargument")) {
            return ERROR_CODE_VALIDATION;
        }

        return ERROR_CODE_UNKNOWN;
    }

    /**
     * Gets a user-friendly message from an exception.
     *
     * @param exception The exception
     * @return A user-friendly error message
     */
    private static String getExceptionMessage(Exception exception) {
        if (exception == null) {
            return Constants.ERROR_UNEXPECTED;
        }

        String message = exception.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }

        return Constants.ERROR_UNEXPECTED;
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Builds a full error message with all available information.
     *
     * @param errorCode The error code
     * @param message The error message
     * @param context Additional context
     * @return The full error message
     */
    private static String buildFullMessage(String errorCode, String message, @Nullable String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(errorCode).append("] ").append(message);

        if (context != null && !context.trim().isEmpty()) {
            sb.append(" | Context: ").append(context);
        }

        return sb.toString();
    }

    /**
     * Checks if an error is recoverable.
     *
     * @param errorCode The error code
     * @return true if the error is likely recoverable
     */
    public static boolean isRecoverableError(String errorCode) {
        switch (errorCode) {
            case ERROR_CODE_NETWORK:
            case ERROR_CODE_IO:
                return true;
            case ERROR_CODE_DATABASE:
            case ERROR_CODE_VALIDATION:
            case ERROR_CODE_PERMISSION:
            case ERROR_CODE_UNKNOWN:
            default:
                return false;
        }
    }

    /**
     * Gets the appropriate error code for a category operation error.
     *
     * @param error The error message from category operation
     * @return The corresponding error code
     */
    public static String getCategoryOperationErrorCode(String error) {
        if (error == null) {
            return ERROR_CODE_UNKNOWN;
        }

        if (error.contains("already exists") || error.contains("sudah ada")) {
            return ERROR_CODE_VALIDATION;
        }

        if (error.contains("MAX_DEPTH") || error.contains("depth")) {
            return ERROR_CODE_VALIDATION;
        }

        if (error.contains("not found") || error.contains("tidak ditemukan")) {
            return ERROR_CODE_DATABASE;
        }

        if (error.contains("children") || error.contains("produk")) {
            return ERROR_CODE_VALIDATION;
        }

        return ERROR_CODE_DATABASE;
    }

    public static void logDebug(String s) {
        Log.d(TAG, s);
    }

    // ================================
    // CUSTOM EXCEPTIONS
    // ================================

    /**
     * Custom exception for validation errors.
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }

        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom exception for database errors.
     */
    public static class DatabaseException extends Exception {
        public DatabaseException(String message) {
            super(message);
        }

        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Custom exception for business logic errors.
     */
    public static class BusinessLogicException extends Exception {
        public BusinessLogicException(String message) {
            super(message);
        }

        public BusinessLogicException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
