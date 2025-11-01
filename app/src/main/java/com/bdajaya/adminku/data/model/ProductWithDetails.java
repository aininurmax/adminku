package com.bdajaya.adminku.data.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.bdajaya.adminku.data.entity.Brand;
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
            parentColumn = "brandId",
            entityColumn = "id"
    )
    public Brand brand;

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

    // Helper methods
    public String getCategoryName() {
        return category != null ? category.getName() : "No Category";
    }

    public String getBrandName() {
        return brand != null ? brand.getName() : "No Brand";
    }

    public String getUnitName() {
        return unit != null ? unit.getName() : "pcs";
    }

    public String getUnitDisplayText() {
        if (unit == null) return "pcs";
        return unit.isBaseUnit() ? unit.getName() :
               unit.getName() + " (" + unit.getConversionFactor() + " " + unit.getBaseUnit() + ")";
    }

    public String getFirstImagePath() {
        if (images != null && !images.isEmpty()) {
            // Images are sorted by orderIndex, so first one is the main image
            return images.get(0).getImagePath();
        }
        return null;
    }

    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }

    public int getImageCount() {
        return images != null ? images.size() : 0;
    }

    /**
     * Get stock in display unit (converted from base unit)
     */
    public long getDisplayStock() {
        if (unit == null) {
            return product.getStock();
        }
        return unit.fromBaseUnit(product.getStock());
    }

    /**
     * Get formatted stock with unit
     */
    public String getFormattedStock() {
        long displayStock = getDisplayStock();
        return displayStock + " " + getUnitName();
    }

    /**
     * Check if unit is compatible with another unit (same base unit)
     */
    public boolean isUnitCompatibleWith(Unit otherUnit) {
        if (unit == null || otherUnit == null) return false;
        return unit.getBaseUnit().equals(otherUnit.getBaseUnit());
    }
}