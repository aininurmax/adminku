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

    // Simpan path file relatif, bukan Base64
    @NonNull
    private String imagePath; // e.g., "products/prod_123/image_1.jpg"

    private int orderIndex;
    private long createdAt;

    // Optional: simpan metadata
    private long fileSize;
    private int width;
    private int height;

    public ProductImage(@NonNull String id, @NonNull String productId,
                        @NonNull String imagePath, int orderIndex, long createdAt) {
        this.id = id;
        this.productId = productId;
        this.imagePath = imagePath;
        this.orderIndex = orderIndex;
        this.createdAt = createdAt;
    }

    // Getters and setters
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }

    @NonNull
    public String getProductId() { return productId; }
    public void setProductId(@NonNull String productId) { this.productId = productId; }

    @NonNull
    public String getImagePath() { return imagePath; }
    public void setImagePath(@NonNull String imagePath) { this.imagePath = imagePath; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
}