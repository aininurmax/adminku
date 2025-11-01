package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Brand;

import java.util.List;

@Dao
public interface BrandDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Brand brand);

    @Update
    void update(Brand brand);

    @Delete
    void delete(Brand brand);

    @Query("SELECT * FROM Brand WHERE id = :id")
    LiveData<Brand> getById(String id);

    @Query("SELECT * FROM Brand WHERE id = :id")
    Brand getByIdSync(String id);

    @Query("SELECT * FROM Brand ORDER BY name")
    LiveData<List<Brand>> getAll();

    @Query("SELECT * FROM Brand ORDER BY name")
    List<Brand> getAllSync();

    @Query("SELECT * FROM Brand WHERE name LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    List<Brand> search(String query, int limit);

    @Query("SELECT COUNT(*) FROM Brand WHERE name = :name")
    int countByName(String name);

    @Query("SELECT COUNT(*) FROM Product WHERE brandId = :brandId")
    int countProductsByBrandId(String brandId);
}
