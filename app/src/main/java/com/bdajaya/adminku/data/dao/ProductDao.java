package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.model.ProductWithDetails;

import java.util.List;

@Dao
public interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM Product WHERE id = :id")
    LiveData<Product> getById(String id);

    @Query("SELECT * FROM Product WHERE id = :id")
    Product getByIdSync(String id);

    @Query("SELECT * FROM Product WHERE barcode = :barcode")
    Product getByBarcode(String barcode);

    @Query("SELECT * FROM Product WHERE status = :status ORDER BY name")
    LiveData<List<Product>> getByStatus(String status);

    @Query("SELECT * FROM Product WHERE status = :status ORDER BY name")
    List<Product> getByStatusSync(String status);

    @Query("SELECT * FROM Product WHERE categoryId = :categoryId")
    List<Product> getByCategoryId(String categoryId);

    @Query("SELECT COUNT(*) FROM Product WHERE categoryId = :categoryId")
    int countByCategoryId(String categoryId);

    @Query("SELECT * FROM Product WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    List<Product> search(String query, int limit);

    @Query("SELECT * FROM Product WHERE brandId = :brandId")
    List<Product> getByBrandId(String brandId);

    @Query("SELECT COUNT(*) FROM Product WHERE brandId = :brandId")
    int countByBrandId(String brandId);

    @Transaction
    @Query("SELECT * FROM Product WHERE id = :id")
    LiveData<ProductWithDetails> getProductWithDetails(String id);

    @Transaction
    @Query("SELECT * FROM Product WHERE status = :status ORDER BY name")
    LiveData<List<ProductWithDetails>> getProductsWithDetailsByStatus(String status);

    @Transaction
    @Query("SELECT * FROM Product WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' ORDER BY name LIMIT :limit")
    List<ProductWithDetails> searchWithDetails(String query, int limit);

    @Query("UPDATE Product SET status = :status WHERE id = :id")
    void updateStatus(String id, String status);

    @Query("UPDATE Product SET stock = stock + :quantity WHERE id = :id")
    void updateStock(String id, long quantity);

    @Query("SELECT MAX(CAST(SUBSTR(barcode, 4) AS INTEGER)) FROM Product WHERE barcode LIKE 'BE-%'")
    int getMaxBarcodeNumber();
}
