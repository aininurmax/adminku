package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index("parentId"), @Index("name")})
public class Category {
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

    private long createdAt;

    private long updatedAt;

    public Category(@NonNull String id, @Nullable String parentId, int level,
                    @NonNull String name, @Nullable String iconUrl,
                    long createdAt, long updatedAt) {
        this.id = id;
        this.parentId = parentId;
        this.level = level;
        this.name = name;
        this.iconUrl = iconUrl;
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

    @Nullable
    public String getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable String parentId) {
        this.parentId = parentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
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

