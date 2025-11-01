package com.bdajaya.adminku.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.bdajaya.adminku.core.Constants;

import java.util.Objects;

/**
 * Entity class representing a category in the application.
 * Categories can have a hierarchical structure with parent-child relationships.
 */
@Entity(indices = {@Index("parentId"), @Index("name")})
public class Category implements Parcelable {
    @PrimaryKey
    @NonNull
    private String id;

    @Nullable
    private String parentId;

    private int level;

    @NonNull
    private String name;

    @Nullable
    private String iconUrl;

    private boolean hasChildren;

    private long createdAt;

    private long updatedAt;

    // Constants
    private static final int MIN_LEVEL = 0;

    /**
     * Creates a new Category with all fields.
     *
     * @param id Unique identifier for the category
     * @param parentId ID of the parent category, null for root categories
     * @param level Hierarchy level (0 for root, increases for children)
     * @param name Display name of the category
     * @param iconUrl URL or path to category icon
     * @param hasChildren Whether this category has child categories
     * @param createdAt Creation timestamp
     * @param updatedAt Last update timestamp
     * @throws IllegalArgumentException if validation fails
     */
    public Category(@NonNull String id, @Nullable String parentId, int level,
                    @NonNull String name, @Nullable String iconUrl, boolean hasChildren,
                    long createdAt, long updatedAt) {
        setId(id);
        setParentId(parentId);
        setLevel(level);
        setName(name);
        setIconUrl(iconUrl);
        setHasChildren(hasChildren);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    /**
     * Creates a new root category with current timestamp.
     *
     * @param id Unique identifier for the category
     * @param name Display name of the category
     */
    @Ignore
    public Category(@NonNull String id, @NonNull String name) {
        this(id, null, MIN_LEVEL, name, null, false,
             System.currentTimeMillis(), System.currentTimeMillis());
    }

    /**
     * Default constructor for Room database operations.
     */
    @Ignore
    public Category() {
        // Room will use setters for field initialization
    }
    /**
     * Checks if this category can be selected (doesn't have children).
     * Implementation should be moved to repository layer.
     *
     * @return true if category can be selected, false otherwise
     */
    public boolean isSelectable() {
        // TODO: Move implementation to repository layer
        return !hasChildren;
    }

    /**
     * Checks if this category can have subcategories based on current level.
     *
     * @return true if category can have children, false if at max level
     */
    public boolean canHaveSubcategory() {
        return this.level < Constants.MAX_CATEGORY_LEVEL;
    }

    /**
     * Checks if this is a root category (has no parent).
     *
     * @return true if this is a root category, false otherwise
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = Objects.requireNonNull(id, "Category ID cannot be null");
    }

    @Nullable
    public String getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable String parentId) {
        // Validate parent ID doesn't create circular reference
        if (parentId != null && parentId.equals(this.id)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
        this.parentId = parentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < MIN_LEVEL) {
            throw new IllegalArgumentException("Category level cannot be negative");
        }
        if (level >= Constants.MAX_CATEGORY_LEVEL) {
            throw new IllegalArgumentException("Category level cannot exceed maximum allowed level: " + Constants.MAX_CATEGORY_LEVEL);
        }
        this.level = level;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = Objects.requireNonNull(name, "Category name cannot be null").trim();
        if (this.name.isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        updateTimestamp();
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
        updateTimestamp();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        if (createdAt < 0) {
            throw new IllegalArgumentException("Creation timestamp cannot be negative");
        }
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        if (updatedAt < 0) {
            throw new IllegalArgumentException("Update timestamp cannot be negative");
        }
        if (updatedAt < this.createdAt) {
            throw new IllegalArgumentException("Update timestamp cannot be before creation timestamp");
        }
        this.updatedAt = updatedAt;
    }

    public boolean hasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    /**
     * Updates the updatedAt timestamp to current time.
     */
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Category category = (Category) obj;
        return Objects.equals(id, category.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", level=" + level +
                ", name='" + name + '\'' +
                ", hasChildren=" + hasChildren +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // Parcelable implementation

    /**
     * Constructor for creating Category from Parcel.
     * Validates data during deserialization.
     *
     * @param in Parcel to read data from
     * @throws IllegalArgumentException if parcel data is invalid
     */
    protected Category(Parcel in) {
        try {
            // Read and validate required fields
            String parcelId = in.readString();
            if (parcelId == null || parcelId.trim().isEmpty()) {
                throw new IllegalArgumentException("Category ID cannot be null or empty");
            }
            this.id = parcelId;

            this.parentId = in.readString();
            this.level = in.readInt();
            this.name = in.readString();
            this.iconUrl = in.readString();
            this.hasChildren = in.readByte() != 0;
            this.createdAt = in.readLong();
            this.updatedAt = in.readLong();

            // Validate data integrity
            validateParcelData();

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to read Category from parcel: " + e.getMessage(), e);
        }
    }

    /**
     * Validates data read from parcel for consistency.
     *
     * @throws IllegalArgumentException if data is invalid
     */
    private void validateParcelData() {
        // Validate required fields
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }

        // Validate level constraints
        if (level < MIN_LEVEL) {
            throw new IllegalArgumentException("Category level cannot be negative");
        }
        if (level >= Constants.MAX_CATEGORY_LEVEL) {
            throw new IllegalArgumentException("Category level cannot exceed maximum allowed level");
        }

        // Validate timestamps
        if (createdAt < 0) {
            throw new IllegalArgumentException("Creation timestamp cannot be negative");
        }
        if (updatedAt < 0) {
            throw new IllegalArgumentException("Update timestamp cannot be negative");
        }
        if (updatedAt < createdAt) {
            throw new IllegalArgumentException("Update timestamp cannot be before creation timestamp");
        }

        // Validate parent reference
        if (parentId != null && parentId.equals(this.id)) {
            throw new IllegalArgumentException("Category cannot be its own parent");
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest == null) {
            throw new IllegalArgumentException("Parcel destination cannot be null");
        }

        try {
            dest.writeString(id);
            dest.writeString(parentId);
            dest.writeInt(level);
            dest.writeString(name);
            dest.writeString(iconUrl);
            dest.writeByte((byte) (hasChildren ? 1 : 0));
            dest.writeLong(createdAt);
            dest.writeLong(updatedAt);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to write Category to parcel: " + e.getMessage(), e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Creator for parceling Category objects.
     * Provides methods to create Category from parcel and create arrays.
     */
    private static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    /**
     * Gets the Parcelable.Creator for this class.
     * Exposed for external parceling operations.
     *
     * @return the CREATOR instance
     */
    public static Creator<Category> getCreator() {
        return CREATOR;
    }
}
