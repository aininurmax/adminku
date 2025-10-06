package com.bdajaya.adminku.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.entity.Product;
import com.bdajaya.adminku.data.entity.ProductImage;
import com.bdajaya.adminku.data.entity.Unit;

import java.util.List;

public class ProductWithDetails {
    @Embedded
    public Product product;

    @Relation(
            parentColumn = "categoryId",
            entityColumn = "id"
    )
    public Category category;

    @Relation(
            parentColumn = "unitId",
            entityColumn = "id"
    )
    public Unit unit;

    @Relation(
            parentColumn = "id",
            entityColumn = "productId"
    )
    public List<ProductImage> images;

    public String getFirstImageBase64() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageBase64();
        }
        return null;
    }

    public String getCategoryName() {
        return category != null ? category.getName() : "Uncategorized";
    }

    public String getUnitName() {
        return unit != null ? unit.getName() : "";
    }
}

