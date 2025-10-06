package com.bdajaya.adminku.data.model;

import com.bdajaya.adminku.data.entity.Category;

import java.util.List;

public class CategoryWithPath {
    private Category category;
    private List<Category> pathToRoot;
    private String pathString;

    public CategoryWithPath(Category category, List<Category> pathToRoot) {
        this.category = category;
        this.pathToRoot = pathToRoot;
        buildPathString();
    }

    private void buildPathString() {
        StringBuilder sb = new StringBuilder();
        for (int i = pathToRoot.size() - 1; i >= 0; i--) {
            sb.append(pathToRoot.get(i).getName());
            if (i > 0) {
                sb.append(" > ");
            }
        }
        this.pathString = sb.toString();
    }

    public Category getCategory() {
        return category;
    }

    public List<Category> getPathToRoot() {
        return pathToRoot;
    }

    public String getPathString() {
        return pathString;
    }
}

