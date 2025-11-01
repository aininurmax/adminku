package com.bdajaya.adminku.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Base repository class providing common patterns for database operations.
 * This class implements the template method pattern for async operations and provides
 * standardized error handling and logging for all repository operations.
 *
 * @author Adminku Development Team
 * @version 1.0.0
 */
public abstract class BaseRepository {

    // ================================
    // ASYNC OPERATION METHODS
    // ================================

    /**
     * Executes a database read operation asynchronously with proper error handling.
     *
     * @param operation The operation to execute
     * @param operationName The name of the operation for logging
     * @param <T> The return type of the operation
     * @return A Result containing the operation result or error
     */
    protected <T> Result<T> executeReadOperation(@NonNull Callable<T> operation, @NonNull String operationName) {
        try {
            ErrorHandler.logDebug("Starting read operation: " + operationName);

            Future<T> future = com.bdajaya.adminku.data.AppDatabase.databaseWriteExecutor.submit(operation);
            T result = future.get();

            ErrorHandler.logDebug("Read operation completed successfully: " + operationName);
            return Result.success(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMessage = "Read operation interrupted: " + operationName;
            return ErrorHandler.<T>handleDatabaseException(e, operationName)
                    .map(r -> null); // Return failure result

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = "Read operation failed: " + operationName;
            if (cause instanceof Exception) {
                return ErrorHandler.<T>handleDatabaseException((Exception) cause, operationName)
                        .map(r -> null); // Return failure result
            } else {
                return ErrorHandler.<T>handleException(new Exception(cause), operationName)
                        .map(r -> null); // Return failure result
            }

        } catch (Exception e) {
            String errorMessage = "Unexpected error in read operation: " + operationName;
            return ErrorHandler.<T>handleException(e, operationName)
                    .map(r -> null); // Return failure result
        }
    }

    /**
     * Executes a database write operation asynchronously with proper error handling.
     *
     * @param operation The operation to execute
     * @param operationName The name of the operation for logging
     * @param <T> The return type of the operation
     * @return A Result containing the operation result or error
     */
    protected <T> Result<T> executeWriteOperation(@NonNull Callable<T> operation, @NonNull String operationName) {
        try {
            ErrorHandler.logDebug("Starting write operation: " + operationName);

            Future<T> future = com.bdajaya.adminku.data.AppDatabase.databaseWriteExecutor.submit(operation);
            T result = future.get();

            ErrorHandler.logDebug("Write operation completed successfully: " + operationName);
            return Result.success(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMessage = "Write operation interrupted: " + operationName;
            return ErrorHandler.<T>handleDatabaseException(e, operationName)
                    .map(r -> null); // Return failure result

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            String errorMessage = "Write operation failed: " + operationName;
            if (cause instanceof Exception) {
                return ErrorHandler.<T>handleDatabaseException((Exception) cause, operationName)
                        .map(r -> null); // Return failure result
            } else {
                return ErrorHandler.<T>handleException(new Exception(cause), operationName)
                        .map(r -> null); // Return failure result
            }

        } catch (Exception e) {
            String errorMessage = "Unexpected error in write operation: " + operationName;
            return ErrorHandler.<T>handleException(e, operationName)
                    .map(r -> null); // Return failure result
        }
    }

    /**
     * Executes a simple database operation that returns void.
     *
     * @param operation The operation to execute
     * @param operationName The name of the operation for logging
     * @return A Result indicating success or failure
     */
    protected Result<Void> executeVoidOperation(@NonNull Runnable operation, @NonNull String operationName) {
        try {
            ErrorHandler.logDebug("Starting void operation: " + operationName);

            com.bdajaya.adminku.data.AppDatabase.databaseWriteExecutor.execute(operation);

            ErrorHandler.logDebug("Void operation completed successfully: " + operationName);
            return Result.success();

        } catch (Exception e) {
            String errorMessage = "Void operation failed: " + operationName;
            return ErrorHandler.<Void>handleException(e, operationName)
                    .map(r -> null); // Return failure result
        }
    }

    // ================================
    // VALIDATION HELPERS
    // ================================

    /**
     * Validates that an ID is not null or empty.
     *
     * @param id The ID to validate
     * @param fieldName The name of the field for error messages
     * @return A ValidationResult indicating success or failure
     */
    protected ValidationUtils.ValidationResult validateId(@Nullable String id, String fieldName) {
        if (id == null || id.trim().isEmpty()) {
            return ValidationUtils.ValidationResult.failure(fieldName + " tidak boleh kosong");
        }

        return ValidationUtils.ValidationResult.success();
    }

    /**
     * Validates that an entity is not null.
     *
     * @param entity The entity to validate
     * @param entityName The name of the entity for error messages
     * @return A ValidationResult indicating success or failure
     */
    protected ValidationUtils.ValidationResult validateEntity(@Nullable Object entity, String entityName) {
        if (entity == null) {
            return ValidationUtils.ValidationResult.failure(entityName + " tidak boleh null");
        }

        return ValidationUtils.ValidationResult.success();
    }

    // ================================
    // LOGGING HELPERS
    // ================================

    /**
     * Logs the start of an operation.
     *
     * @param operationName The name of the operation
     */
    protected void logOperationStart(String operationName) {
        ErrorHandler.logDebug("Starting operation: " + operationName);
    }

    /**
     * Logs the successful completion of an operation.
     *
     * @param operationName The name of the operation
     */
    protected void logOperationSuccess(String operationName) {
        ErrorHandler.logDebug("Operation completed successfully: " + operationName);
    }

    /**
     * Logs the failure of an operation.
     *
     * @param operationName The name of the operation
     * @param error The error message
     */
    protected void logOperationFailure(String operationName, String error) {
        ErrorHandler.logError(ErrorHandler.ERROR_CODE_DATABASE, "Operation failed: " + operationName + " - " + error);
    }

    // ================================
    // TRANSACTION HELPERS
    // ================================

    /**
     * Executes multiple operations in a transaction-like manner.
     * Note: This is a simplified version. For true transactions, use Room's @Transaction annotation.
     *
     * @param operations The operations to execute
     * @param operationName The name of the combined operation for logging
     * @return A Result indicating overall success or failure
     */
    protected Result<Void> executeTransaction(@NonNull TransactionOperation[] operations, @NonNull String operationName) {
        logOperationStart(operationName);

        try {
            for (int i = 0; i < operations.length; i++) {
                TransactionOperation operation = operations[i];
                String stepName = operationName + " - Step " + (i + 1);

                if (operation instanceof ReadOperation) {
                    ReadOperation<?> readOp = (ReadOperation<?>) operation;
                    Result<?> result = executeReadOperation(readOp.callable, stepName);
                    if (result.isFailure()) {
                        logOperationFailure(operationName, "Failed at step " + (i + 1) + ": " + result.getErrorMessage());
                        return Result.failure("Transaction failed at step " + (i + 1) + ": " + result.getErrorMessage());
                    }
                } else if (operation instanceof WriteOperation) {
                    WriteOperation<?> writeOp = (WriteOperation<?>) operation;
                    Result<?> result = executeWriteOperation(writeOp.callable, stepName);
                    if (result.isFailure()) {
                        logOperationFailure(operationName, "Failed at step " + (i + 1) + ": " + result.getErrorMessage());
                        return Result.failure("Transaction failed at step " + (i + 1) + ": " + result.getErrorMessage());
                    }
                }
            }

            logOperationSuccess(operationName);
            return Result.success();

        } catch (Exception e) {
            logOperationFailure(operationName, e.getMessage());
            return ErrorHandler.<Void>handleException(e, operationName)
                    .map(r -> null); // Return failure result
        }
    }

    // ================================
    // ABSTRACT METHODS
    // ================================

    /**
     * Gets the name of this repository for logging purposes.
     *
     * @return The repository name
     */
    protected abstract String getRepositoryName();

    // ================================
    // INNER CLASSES
    // ================================

    /**
     * Interface for transaction operations that can be read or write operations.
     */
    protected interface TransactionOperation {
        // Marker interface for transaction operations
    }

    /**
     * Represents a read operation in a transaction.
     *
     * @param <T> The return type of the operation
     */
    protected static class ReadOperation<T> implements TransactionOperation {
        final Callable<T> callable;

        ReadOperation(Callable<T> callable) {
            this.callable = callable;
        }
    }

    /**
     * Represents a write operation in a transaction.
     *
     * @param <T> The return type of the operation
     */
    protected static class WriteOperation<T> implements TransactionOperation {
        final Callable<T> callable;

        WriteOperation(Callable<T> callable) {
            this.callable = callable;
        }
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Creates a ReadOperation for use in transactions.
     *
     * @param callable The callable operation
     * @param <T> The return type
     * @return A ReadOperation instance
     */
    protected static <T> ReadOperation<T> readOperation(Callable<T> callable) {
        return new ReadOperation<>(callable);
    }

    /**
     * Creates a WriteOperation for use in transactions.
     *
     * @param callable The callable operation
     * @param <T> The return type
     * @return A WriteOperation instance
     */
    protected static <T> WriteOperation<T> writeOperation(Callable<T> callable) {
        return new WriteOperation<>(callable);
    }

    /**
     * Safely gets the current timestamp.
     *
     * @return The current timestamp in milliseconds
     */
    protected long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Validates and sanitizes input data before processing.
     *
     * @param data The data to validate
     * @param validator The validation function
     * @param <T> The type of data
     * @return A Result with validation result
     */
    protected <T> Result<T> validateInput(T data, InputValidator<T> validator) {
        if (validator == null) {
            return Result.failure("Validator tidak boleh null");
        }

        try {
            ValidationUtils.ValidationResult result = validator.validate(data);
            if (result.isSuccess()) {
                return Result.success(data);
            } else {
                return Result.failure(result.getErrorMessage());
            }
        } catch (Exception e) {
            return ErrorHandler.handleException(e, "Input validation");
        }
    }

    // ================================
    // INTERFACES
    // ================================

    /**
     * Interface for input validation functions.
     *
     * @param <T> The type of data to validate
     */
    protected interface InputValidator<T> {
        ValidationUtils.ValidationResult validate(T data);
    }
}
