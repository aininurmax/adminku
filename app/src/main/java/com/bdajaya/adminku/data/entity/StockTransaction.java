package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Objects;

@Entity(
        tableName = "StockTransaction",
        indices = {
                @Index(value = "productId"),
                @Index(value = "transactionType"),
                @Index(value = "timestamp"),
                @Index(value = "unitId"),
                @Index(value = {"productId", "timestamp"})
        },
        foreignKeys = {
                @ForeignKey(
                        entity = Product.class,
                        parentColumns = "id",
                        childColumns = "productId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Unit.class,
                        parentColumns = "id",
                        childColumns = "unitId",
                        onDelete = ForeignKey.RESTRICT
                )
        }
)
public class StockTransaction {

    public enum TransactionType {
        ADD("ADD"),
        REMOVE("REMOVE"),
        ADJUST("ADJUST");

        private final String value;

        TransactionType(String value) {
            this.value = value;
        }

        @NonNull
        public String getValue() {
            return value;
        }

        @Nullable
        public static TransactionType fromString(@Nullable String value) {
            for (TransactionType type : values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }
    }

    @PrimaryKey
    @NonNull
    private String id;

    @NonNull
    private String productId;

    @NonNull
    private String transactionType;

    private long quantity; // in base unit - ALWAYS stored in base unit

    private long originalQuantity; // quantity in original unit (for display)

    private long originalConversionFactor; // to preserve history

    @NonNull
    private String unitId;

    @Nullable
    private String notes;

    private long timestamp;

    // Constructor
    public StockTransaction(@NonNull String id, @NonNull String productId,
                            @NonNull String transactionType, long quantity,
                            long originalQuantity, long originalConversionFactor,
                            @NonNull String unitId, @Nullable String notes, long timestamp) {
        this.id = id;
        this.productId = productId;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.originalQuantity = originalQuantity;
        this.originalConversionFactor = originalConversionFactor;
        this.unitId = unitId;
        this.notes = notes;
        this.timestamp = timestamp;
    }

    // Builder Pattern
    public static class Builder {
        private String id;
        private String productId;
        private TransactionType transactionType;
        private long quantity;
        private long originalQuantity;
        private long originalConversionFactor = 1;
        private String unitId;
        private String notes;
        private long timestamp;

        public Builder(@NonNull String productId, @NonNull TransactionType transactionType) {
            this.productId = productId;
            this.transactionType = transactionType;
            this.timestamp = System.currentTimeMillis();
            this.id = java.util.UUID.randomUUID().toString();
        }

        public Builder id(@NonNull String id) {
            this.id = id;
            return this;
        }

        public Builder quantity(long quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder originalQuantity(long originalQuantity) {
            this.originalQuantity = originalQuantity;
            return this;
        }

        public Builder originalConversionFactor(long originalConversionFactor) {
            this.originalConversionFactor = originalConversionFactor;
            return this;
        }

        public Builder unitId(@NonNull String unitId) {
            this.unitId = unitId;
            return this;
        }

        public Builder notes(@Nullable String notes) {
            this.notes = notes;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public StockTransaction build() {
            if (productId == null || transactionType == null || unitId == null) {
                throw new IllegalStateException("productId, transactionType, and unitId are required");
            }
            return new StockTransaction(id, productId, transactionType.getValue(),
                    quantity, originalQuantity, originalConversionFactor, unitId, notes, timestamp);
        }
    }

    // Getters and Setters
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
    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(@NonNull String transactionType) {
        this.transactionType = transactionType;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public long getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(long originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public long getOriginalConversionFactor() {
        return originalConversionFactor;
    }

    public void setOriginalConversionFactor(long originalConversionFactor) {
        this.originalConversionFactor = originalConversionFactor;
    }

    @NonNull
    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(@NonNull String unitId) {
        this.unitId = unitId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Helper Methods
    @Nullable
    public TransactionType getTransactionTypeEnum() {
        return TransactionType.fromString(transactionType);
    }

    public void setTransactionType(@NonNull TransactionType type) {
        this.transactionType = type.getValue();
    }

    public boolean isAddition() {
        return TransactionType.ADD.getValue().equals(transactionType);
    }

    public boolean isRemoval() {
        return TransactionType.REMOVE.getValue().equals(transactionType);
    }

    public boolean isAdjustment() {
        return TransactionType.ADJUST.getValue().equals(transactionType);
    }

    /**
     * Get effective quantity (signed based on transaction type)
     */
    public long getEffectiveQuantity() {
        if (isAddition()) {
            return quantity;
        } else if (isRemoval()) {
            return -quantity;
        } else {
            return quantity;
        }
    }

    /**
     * Get display string for transaction
     */
    public String getDisplayQuantity() {
        String sign = isAddition() ? "+" : (isRemoval() ? "-" : "");
        return sign + originalQuantity;
    }

    public boolean isValid() {
        return id != null && !id.trim().isEmpty()
                && productId != null && !productId.trim().isEmpty()
                && transactionType != null && !transactionType.trim().isEmpty()
                && unitId != null && !unitId.trim().isEmpty()
                && quantity >= 0
                && timestamp > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockTransaction that = (StockTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    @NonNull
    public String toString() {
        return "StockTransaction{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", quantity=" + quantity +
                ", originalQuantity=" + originalQuantity +
                ", unitId='" + unitId + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}