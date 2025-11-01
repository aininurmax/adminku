package com.bdajaya.adminku.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.bdajaya.adminku.data.entity.StockTransaction;
import com.bdajaya.adminku.data.model.StockTransactionWithUnit;

import java.util.List;

@Dao
public interface StockTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(StockTransaction stockTransaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<StockTransaction> transactions);

    @Query("SELECT * FROM StockTransaction WHERE id = :id")
    StockTransaction getById(String id);

    @Query("SELECT * FROM StockTransaction WHERE productId = :productId ORDER BY timestamp DESC")
    LiveData<List<StockTransaction>> getByProductId(String productId);

    @Query("SELECT * FROM StockTransaction WHERE productId = :productId ORDER BY timestamp DESC")
    List<StockTransaction> getByProductIdSync(String productId);

    @Transaction
    @Query("SELECT * FROM StockTransaction WHERE productId = :productId ORDER BY timestamp DESC")
    LiveData<List<StockTransactionWithUnit>> getByProductIdWithUnit(String productId);

    @Query("SELECT * FROM StockTransaction WHERE productId = :productId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<StockTransaction> getByProductIdPaginated(String productId, int limit, int offset);

    /**
     * Calculate total stock in BASE UNIT
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN transactionType = 'ADD' THEN quantity " +
            "WHEN transactionType = 'REMOVE' THEN -quantity " +
            "WHEN transactionType = 'ADJUST' THEN quantity " +
            "ELSE 0 END), 0) " +
            "FROM StockTransaction WHERE productId = :productId")
    long getTotalStockForProduct(String productId);

    /**
     * Get last stock adjustment (for ADJUST type calculation)
     */
    @Query("SELECT COALESCE(SUM(quantity), 0) FROM StockTransaction " +
            "WHERE productId = :productId AND transactionType = 'ADJUST' " +
            "AND timestamp = (SELECT MAX(timestamp) FROM StockTransaction " +
            "WHERE productId = :productId AND transactionType = 'ADJUST')")
    long getLastAdjustmentStock(String productId);

    /**
     * Get stock changes after last adjustment
     */
    @Query("SELECT COALESCE(SUM(CASE " +
            "WHEN transactionType = 'ADD' THEN quantity " +
            "WHEN transactionType = 'REMOVE' THEN -quantity " +
            "ELSE 0 END), 0) " +
            "FROM StockTransaction WHERE productId = :productId " +
            "AND timestamp > COALESCE((SELECT MAX(timestamp) FROM StockTransaction " +
            "WHERE productId = :productId AND transactionType = 'ADJUST'), 0)")
    long getStockChangesSinceLastAdjustment(String productId);

    @Query("SELECT * FROM StockTransaction WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    List<StockTransaction> getByTimeRange(long startTime, long endTime);

    @Query("SELECT * FROM StockTransaction ORDER BY timestamp DESC LIMIT :limit")
    List<StockTransaction> getRecentTransactions(int limit);

    @Query("SELECT * FROM StockTransaction WHERE productId = :productId AND transactionType = :transactionType ORDER BY timestamp DESC")
    List<StockTransaction> getByProductIdAndType(String productId, String transactionType);

    @Query("SELECT * FROM StockTransaction WHERE productId = :productId ORDER BY timestamp DESC LIMIT 1")
    StockTransaction getLastTransactionForProduct(String productId);

    @Query("SELECT COUNT(*) FROM StockTransaction WHERE productId = :productId")
    int getTransactionCountForProduct(String productId);

    @Query("DELETE FROM StockTransaction WHERE productId = :productId")
    int deleteByProductId(String productId);

    @Query("DELETE FROM StockTransaction WHERE timestamp < :timestamp")
    int deleteOlderThan(long timestamp);

    /**
     * Get transactions grouped by unit
     */
    @Query("SELECT unitId, SUM(CASE " +
            "WHEN transactionType = 'ADD' THEN quantity " +
            "WHEN transactionType = 'REMOVE' THEN -quantity " +
            "ELSE 0 END) as totalQuantity " +
            "FROM StockTransaction WHERE productId = :productId " +
            "GROUP BY unitId")
    List<UnitStockSummary> getStockSummaryByUnit(String productId);

    class UnitStockSummary {
        public String unitId;
        public long totalQuantity;
    }
}