package com.bdajaya.adminku.data.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Category;

import java.util.List;

/**
 * Data Access Object (DAO) for Category entities.
 * Provides methods for performing CRUD operations and complex queries on Category data.
 *
 * This DAO supports hierarchical category management, including:
 * - Root and child category retrieval
 * - Synchronous and LiveData query methods
 * - Search and filtering capabilities
 * - Batch operations
 */
@Dao
public interface CategoryDao {

    /**
     * Insert a single category
     * @param category Category to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(@NonNull Category category);

    /**
     * Update an existing category
     * @param category Category to update
     */
    @Update
    void update(@NonNull Category category);

    /**
     * Delete a category
     * @param category Category to delete
     */
    @Delete
    void delete(@NonNull Category category);

    /**
     * Get a category by its ID (synchronous)
     * @param id Category ID
     * @return Category or null if not found
     */
    @Nullable
    @Query("SELECT * FROM Category WHERE id = :id")
    Category getById(@NonNull String id);

    /**
     * Get a category by its ID (LiveData)
     * @param id Category ID
     * @return LiveData of Category
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE id = :id")
    LiveData<Category> getByIdLive(@NonNull String id);

    /**
     * Get root-level categories (LiveData)
     * @return LiveData list of root categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE parentId IS NULL ORDER BY name")
    LiveData<List<Category>> getRoots();

    /**
     * Get root-level categories (synchronous)
     * @return List of root categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE parentId IS NULL ORDER BY name")
    List<Category> getRootsSync();

    /**
     * Get children of a specific category (LiveData)
     * @param parentId Parent category ID
     * @return LiveData list of child categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE parentId = :parentId ORDER BY name")
    LiveData<List<Category>> getChildren(@NonNull String parentId);

    /**
     * Get children of a specific category (synchronous)
     * @param parentId Parent category ID
     * @return List of child categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE parentId = :parentId ORDER BY name")
    List<Category> getChildrenSync(@NonNull String parentId);

    /**
     * Count the number of children for a category
     * @param id Parent category ID
     * @return Number of children
     */
    @Query("SELECT COUNT(*) FROM Category WHERE parentId = :id")
    int countChildren(@NonNull String id);

    /**
     * Search categories by name
     * @param query Search query
     * @param limit Maximum number of results
     * @return List of matching categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE name LIKE '%' || :query || '%' ORDER BY level, name LIMIT :limit")
    List<Category> search(@NonNull String query, int limit);

    /**
     * Get categories by level
     * @param level Category level
     * @return List of categories at the specified level
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE level = :level ORDER BY name")
    List<Category> getByLevel(int level);

    /**
     * Get the maximum category level
     * @return Maximum category level
     */
    @Query("SELECT MAX(level) FROM Category")
    int getMaxLevel();

    /**
     * Count categories with the same parent and name
     * @param parentId Parent category ID
     * @param name Category name
     * @return Number of matching categories
     */
    @Query("SELECT COUNT(*) FROM Category WHERE parentId = :parentId AND name = :name")
    int countByParentAndName(@Nullable String parentId, @NonNull String name);

    /**
     * Count root-level categories with a specific name
     * @param name Category name
     * @return Number of matching root categories
     */
    @Query("SELECT COUNT(*) FROM Category WHERE parentId IS NULL AND name = :name")
    int countRootCategoriesByName(@NonNull String name);

    /**
     * Batch insert categories
     * @param categories List of categories to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(@NonNull List<Category> categories);

    /**
     * Batch delete categories
     * @param categories List of categories to delete
     */
    @Delete
    void deleteAll(@NonNull List<Category> categories);

    /**
     * Get paginated children of a category
     * @param parentId Parent category ID
     * @param pageSize Number of items per page
     * @param offset Offset for pagination
     * @return Paginated list of child categories
     */
    @NonNull
    @Query("SELECT * FROM Category WHERE parentId = :parentId ORDER BY name LIMIT :pageSize OFFSET :offset")
    List<Category> getChildrenPaginated(@NonNull String parentId, int pageSize, int offset);

    /**
     * Check if a category has any descendants
     * @param id Category ID
     * @return true if the category has descendants, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM Category WHERE parentId = :id")
    boolean hasDescendants(@NonNull String id);
}
