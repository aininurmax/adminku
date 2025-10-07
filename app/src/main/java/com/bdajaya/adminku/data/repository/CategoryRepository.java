package com.bdajaya.adminku.data.repository;

import androidx.lifecycle.LiveData;

import com.bdajaya.adminku.data.AppDatabase;
import com.bdajaya.adminku.data.dao.CategoryDao;
import com.bdajaya.adminku.data.dao.ProductDao;
import com.bdajaya.adminku.data.entity.Category;
import com.bdajaya.adminku.data.model.CategoryWithPath;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class CategoryRepository {
    private final CategoryDao categoryDao;
    private static final int MAX_DEPTH = 5;

    public CategoryRepository(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    public LiveData<List<Category>> getRootCategories() {
        return categoryDao.getRoots();
    }

    public List<Category> getRootCategoriesSync() {
        return categoryDao.getRootsSync();
    }

    public LiveData<List<Category>> getChildCategories(String parentId) {
        return categoryDao.getChildren(parentId);
    }

    public List<Category> getChildCategoriesSync(String parentId) {
        return categoryDao.getChildrenSync(parentId);
    }

    public LiveData<Category> getCategoryById(String id) {
        return categoryDao.getByIdLive(id);
    }

    public Category getCategoryByIdSync(String id) {
        return categoryDao.getById(id);
    }

    public List<Category> searchCategories(String query, int limit) {
        return categoryDao.search(query, limit);
    }

    public List<CategoryWithPath> searchCategoriesWithPath(String query, int limit) {
        List<Category> categories = categoryDao.search(query, limit);
        List<CategoryWithPath> result = new ArrayList<>();

        for (Category category : categories) {
            List<Category> path = pathToRoot(category.getId());
            result.add(new CategoryWithPath(category, path));
        }

        return result;
    }

    public List<Category> pathToRoot(String categoryId) {
        List<Category> path = new ArrayList<>();
        Category current = categoryDao.getById(categoryId);

        while (current != null) {
            path.add(current);
            if (current.getParentId() == null) {
                break;
            }
            current = categoryDao.getById(current.getParentId());
        }

        return path;
    }

    public String addCategory(String parentId, String name) {
        try {
            Future<String> future = AppDatabase.databaseWriteExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    // Check if name already exists at this level
                    if (categoryDao.countByParentAndName(parentId, name) > 0) {
                        return null; // Name already exists
                    }

                    // Calculate level
                    int level = 0;
                    if (parentId != null) {
                        Category parent = categoryDao.getById(parentId);
                        if (parent != null) {
                            level = parent.getLevel() + 1;

                            // Check max depth
                            if (level >= MAX_DEPTH) {
                                return "MAX_DEPTH_REACHED";
                            }
                        }
                    }

                    // Create and insert new category
                    String id = UUID.randomUUID().toString();
                    long now = System.currentTimeMillis();

                    Category category = new Category(
                            id,
                            parentId,
                            level,
                            name,
                            null,
                            now,
                            now
                    );

                    categoryDao.insert(category);
                    return id;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateCategory(String id, String newName) {
        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Category category = categoryDao.getById(id);
                    if (category == null) {
                        return false;
                    }

                    // Check if name already exists at this level
                    if (categoryDao.countByParentAndName(category.getParentId(), newName) > 0) {
                        return false;
                    }

                    category.setName(newName);
                    category.setUpdatedAt(System.currentTimeMillis());
                    categoryDao.update(category);
                    return true;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String deleteCategory(String id, ProductDao productDao) {
        try {
            Future<String> future = AppDatabase.databaseWriteExecutor.submit(new Callable<String>() {
                @Override
                public String call() {
                    // Check if category has children
                    if (categoryDao.countChildren(id) > 0) {
                        return "HAS_CHILDREN";
                    }

                    // Check if category has products
                    if (productDao.countByCategoryId(id) > 0) {
                        return "HAS_PRODUCTS";
                    }

                    // Delete category
                    Category category = categoryDao.getById(id);
                    if (category != null) {
                        categoryDao.delete(category);
                        return "SUCCESS";
                    }

                    return "NOT_FOUND";
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "ERROR";
        }
    }

    public int getMaxLevel() {
        try {
            Future<Integer> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return categoryDao.getMaxLevel();
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean isMaxDepthReached(String parentId) {
        if (parentId == null) {
            return false;
        }

        try {
            Future<Boolean> future = AppDatabase.databaseWriteExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    Category parent = categoryDao.getById(parentId);
                    return parent != null && parent.getLevel() >= MAX_DEPTH - 1;
                }
            });

            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return true; // Safer to assume max depth is reached in case of error
        }
    }

    public void insert(Category newCategory) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoryDao.insert(newCategory);
        });
    }
}
