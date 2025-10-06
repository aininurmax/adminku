package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = "name", unique = true)})
public class Unit {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String baseUnit; // "pcs" or "gram"

    private long conversionFactor; // 1 for base units, otherwise conversion to base

    private boolean isBaseUnit;

    private long createdAt;

    private long updatedAt;

    public Unit(@NonNull String id, @NonNull String name, @NonNull String baseUnit,
                long conversionFactor, boolean isBaseUnit, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.baseUnit = baseUnit;
        this.conversionFactor = conversionFactor;
        this.isBaseUnit = isBaseUnit;
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

    @NonNull
    public String getBaseUnit() {
        return baseUnit;
    }

    public void setBaseUnit(@NonNull String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public long getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(long conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public boolean isBaseUnit() {
        return isBaseUnit;
    }

    public void setBaseUnit(boolean baseUnit) {
        isBaseUnit = baseUnit;
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
}

