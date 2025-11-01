package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {
        @Index(value = "name", unique = true),
        @Index(value = "baseUnit"),
        @Index(value = "isBaseUnit")
})
public class Unit {
    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String name;

    @NonNull
    private String baseUnit; // "pcs" or "gr"

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

    // Getters
    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getBaseUnit() {
        return baseUnit;
    }

    public long getConversionFactor() {
        return conversionFactor;
    }

    public boolean isBaseUnit() {
        return isBaseUnit;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setBaseUnit(@NonNull String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public void setConversionFactor(long conversionFactor) {
        this.conversionFactor = conversionFactor;
    }

    public void setBaseUnit(boolean baseUnit) {
        isBaseUnit = baseUnit;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    /**
     * Convert quantity to base unit
     */
    public long toBaseUnit(long quantity) {
        return quantity * conversionFactor;
    }

    /**
     * Convert quantity from base unit
     */
    public long fromBaseUnit(long baseQuantity) {
        if (conversionFactor == 0) {
            return 0;
        }
        return baseQuantity / conversionFactor;
    }

    /**
     * Get display text for unit
     */
    public String getDisplayText() {
        if (isBaseUnit) {
            return name + " (Base)";
        }
        return name + " (" + conversionFactor + " " + baseUnit + ")";
    }

    /**
     * Check if compatible with another unit
     */
    public boolean isCompatibleWith(Unit other) {
        return this.baseUnit.equals(other.baseUnit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Unit unit = (Unit) o;
        return id.equals(unit.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Unit{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", baseUnit='" + baseUnit + '\'' +
                ", conversionFactor=" + conversionFactor +
                ", isBaseUnit=" + isBaseUnit +
                '}';
    }
}