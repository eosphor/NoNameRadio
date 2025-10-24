package com.nonameradio.app.core.architecture;

import java.util.List;

/**
 * Generic repository interface for data access operations.
 * Provides basic CRUD operations for any entity type.
 *
 * @param <T> The entity type
 * @param <ID> The identifier type
 */
public interface IRepository<T, ID> {
    /**
     * Retrieve all entities
     * @return List of all entities
     */
    List<T> getAll();

    /**
     * Find entity by ID
     * @param id The entity identifier
     * @return The entity or null if not found
     */
    T findById(ID id);

    /**
     * Save or update an entity
     * @param entity The entity to save
     * @return The saved entity
     */
    T save(T entity);

    /**
     * Delete an entity by ID
     * @param id The entity identifier
     * @return true if deleted, false if not found
     */
    boolean deleteById(ID id);

    /**
     * Check if entity exists by ID
     * @param id The entity identifier
     * @return true if exists
     */
    boolean existsById(ID id);

    /**
     * Get total count of entities
     * @return Total count
     */
    long count();
}
