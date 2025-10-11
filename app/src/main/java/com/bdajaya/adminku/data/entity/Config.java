package com.bdajaya.adminku.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Config {
    @PrimaryKey
    @NonNull
    private String key;

    @NonNull
    private String value;

    public Config(@NonNull String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }
}
