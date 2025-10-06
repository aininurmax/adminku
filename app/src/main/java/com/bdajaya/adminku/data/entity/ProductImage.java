package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        indices = {@Index("productId")},
        foreignKeys = @ForeignKey(
                entity = Product.class,
                parentColumns = "id",
                childColumns = "productId",
                onDelete = ForeignKey.CASCADE
        )
)
public class ProductImage {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String productId;

    @NonNull
    private String imageBase64;

    private int orderIndex;

    private long createdAt;

    public ProductImage(@NonNull String id, @NonNull String productId,
                        @NonNull String imageBase64, int orderIndex, long createdAt) {
        this.id = id;
        this.productId = productId;
        this.imageBase64 = imageBase64;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getProductId() {
        return productId;
    }

    public void setProductId(@NonNull String productId) {
        this.productId = productId;
    }

    @NonNull
    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(@NonNull String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}


