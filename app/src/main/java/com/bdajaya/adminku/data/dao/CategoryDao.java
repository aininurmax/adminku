package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Category;

import java.util.List;


@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Category category);

    @Update
    void update(Category category);

    @Delete
    void delete(Category category);

    @Query("SELECT * FROM Category WHERE id = :id")
    Category getById(String id);

    @Query("SELECT * FROM Category WHERE id = :id")
    LiveData<Category> getByIdLive(String id);

    @Query("SELECT * FROM Category WHERE parentId IS NULL ORDER BY name")
    LiveData<List<Category>> getRoots();

    @Query("SELECT * FROM Category WHERE parentId IS NULL ORDER BY name")
    List<Category> getRootsSync();

    @Query("SELECT * FROM Category WHERE parentId = :parentId ORDER BY name")
    LiveData<List<Category>> getChildren(String parentId);

    @Query("SELECT * FROM Category WHERE parentId = :parentId ORDER BY name")
    List<Category> getChildrenSync(String parentId);

    @Query("SELECT COUNT(*) FROM Category WHERE parentId = :id")
    int countChildren(String id);

    @Query("SELECT * FROM Category WHERE name LIKE '%' || :query || '%' ORDER BY level, name LIMIT :limit")
    List<Category> search(String query, int limit);

    @Query("SELECT * FROM Category WHERE level = :level ORDER BY name")
    List<Category> getByLevel(int level);

    @Query("SELECT MAX(level) FROM Category")
    int getMaxLevel();

    @Query("SELECT COUNT(*) FROM Category WHERE parentId = :parentId AND name = :name")
    int countByParentAndName(String parentId, String name);
}
