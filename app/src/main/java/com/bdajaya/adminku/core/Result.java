package com.bdajaya.adminku.core;

/**
 * Generic result wrapper for operations that can either succeed or fail.
 * This class provides a standardized way to handle operation results throughout the application,
 * improving error handling and making the code more robust.
 *
 * @param <T> The type of the success value
 * @author Adminku Development Team
 * @version 1.0.0
 */
public class Result<T> {

    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final String errorCode;
    private final Exception exception;

    // Private constructor to enforce factory methods
    private Result(boolean success, T data, String errorMessage, String errorCode, Exception exception) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.exception = exception;
    }

    // ================================
    // FACTORY METHODS
    // ================================

    /**
     * Creates a successful result with data.
     *
     * @param data The success data
     * @param <T> The type of the data
     * @return A successful Result instance
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, data, null, null, null);
    }

    /**
     * Creates a successful result without data.
     *
     * @param <T> The type of the expected data
     * @return A successful Result instance
     */
    public static <T> Result<T> success() {
        return new Result<>(true, null, null, null, null);
    }

    /**
     * Creates a failed result with an error message.
     *
     * @param errorMessage The error message
     * @param <T> The type of the expected data
     * @return A failed Result instance
     */
    public static <T> Result<T> failure(String errorMessage) {
        return new Result<>(false, null, errorMessage, null, null);
    }

    /**
     * Creates a failed result with an error message and exception.
     *
     * @param errorMessage The error message
     * @param exception The underlying exception
     * @param <T> The type of the expected data
     * @return A failed Result instance
     */
    public static <T> Result<T> failure(String errorMessage, Exception exception) {
        return new Result<>(false, null, errorMessage, null, exception);
    }

    /**
     * Creates a failed result with an exception only.
     *
     * @param exception The underlying exception
     * @param <T> The type of the expected data
     * @return A failed Result instance
     */
    public static <T> Result<T> failure(Exception exception) {
        return new Result<>(false, null, exception.getMessage(), null, exception);
    }

    /**
     * Creates a failed result with an error message and error code.
     *
     * @param errorMessage The error message
     * @param errorCode The error code
     * @param <T> The type of the expected data
     * @return A failed Result instance
     */
    public static <T> Result<T> failure(String errorMessage, String errorCode) {
        return new Result<>(false, null, errorMessage, errorCode, null);
    }

    /**
     * Creates a failed result with an error message, error code, and exception.
     *
     * @param errorMessage The error message
     * @param errorCode The error code
     * @param exception The underlying exception
     * @param <T> The type of the expected data
     * @return A failed Result instance
     */
    public static <T> Result<T> failure(String errorMessage, String errorCode, Exception exception) {
        return new Result<>(false, null, errorMessage, errorCode, exception);
    }

    // ================================
    // GETTER METHODS
    // ================================

    /**
     * Checks if the operation was successful.
     *
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Checks if the operation failed.
     *
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * Gets the success data.
     * Should only be called if {@link #isSuccess()} returns true.
     *
     * @return The success data, or null if no data
     * @throws IllegalStateException if called on a failed result
     */
    public T getData() {
        if (!success) {
            throw new IllegalStateException("Cannot get data from a failed result");
        }
        return data;
    }

    /**
     * Gets the success data or a default value.
     *
     * @param defaultValue The default value to return if result is failure or data is null
     * @return The success data or the default value
     */
    public T getDataOrDefault(T defaultValue) {
        return success && data != null ? data : defaultValue;
    }

    /**
     * Gets the error message.
     * Should only be called if {@link #isFailure()} returns true.
     *
     * @return The error message, or null if no message
     * @throws IllegalStateException if called on a successful result
     */
    public String getErrorMessage() {
        if (success) {
            throw new IllegalStateException("Cannot get error message from a successful result");
        }
        return errorMessage;
    }

    /**
     * Gets the error message or a default value.
     *
     * @param defaultMessage The default message to return if result is success or no message
     * @return The error message or the default message
     */
    public String getErrorMessageOrDefault(String defaultMessage) {
        return !success && errorMessage != null ? errorMessage : defaultMessage;
    }

    /**
     * Gets the underlying exception.
     *
     * @return The exception, or null if no exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Gets the error code.
     *
     * @return The error code, or null if no error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Executes a function if the result is successful.
     *
     * @param successFunction The function to execute with the data
     * @return This result instance for method chaining
     */
    public Result<T> onSuccess(SuccessCallback<T> successFunction) {
        if (success && successFunction != null) {
            successFunction.onSuccess(data);
        }
        return this;
    }

    /**
     * Executes a function if the result is a failure.
     *
     * @param failureFunction The function to execute with the error
     * @return This result instance for method chaining
     */
    public Result<T> onFailure(FailureCallback failureFunction) {
        if (!success && failureFunction != null) {
            failureFunction.onFailure(errorMessage, exception);
        }
        return this;
    }

    /**
     * Maps the success data to a new type if successful.
     *
     * @param mapper The mapping function
     * @param <R> The new result type
     * @return A new Result with mapped data, or a failure if original was a failure
     */
    public <R> Result<R> map(SuccessMapper<T, R> mapper) {
        if (!success) {
            return Result.failure(errorMessage, exception);
        }

        if (mapper == null) {
            return Result.failure("Mapper function cannot be null");
        }

        try {
            R mappedData = mapper.map(data);
            return Result.success(mappedData);
        } catch (Exception e) {
            return Result.failure("Error mapping result data: " + e.getMessage(), e);
        }
    }

    /**
     * Flat maps the success data to a new Result if successful.
     *
     * @param mapper The mapping function that returns a Result
     * @param <R> The new result type
     * @return A new Result from the mapper, or a failure if original was a failure
     */
    public <R> Result<R> flatMap(SuccessFlatMapper<T, R> mapper) {
        if (!success) {
            return Result.failure(errorMessage, exception);
        }

        if (mapper == null) {
            return Result.failure("Mapper function cannot be null");
        }

        try {
            return mapper.map(data);
        } catch (Exception e) {
            return Result.failure("Error flat mapping result data: " + e.getMessage(), e);
        }
    }

    // ================================
    // OVERRIDE METHODS
    // ================================

    @Override
    public String toString() {
        if (success) {
            return "Result{success=" + success + ", data=" + data + "}";
        } else {
            return "Result{success=" + success + ", errorMessage='" + errorMessage + "'}";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Result<?> result = (Result<?>) obj;

        if (success != result.success) return false;
        if (data != null ? !data.equals(result.data) : result.data != null) return false;
        if (errorMessage != null ? !errorMessage.equals(result.errorMessage) : result.errorMessage != null) return false;
        if (errorCode != null ? !errorCode.equals(result.errorCode) : result.errorCode != null) return false;
        return exception != null ? exception.equals(result.exception) : result.exception == null;
    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (errorCode != null ? errorCode.hashCode() : 0);
        result = 31 * result + (exception != null ? exception.hashCode() : 0);
        return result;
    }

    // ================================
    // CALLBACK INTERFACES
    // ================================

    /**
     * Callback interface for successful operations.
     *
     * @param <T> The type of the success data
     */
    public interface SuccessCallback<T> {
        void onSuccess(T data);
    }

    /**
     * Callback interface for failed operations.
     */
    public interface FailureCallback {
        void onFailure(String errorMessage, Exception exception);
    }

    /**
     * Mapper interface for transforming success data.
     *
     * @param <T> The source type
     * @param <R> The target type
     */
    public interface SuccessMapper<T, R> {
        R map(T data);
    }

    /**
     * Flat mapper interface for transforming success data to a new Result.
     *
     * @param <T> The source type
     * @param <R> The target type
     */
    public interface SuccessFlatMapper<T, R> {
        Result<R> map(T data);
    }
}
