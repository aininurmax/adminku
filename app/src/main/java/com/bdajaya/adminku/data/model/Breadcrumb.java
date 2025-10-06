package com.bdajaya.adminku.data.model;

import com.bdajaya.adminku.data.entity.Category;

public class Breadcrumb {
    private String id;
    private String name;
    private int level;

    public Breadcrumb(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.level = category.getLevel();
    }

    public Breadcrumb(String id, String name, int level) {
        this.id = id;
        this.name = name;
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }
}

