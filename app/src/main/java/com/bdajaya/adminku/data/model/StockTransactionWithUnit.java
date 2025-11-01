package com.bdajaya.adminku.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.bdajaya.adminku.data.entity.StockTransaction;
import com.bdajaya.adminku.data.entity.Unit;

/**
 * Model for stock transaction with unit details
 */
public class StockTransactionWithUnit {
    @Embedded
    public StockTransaction transaction;

    @Relation(
            parentColumn = "unitId",
            entityColumn = "id"
    )
    public Unit unit;

    public String getDisplayText() {
        if (unit == null) return transaction.getDisplayQuantity();
        return transaction.getDisplayQuantity() + " " + unit.getName();
    }
}