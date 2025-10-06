package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        indices = {
                @Index(value = "barcode", unique = true),
                @Index("name"),
                @Index("categoryId"),
                @Index("unitId"),
                @Index("status")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.SET_NULL
                )
        }
)
public class Product {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String name;

    @Nullable
    private String description;

    @NonNull
    private String barcode;

    @Nullable
    private String categoryId;

    @NonNull
    private String unitId;

    private long buyPrice; // in cents

    private long sellPrice; // in cents

    private int margin; // percentage

    private long stock; // in base unit

    @NonNull
    private String status; // "LIVE", "OUT_OF_STOCK", "ARCHIVED"

    private long createdAt;

    private long updatedAt;

    public Product(@NonNull String id, @NonNull String name, @Nullable String description,
                   @NonNull String barcode, @Nullable String categoryId, @NonNull String unitId,
                   long buyPrice, long sellPrice, int margin, long stock, @NonNull String status,
                   long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.barcode = barcode;
        this.categoryId = categoryId;
        this.unitId = unitId;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.margin = margin;
        this.stock = stock;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @NonNull
    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(@NonNull String barcode) {
        this.barcode = barcode;
    }

    @Nullable
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable String categoryId) {
        this.categoryId = categoryId;
    }

    @NonNull
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(@NonNull String unitId) {
        this.unitId = unitId;
    }

    public long getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(long buyPrice) {
        this.buyPrice = buyPrice;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(long sellPrice) {
        this.sellPrice = sellPrice;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public double getCogsRatio() {
        if (sellPrice == 0) return 0;
        return (double) buyPrice / sellPrice;
    }

    public void updateMarginFromPrices() {
        if (buyPrice == 0) {
            this.margin = 0;
            return;
        }
        this.margin = (int) (((double) (sellPrice - buyPrice) / buyPrice) * 100);
    }

    public void updateSellPriceFromMargin() {
        this.sellPrice = buyPrice + (buyPrice * margin / 100);
    }
}
