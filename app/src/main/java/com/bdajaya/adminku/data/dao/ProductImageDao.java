package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.ProductImage;

import java.util.List;

@Dao
public interface ProductImageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ProductImage productImage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProductImage> productImages);

    @Update
    void update(ProductImage productImage);

    @Delete
    void delete(ProductImage productImage);

    @Query("DELETE FROM ProductImage WHERE productId = :productId")
    void deleteAllForProduct(String productId);

    @Query("SELECT * FROM ProductImage WHERE id = :id")
    ProductImage getById(String id);

    @Query("SELECT * FROM ProductImage WHERE productId = :productId ORDER BY orderIndex")
    LiveData<List<ProductImage>> getByProductId(String productId);

    @Query("SELECT * FROM ProductImage WHERE productId = :productId ORDER BY orderIndex")
    List<ProductImage> getByProductIdSync(String productId);

    @Query("SELECT COUNT(*) FROM ProductImage WHERE productId = :productId")
    int countByProductId(String productId);

    @Query("UPDATE ProductImage SET orderIndex = :newIndex WHERE id = :id")
    void updateOrderIndex(String id, int newIndex);
}
