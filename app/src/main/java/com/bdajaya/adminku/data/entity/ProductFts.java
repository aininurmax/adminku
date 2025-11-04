package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Fts4;

/**
 * Full-text search virtual table for Product (name + description).
 * This maps to an FTS4 virtual table. Migration will create this virtual table and populate it.
 *
 * Note: contentEntity linking is handled by migration; Room's @Fts4 can be used when creating
 * the entity fully managed by Room. Here we declare the FTS entity for code clarity.
 */
@Fts4(contentEntity = Product.class)
@Entity(tableName = "ProductFts")
public class ProductFts {
    @NonNull
    public String name;

    public String description;

    public ProductFts(@NonNull String name, String description) {
        this.name = name;
        this.description = description;
    }
}