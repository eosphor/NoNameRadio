package com.nonameradio.app.core.architecture;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Generic service interface for business operations.
 * Provides async operations using CompletableFuture for modern concurrency.
 *
 * @param <T> The entity type
 * @param <ID> The identifier type
 */
public interface IService<T, ID> {
    /**
     * Get all entities asynchronously
     * @return CompletableFuture with list of entities
     */
    CompletableFuture<Result<List<T>>> getAll();

    /**
     * Get entity by ID asynchronously
     * @param id The entity identifier
     * @return CompletableFuture with result containing entity or error
     */
    CompletableFuture<Result<T>> getById(ID id);

    /**
     * Save entity asynchronously
     * @param entity The entity to save
     * @return CompletableFuture with result containing saved entity or error
     */
    CompletableFuture<Result<T>> save(T entity);

    /**
     * Delete entity by ID asynchronously
     * @param id The entity identifier
     * @return CompletableFuture with result indicating success/failure
     */
    CompletableFuture<Result<Void>> deleteById(ID id);

    /**
     * Result wrapper for service operations
     * @param <T> The result data type
     */
    class Result<T> {
        private final T data;
        private final Exception error;

        private Result(T data, Exception error) {
            this.data = data;
            this.error = error;
        }

        public static <T> Result<T> success(T data) {
            return new Result<>(data, null);
        }

        public static <T> Result<T> error(Exception error) {
            return new Result<>(null, error);
        }

        public boolean isSuccess() {
            return error == null;
        }

        public T getData() {
            return data;
        }

        public Exception getError() {
            return error;
        }
    }
}
